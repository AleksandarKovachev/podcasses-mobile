package com.podcasses.view;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountFragment extends BaseFragment implements Player.EventListener {

    @Inject
    ViewModelFactory viewModelFactory;

    private AccountViewModel accountViewModel;
    private FragmentAccountBinding binding;

    private LiveData<String> token;

    private LiveData<ApiResponse> accountResponse;
    private LiveData<ApiResponse> accountSubscribesResponse;
    private LiveData<ApiResponse> checkAccountSubscribeResponse;
    private LiveData<ApiResponse> podcasts;
    private LiveData<ApiResponse> podcastFiles;

    private Podcast playingPodcast;
    private IBinder binder;
    private AudioPlayerService service;
    private SimpleExoPlayer player;

    private Account account;
    private String username;
    private String accountId;

    static AccountFragment newInstance(int instance) {
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

        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);
        binding.setViewModel(accountViewModel);

        binding.refreshLayout.setOnRefreshListener(r -> getAccountData(null, r));

        token = isAuthenticated();
        token.observe(this, s -> {
            if (!Strings.isEmptyOrWhitespace(s)) {
                JWT jwt = new JWT(s);
                username = jwt.getClaim(KeycloakToken.PREFERRED_USERNAME_CLAIMS).asString();
                accountId = jwt.getSubject();
                binding.setAccountId(accountId);
                binding.setToken(s);

                accountViewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accountId);
                accountViewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + accountId);

                getAccountData(s, null);
            }
        });

        setListClick();

        service = ((AudioPlayerService.LocalBinder) binder).getService();
        player = service.getPlayerInstance();

        if (player != null) {
            playingPodcast = service.getPodcast();
            player.addListener(this);
            setPlayingStatus(player.getPlayWhenReady());
        }

        return binding.getRoot();
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

    void updateTitle() {
        if (account != null && getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(account.getUsername());
    }

    private void getAccountData(String token, RefreshLayout refreshLayout) {
        accountResponse = accountViewModel.account(this, username, refreshLayout != null);
        accountSubscribesResponse = accountViewModel.accountSubscribes(accountId);
        checkAccountSubscribeResponse = accountViewModel.checkAccountSubscribe(token != null ? token : this.token.getValue(), accountId);
        podcastFiles = accountViewModel.podcastFiles(this, token != null ? token : this.token.getValue(), accountId, refreshLayout != null);
        podcasts = accountViewModel.podcasts(this, accountId, refreshLayout != null, true);

        podcasts.observe(this, apiResponse -> consumeResponse(apiResponse, podcasts, refreshLayout));
        podcastFiles.observe(this, apiResponse -> consumeResponse(apiResponse, podcastFiles, refreshLayout));
        accountResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountResponse, refreshLayout));
        accountSubscribesResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountSubscribesResponse, refreshLayout));
        checkAccountSubscribeResponse.observe(this, apiResponse -> consumeResponse(apiResponse, checkAccountSubscribeResponse, refreshLayout));
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
                    accountViewModel.setAccount(account);
                } else if (apiResponse.data instanceof Integer) {
                    accountViewModel.setAccountSubscribes(String.format(getString(R.string.subscribe), apiResponse.data));
                } else if (apiResponse.data instanceof Boolean) {
                    accountViewModel.setIsSubscribed((Boolean) apiResponse.data);
                } else if (apiResponse.data instanceof List) {
                    if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                        binding.podcastFilesCardView.setVisibility(View.GONE);
                        return;
                    }
                    if (((List) apiResponse.data).get(0) instanceof Podcast) {
                        accountViewModel.setPodcastsInAdapter((List<Podcast>) apiResponse.data);
                        if (player != null) {
                            setPlayingStatus(player.getPlayWhenReady());
                        }
                    } else {
                        accountViewModel.setPodcastFilesInAdapter((List<PodcastFile>) apiResponse.data);
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
            default:
                break;
        }
    }

    private void setListClick() {
        accountViewModel.getSelected().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId()));
                accountViewModel.getSelected().setValue(null);
            }
        });
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playingPodcast = service.getPodcast();
        setPlayingStatus(playWhenReady);

        if (playbackState == Player.STATE_IDLE) {
            accountViewModel.setPlayingIndex(-1);
        }
    }

    private void setPlayingStatus(boolean playingStatus) {
        if (!playingStatus) {
            accountViewModel.setPlayingIndex(-1);
            return;
        }
        if (playingPodcast != null && !CollectionUtils.isEmpty(accountViewModel.getPodcasts())) {
            int i;
            for (i = 0; i < accountViewModel.getPodcasts().size(); i++) {
                if (playingPodcast.getId().equals(accountViewModel.getPodcasts().get(i).getId())) {
                    break;
                }
            }
            accountViewModel.setPlayingIndex(i);
        }
    }


}
