package com.podcasses.view;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastChannelBinding;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.PodcastChannelViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    AuthenticationCallInterface authenticationCallInterface;

    private FragmentPodcastChannelBinding binding;
    private PodcastChannelViewModel viewModel;

    private static String podcastChannelId;
    private static PodcastChannel podcastChannel;
    private static boolean isSubscribed;

    private MutableLiveData<String> token;

    private LiveData<ApiResponse> podcastChannelResponse;
    private LiveData<ApiResponse> podcastsResponse;
    private LiveData<ApiResponse> podcastChannelSubscribeStatus;

    private int page = 0;

    private AdLoader adLoader;

    static PodcastChannelFragment newInstance(int instance, String podcastChannelId, PodcastChannel podcastChannel, boolean isSubscribed) {
        PodcastChannelFragment.podcastChannelId = podcastChannelId;
        PodcastChannelFragment.podcastChannel = podcastChannel;
        PodcastChannelFragment.isSubscribed = isSubscribed;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        PodcastChannelFragment fragment = new PodcastChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast_channel, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastChannelViewModel.class);
        binding.setViewModel(viewModel);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.e(getTag(), "getInstanceId failed", task.getException());
                        return;
                    }
                    binding.setDeviceId(task.getResult().getToken());
                });

        setPodcastClick();
        setAuthorClick();
        setChannelClick();
        return binding.getRoot();
    }

    void updateActionBar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.setIsSubscribed(isSubscribed);

        if (podcastChannel != null) {
            viewModel.setPodcastChannel(podcastChannel);
        } else {
            podcastChannelResponse = viewModel.podcastChannel(this, podcastChannelId);
            podcastChannelResponse.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcastChannelResponse));
        }

        token = AuthenticationUtil.getAuthenticationToken(getContext(), authenticationCallInterface);
        if (token != null) {
            token.observe(getViewLifecycleOwner(), s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    token.removeObservers(this);
                    JWT jwt = new JWT(s);

                    if (podcastChannel != null && jwt.getSubject().equals(podcastChannel.getUserId())) {
                        viewModel.setIsMyPodcastChannel(true);
                        loadPodcasts(true, true);
                        setInfiniteScrollListener(true);
                    }

                    binding.setToken(s);
                    processPodcastChannelStatisticData(s);
                    podcastChannelSubscribeStatus = viewModel.checkPodcastChannelSubscribe(s, podcastChannelId);
                    podcastChannelSubscribeStatus.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcastChannelSubscribeStatus));
                } else if (AccountManager.get(getContext()).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE).length != 0) {
                    token.removeObservers(this);
                    processPodcastChannelStatisticData(null);
                    loadPodcasts(false, false);
                    setInfiniteScrollListener(false);
                }
            });
        } else {
            processPodcastChannelStatisticData(null);
            loadPodcasts(false, false);
            setInfiniteScrollListener(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.podcast_channel_menu, menu);
    }

    private void processPodcastChannelStatisticData(String token) {
        LiveData<ApiResponse> podcastChannelViews = viewModel.podcastChannelViews(podcastChannelId);
        LiveData<ApiResponse> podcastChannelSubscribes = viewModel.podcastChannelSubscribes(podcastChannelId);
        LiveData<ApiResponse> podcastChannelEpisodes = viewModel.podcastChannelEpisodes(token, podcastChannelId);

        podcastChannelViews.observe(getViewLifecycleOwner(), a -> {
            if (a.status == ApiResponse.Status.SUCCESS) {
                podcastChannelViews.removeObservers(this);
                viewModel.setViews((Integer) a.data);
            } else if (a.status == ApiResponse.Status.ERROR) {
                podcastChannelViews.removeObservers(this);
                Log.e("ErrorResponse", String.format("API Error response from url %1$s: ", a.url), a.error);
            }
        });

        podcastChannelSubscribes.observe(getViewLifecycleOwner(), a -> {
            if (a.status == ApiResponse.Status.SUCCESS) {
                podcastChannelSubscribes.removeObservers(this);
                viewModel.setSubscribes((Integer) a.data);
            } else if (a.status == ApiResponse.Status.ERROR) {
                podcastChannelSubscribes.removeObservers(this);
                Log.e("ErrorResponse", String.format("API Error response from url %1$s: ", a.url), a.error);
            }
        });

        podcastChannelEpisodes.observe(getViewLifecycleOwner(), a -> {
            if (a.status == ApiResponse.Status.SUCCESS) {
                podcastChannelEpisodes.removeObservers(this);
                viewModel.setPodcastsCount((Integer) a.data);
            } else if (a.status == ApiResponse.Status.ERROR) {
                podcastChannelEpisodes.removeObservers(this);
                Log.e("ErrorResponse", String.format("API Error response from url %1$s: ", a.url), a.error);
            }
        });
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse);
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                setDataFromResponse(apiResponse);
                break;
            case ERROR:
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
        }
    }

    private void setDataFromResponse(@NonNull ApiResponse apiResponse) {
        if (apiResponse.data instanceof Boolean) {
            viewModel.setIsSubscribed((Boolean) apiResponse.data);
            loadPodcasts(true, (Boolean) apiResponse.data);
            setInfiniteScrollListener((Boolean) apiResponse.data);
        } else if (apiResponse.data instanceof PodcastChannel) {
            viewModel.setPodcastChannel((PodcastChannel) apiResponse.data);
            podcastChannel = (PodcastChannel) apiResponse.data;

            if (apiResponse.status == ApiResponse.Status.DATABASE) {
                viewModel.setIsSubscribed(podcastChannel.getIsSubscribed() == 1);
            }

            if (token != null && token.getValue() != null && new JWT(token.getValue()).getSubject().equals(podcastChannel.getUserId())) {
                viewModel.setIsMyPodcastChannel(true);
                loadPodcasts(true, true);
                setInfiniteScrollListener(true);
            } else {
                viewModel.setIsMyPodcastChannel(false);
                loadPodcasts(false, false);
                setInfiniteScrollListener(false);
            }
        } else if (apiResponse.data instanceof List) {
            viewModel.setIsLoading(false);
            if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                return;
            }
            viewModel.setPodcastsInAdapter((List<Object>) apiResponse.data);

            if (token != null) {
                getAccountPodcasts((List<Podcast>) apiResponse.data, false);
            }
            if (((List<Object>) apiResponse.data).size() >= 5) {
                addAds();
            }
        }
    }

    private void getAccountPodcasts(List<Podcast> podcasts, boolean isSwipedToRefresh) {
        List<String> podcastIds = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            podcastIds.add(podcast.getId());
        }

        LiveData<ApiResponse> accountPodcasts = viewModel.accountPodcasts(this, token.getValue(), podcastIds, isSwipedToRefresh);
        accountPodcasts.observe(getViewLifecycleOwner(), response -> consumeAccountPodcasts(response, accountPodcasts));
    }

    private void consumeAccountPodcasts(ApiResponse accountPodcastsResponse, LiveData<ApiResponse> liveData) {
        switch (accountPodcastsResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setAccountPodcastsData(accountPodcastsResponse);
                break;
            case SUCCESS: {
                liveData.removeObservers(this);
                setAccountPodcastsData(accountPodcastsResponse);
                break;
            }
            case ERROR: {
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(accountPodcastsResponse, getContext());
                break;
            }
        }
    }

    private void setAccountPodcastsData(ApiResponse accountPodcastsResponse) {
        if (!CollectionUtils.isEmpty((List<AccountPodcast>) accountPodcastsResponse.data)) {
            for (Object podcast : viewModel.getPodcasts()) {
                for (AccountPodcast accountPodcast : (List<AccountPodcast>) accountPodcastsResponse.data) {
                    if (accountPodcast.getPodcastId().equals(((Podcast) podcast).getId())) {
                        ((Podcast) podcast).setMarkAsPlayed(accountPodcast.getMarkAsPlayed() == 1);
                    }
                }
            }
        }
    }

    private void setInfiniteScrollListener(boolean shouldSavePodcasts) {
        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                if (!viewModel.getIsLoading()) {
                    ++page;
                    viewModel.setIsLoading(true);
                    loadPodcasts(false, shouldSavePodcasts);
                }
            }
        });
    }

    private void loadPodcasts(boolean isSwipedToRefresh, boolean shouldSavePodcasts) {
        podcastsResponse = viewModel.podcasts(this, null, null, podcastChannelId, isSwipedToRefresh, shouldSavePodcasts, page);
        podcastsResponse.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcastsResponse));
    }

    private void setPodcastClick() {
        viewModel.getSelectedPodcast().observe(getViewLifecycleOwner(), podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setAuthorClick() {
        viewModel.getSelectedAuthor().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedAuthor().get() != null) {
                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAuthor().get(), false));
                    viewModel.getSelectedAuthor().set(null);
                }
            }
        });
    }

    private void setChannelClick() {
        viewModel.getSelectedChannelId().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedChannelId().get() != null) {
                    fragmentNavigation.pushFragment(PodcastChannelFragment.newInstance(fragmentCount + 1, viewModel.getSelectedChannelId().get(), null, false));
                    viewModel.getSelectedChannelId().set(null);
                }
            }
        });
    }

    private void addAds() {
        adLoader = new AdLoader.Builder(getContext(), BuildConfig.PODCAST_FROM_PODCAST_CHANNEL_NATIVE_ADS)
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    Log.i(getTag(), "Native Ad In Account Podcasts Loaded");
                    if (!adLoader.isLoading()) {
                        viewModel.addElementInPodcastsAdapter(unifiedNativeAd, viewModel.getPodcastAdapter().getItemCount() / 2);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.e(getTag(), "Native Ad In Account Podcasts Failed to loaded: " + errorCode);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .build())
                .build();
        adLoader.loadAds(new AdRequest.Builder().build(), 1);
    }

}
