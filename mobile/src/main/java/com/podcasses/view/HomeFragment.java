package com.podcasses.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.constant.PodcastType;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentHomeBinding;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.request.TrendingReport;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.HomeViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class HomeFragment extends BaseFragment implements OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    private FragmentHomeBinding binder;

    private HomeViewModel viewModel;
    private LiveData<ApiResponse> trendingPodcasts;

    private AdLoader adLoader;

    static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel.class);
        viewModel.setLifecycleOwner(this);
        viewModel.setFragmentManager(getParentFragmentManager());
        binder.setViewModel(viewModel);
        binder.setLifecycleOwner(this);
        binder.setFragmentManager(getChildFragmentManager());
        binder.refreshLayout.setOnRefreshListener(this);
        binder.setTypes(Arrays.asList(PodcastType.FROM_SUBSCRIPTIONS.getType(),
                PodcastType.IN_PROGRESS.getType(),
                PodcastType.DOWNLOADED.getType()));
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getData(null);
        setListClick();
        setChannelIdClick();
        setPodcastChannelClick();
        setTrendingFilterChange();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getData(refreshLayout);
    }

    private void getData(RefreshLayout refreshLayout) {
        LiveData<String> token = AuthenticationUtil.getAuthenticationToken(getContext());
        if (token != null) {
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    binder.userHomeCard.setVisibility(View.VISIBLE);
                    getSubscribedPodcastChannels(s);
                }
            });
        } else {
            getSubscribedPodcastChannels(null);
        }

        getNewPodcastChannels();
        getNewPodcasts();

        TrendingFilter trendingFilter = new TrendingFilter(TrendingReport.WEEKLY, null, null, null, null);
        getTrendingPodcasts(refreshLayout, trendingFilter);
    }

    private void getSubscribedPodcastChannels(String token) {
        LiveData<ApiResponse> podcastChannels = viewModel.getSubscribedPodcastChannels(token);
        podcastChannels.observe(this, response -> consumeApiResponse(response, podcastChannels));
    }

    private void getNewPodcastChannels() {
        LiveData<ApiResponse> newPodcasts = viewModel.podcastChannels();
        newPodcasts.observe(this, apiResponse -> {
            if (apiResponse.status == ApiResponse.Status.SUCCESS) {
                newPodcasts.removeObservers(this);
                viewModel.setNewPodcastChannelsInAdapter((List<PodcastChannel>) apiResponse.data);
            } else if (apiResponse.status == ApiResponse.Status.ERROR) {
                newPodcasts.removeObservers(this);
                Log.e(getTag(), "getNewPodcastChannels: ", apiResponse.error);
            }
        });
    }

    private void getNewPodcasts() {
        LiveData<ApiResponse> newPodcasts = viewModel.newPodcasts();
        newPodcasts.observe(this, apiResponse -> {
            if (apiResponse.status == ApiResponse.Status.SUCCESS) {
                newPodcasts.removeObservers(this);
                viewModel.setNewPodcastsInAdapter((List<Object>) apiResponse.data);
            } else if (apiResponse.status == ApiResponse.Status.ERROR) {
                newPodcasts.removeObservers(this);
                Log.e(getTag(), "getNewPodcasts: ", apiResponse.error);
            }
        });
    }

    private void getTrendingPodcasts(RefreshLayout refreshLayout, TrendingFilter trendingFilter) {
        trendingPodcasts = viewModel.trendingPodcasts(this, trendingFilter, refreshLayout != null);
        trendingPodcasts.observe(this, apiResponse -> consumeResponse(apiResponse, trendingPodcasts, refreshLayout));
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse, refreshLayout != null);
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                setDataFromResponse(apiResponse, refreshLayout != null);
                break;
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
        }
    }

    private void setDataFromResponse(@NonNull ApiResponse apiResponse, boolean isSwipedToRefresh) {
        if (apiResponse.data instanceof List) {
            viewModel.setTrendingPodcastsInAdapter((List<Object>) apiResponse.data);
            if (((List<Object>) apiResponse.data).size() > 5) {
                addAds();
            }
            getAccountPodcasts((List<Podcast>) apiResponse.data, isSwipedToRefresh);
        }
    }

    private void getAccountPodcasts(List<Podcast> podcasts, boolean isSwipedToRefresh) {
        List<String> podcastIds = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            podcastIds.add(podcast.getId());
        }

        LiveData<String> token = AuthenticationUtil.getAuthenticationToken(getContext());
        if (token == null) {
            LiveData<ApiResponse> accountPodcasts = viewModel.accountPodcasts(this, null, podcastIds, isSwipedToRefresh);
            accountPodcasts.observe(this, response -> consumeApiResponse(response, accountPodcasts));
        } else {
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    LiveData<ApiResponse> accountPodcasts = viewModel.accountPodcasts(this, s, podcastIds, isSwipedToRefresh);
                    accountPodcasts.observe(this, response -> consumeApiResponse(response, accountPodcasts));
                }
            });
        }
    }

    private void consumeApiResponse(ApiResponse apiResponse, LiveData<ApiResponse> liveData) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse);
                break;
            case SUCCESS: {
                liveData.removeObservers(this);
                setDataFromResponse(apiResponse);
                break;
            }
            case ERROR: {
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            }
        }
    }

    private void setDataFromResponse(ApiResponse apiResponse) {
        if (CollectionUtils.isEmpty((List<?>) apiResponse.data)) {
            return;
        }

        if (((List<?>) apiResponse.data).get(0) instanceof PodcastChannel) {
            binder.subscribedPodcastChannelsHeader.setVisibility(View.VISIBLE);
            viewModel.setPodcastChannelsInAdapter((List<PodcastChannel>) apiResponse.data);
            return;
        }

        for (Object podcast : viewModel.getPodcasts()) {
            for (AccountPodcast accountPodcast : (List<AccountPodcast>) apiResponse.data) {
                if (accountPodcast.getPodcastId().equals(((Podcast) podcast).getId())) {
                    ((Podcast) podcast).setMarkAsPlayed(accountPodcast.getMarkAsPlayed() == 1);
                }
            }
        }
    }

    private void setListClick() {
        viewModel.getSelectedPodcast().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setChannelIdClick() {
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

    private void setPodcastChannelClick() {
        viewModel.getSelectedPodcastChannel().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedPodcastChannel().get() != null) {
                    fragmentNavigation.pushFragment(PodcastChannelFragment.newInstance(fragmentCount + 1,
                            viewModel.getSelectedPodcastChannel().get().getId(), viewModel.getSelectedPodcastChannel().get(), true));
                    viewModel.getSelectedPodcastChannel().set(null);
                }
            }
        });
    }

    private void setTrendingFilterChange() {
        viewModel.getTrendingFilterMutableLiveData().observe(this, trendingFilter ->
                getTrendingPodcasts(null, trendingFilter)
        );
    }

    private void addAds() {
        adLoader = new AdLoader.Builder(getContext(), BuildConfig.TRENDING_NATIVE_ADS)
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    Log.i(getTag(), "Native Ad In Trending Podcasts Loaded");
                    if (!adLoader.isLoading()) {
                        viewModel.addElementInTrendingPodcastsAdapter(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.e(getTag(), "Native Ad In Trending Podcasts Failed to loaded: " + errorCode);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .build())
                .build();
        adLoader.loadAds(new AdRequest.Builder().build(), 1);
    }

}
