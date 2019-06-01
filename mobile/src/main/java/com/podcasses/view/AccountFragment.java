package com.podcasses.view;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.auth0.android.jwt.JWT;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountFragment extends BaseFragment implements Player.EventListener, OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    @Inject
    SharedPreferencesManager sharedPreferencesManager;

    private AccountViewModel viewModel;
    private FragmentAccountBinding binding;

    private LiveData<String> token;

    private LiveData<ApiResponse> accountResponse;
    private LiveData<ApiResponse> accountSubscribesResponse;
    private LiveData<ApiResponse> checkAccountSubscribeResponse;
    private LiveData<ApiResponse> podcasts;
    private LiveData<ApiResponse> podcastFiles;

    private String playingPodcastId;
    private IBinder binder;
    private AudioPlayerService service;
    private SimpleExoPlayer player;

    private Account account;
    private String username;
    private static String accountId;
    private static int instanceCount;

    static AccountFragment newInstance(int instance, String openedAccountId) {
        accountId = openedAccountId;
        instanceCount = instance;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        AccountFragment fragment = new AccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);
        binding.setViewModel(viewModel);
        binding.refreshLayout.setOnRefreshListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (accountId != null) {
            binding.setAccountId(accountId);
            viewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accountId);
            viewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + accountId);
            token = AuthenticationUtil.isAuthenticated(getContext(), this);
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    token.removeObservers(this);
                    binding.setToken(s);
                    JWT jwt = new JWT(s);
                    if (accountId.equals(jwt.getSubject())) {
                        binding.setIsMyAccount(true);
                        getAccountData(s, true, null);
                    } else {
                        getAccountData(s, false, null);
                    }
                }
            });
        } else {
            binding.setIsMyAccount(true);
            token = AuthenticationUtil.isAuthenticated(getContext(), this);
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    token.removeObservers(this);
                    JWT jwt = new JWT(s);
                    accountId = jwt.getSubject();
                    binding.setAccountId(accountId);
                    binding.setInstance(instanceCount);
                    binding.setFragmentManager(getChildFragmentManager());
                    username = jwt.getClaim(KeycloakToken.PREFERRED_USERNAME_CLAIMS).asString();
                    binding.setToken(s);

                    viewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accountId);
                    viewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + accountId);

                    getAccountData(s, true, null);
                }
            });
        }

        setPodcastClick();
        setAccountClick();
        setAccountEditClick();

        service = ((AudioPlayerService.LocalBinder) binder).getService();
        player = service.getPlayerInstance();

        if (player != null) {
            playingPodcastId = service.getPodcastId();
            player.addListener(this);
            setPlayingStatus(player.getPlayWhenReady());
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getAccountData(null, false, refreshLayout);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            FragmentCallback fragmentCallback = (FragmentCallback) context;
            binder = fragmentCallback.getBinder();
        } catch (ClassCastException e) {
            Log.e(getTag(), "Activity (Context) must implement FragmentCallback");
            throw new RuntimeException();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playingPodcastId = service.getPodcastId();
        setPlayingStatus(playWhenReady);

        if (playbackState == Player.STATE_IDLE) {
            viewModel.setPlayingIndex(-1);
        } else if (!sharedPreferencesManager.isPodcastViewed(playingPodcastId)) {
            NetworkRequestsUtil.sendPodcastViewRequest(apiCallInterface, sharedPreferencesManager, playingPodcastId);
        }
    }

    void updateTitle() {
        if (account != null && getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(account.getUsername());
    }

    private void getAccountData(String token, boolean isMyAccount, RefreshLayout refreshLayout) {
        accountSubscribesResponse = viewModel.accountSubscribes(accountId);
        podcasts = viewModel.podcasts(this, null, null, accountId, refreshLayout != null, true);

        if (isMyAccount) {
            accountResponse = viewModel.account(this, username, refreshLayout != null);
            podcastFiles = viewModel.podcastFiles(this, token != null ? token : this.token.getValue(), accountId, refreshLayout != null);
            podcastFiles.observe(this, apiResponse -> consumeResponse(apiResponse, podcastFiles, refreshLayout));
        } else {
            accountResponse = viewModel.account(accountId);
            checkAccountSubscribeResponse = viewModel.checkAccountSubscribe(token != null ? token : this.token.getValue(), accountId);
            checkAccountSubscribeResponse.observe(this, apiResponse -> consumeResponse(apiResponse, checkAccountSubscribeResponse, refreshLayout));
        }
        podcasts.observe(this, apiResponse -> consumeResponse(apiResponse, podcasts, refreshLayout));
        accountResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountResponse, refreshLayout));
        accountSubscribesResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountSubscribesResponse, refreshLayout));
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                if (apiResponse.data instanceof Account) {
                    account = (Account) apiResponse.data;
                    updateTitle();
                    viewModel.setAccount(account);
                } else if (apiResponse.data instanceof Integer) {
                    viewModel.setAccountSubscribes((Integer) apiResponse.data);
                } else if (apiResponse.data instanceof Boolean) {
                    viewModel.setIsSubscribed((Boolean) apiResponse.data);
                } else if (apiResponse.data instanceof List) {
                    if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                        binding.podcastFilesCardView.setVisibility(View.GONE);
                        return;
                    }
                    if (((List) apiResponse.data).get(0) instanceof Podcast) {
                        viewModel.setPodcastsInAdapter((List<Podcast>) apiResponse.data);
                        if (player != null) {
                            setPlayingStatus(player.getPlayWhenReady());
                        }
                        getAccountPodcasts((List<Podcast>) apiResponse.data);
                    } else {
                        viewModel.setPodcastFilesInAdapter((List<PodcastFile>) apiResponse.data);
                        binding.podcastFilesCardView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            case FETCHED:
                liveData.removeObservers(this);
                break;
        }
    }

    private void setPodcastClick() {
        viewModel.getSelectedPodcast().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setAccountClick() {
        viewModel.getSelectedAccount().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedAccount().get() != null) {
                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAccount().get()));
                    viewModel.getSelectedAccount().set(null);
                }
            }
        });
    }

    private void setAccountEditClick() {
        viewModel.getEditAccountId().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getEditAccountId().get() != null) {
                    fragmentNavigation.pushFragment(EditAccountFragment.newInstance(fragmentCount + 1, account));
                    viewModel.getEditAccountId().set(null);
                }
            }
        });
    }

    private void getAccountPodcasts(List<Podcast> podcasts) {
        List<String> podcastIds = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            podcastIds.add(podcast.getId());
        }

        LiveData<ApiResponse> accountPodcasts = viewModel.accountPodcasts(token.getValue(), podcastIds);
        accountPodcasts.observe(this, response -> consumeAccountPodcasts(response, accountPodcasts));
    }

    private void consumeAccountPodcasts(ApiResponse accountPodcastsResponse, LiveData<ApiResponse> liveData) {
        switch (accountPodcastsResponse.status) {
            case LOADING:
                break;
            case SUCCESS: {
                liveData.removeObservers(this);
                if (!CollectionUtils.isEmpty((List<AccountPodcast>) accountPodcastsResponse.data)) {
                    for (Podcast podcast : viewModel.getPodcasts()) {
                        for (AccountPodcast accountPodcast : (List<AccountPodcast>) accountPodcastsResponse.data) {
                            if (accountPodcast.getPodcastId().equals(podcast.getId())) {
                                podcast.setMarkAsPlayed(accountPodcast.getMarkAsPlayed() == 1);
                            }
                        }
                    }
                }
                break;
            }
            case ERROR: {
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(accountPodcastsResponse, getContext());
                break;
            }
        }
    }

    private void setPlayingStatus(boolean playingStatus) {
        if (!playingStatus) {
            viewModel.setPlayingIndex(-1);
            return;
        }
        if (playingPodcastId != null && !CollectionUtils.isEmpty(viewModel.getPodcasts())) {
            int i;
            for (i = 0; i < viewModel.getPodcasts().size(); i++) {
                if (playingPodcastId.equals(viewModel.getPodcasts().get(i).getId())) {
                    break;
                }
            }
            viewModel.setPlayingIndex(i);
        }
    }

}
