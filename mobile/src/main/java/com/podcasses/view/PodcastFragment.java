package com.podcasses.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.podcasses.constant.LikeStatus;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastBinding;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.LikeStatusUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;
import com.podcasses.viewmodel.PodcastViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastFragment extends BaseFragment implements Player.EventListener, OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    private SharedPreferencesManager sharedPreferencesManager;

    private FragmentPodcastBinding binding;

    private PodcastViewModel viewModel;

    private static String id;

    private LiveData<ApiResponse> podcastResponse;
    private LiveData<ApiResponse> accountPodcastResponse;
    private LiveData<ApiResponse> commentsResponse;
    private LiveData<ApiResponse> accountCommentsResponse;
    private LiveData<String> token;
    private static Podcast podcast;
    private AccountPodcast accountPodcast;

    private String playingPodcastId;
    private IBinder binder;
    private AudioPlayerService service;

    static PodcastFragment newInstance(int instance, String podcastId, Podcast openedPodcast) {
        id = podcastId;
        podcast = openedPodcast;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        PodcastFragment fragment = new PodcastFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel.class);
        binding.setViewModel(viewModel);
        binding.setPodcastId(id);
        binding.refreshLayout.setOnRefreshListener(this);
        sharedPreferencesManager = ((BaseApplication) getContext().getApplicationContext()).getSharedPreferencesManager();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        token = AuthenticationUtil.isAuthenticated(getContext(), this);
        viewModel.setPodcastImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PODCAST_IMAGE + id);

        if (podcast != null) {
            setPodcastData();
        } else {
            podcastResponse = viewModel.podcast(this, id, false);
            podcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastResponse, null));
        }
        setAccountPodcast(null, false, null);

        token.observe(this, s -> {
            if (!Strings.isEmptyOrWhitespace(s)) {
                JWT jwt = new JWT(s);
                viewModel.setAccountId(jwt.getSubject());
                setAccountPodcast(s, false, null);
            }
        });

        commentsResponse = viewModel.comments(id);
        commentsResponse.observe(this, apiResponse -> consumeResponse(apiResponse, commentsResponse, null));

        binding.likeButton.setOnClickListener(onLikeClickListener);
        binding.dislikeButton.setOnClickListener(onDislikeClickListener);

        service = ((AudioPlayerService.LocalBinder) binder).getService();
        SimpleExoPlayer player = service.getPlayerInstance();

        if (player != null) {
            playingPodcastId = service.getPodcastId();
            player.addListener(this);
            setPlayingStatus(player.getPlayWhenReady());
        }

        setAccountClickListener();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        podcastResponse = viewModel.podcast(this, id, true);
        podcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastResponse, refreshLayout));
        setAccountPodcast(token.getValue(), true, refreshLayout);
        commentsResponse.observe(this, apiResponse -> consumeResponse(apiResponse, commentsResponse, refreshLayout));
    }

    private void setAccountPodcast(String token, boolean isSwipedToRefresh, RefreshLayout refreshLayout) {
        accountPodcastResponse = viewModel.accountPodcasts(this, token, id, isSwipedToRefresh);
        accountPodcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountPodcastResponse, refreshLayout));
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
            binding.playButton.setSelected(false);
        } else if (!sharedPreferencesManager.isPodcastViewed(id)) {
            NetworkRequestsUtil.sendPodcastViewRequest(apiCallInterface, sharedPreferencesManager, id);
        }
    }

    void updateTitle() {
        if (podcast != null && getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(podcast.getTitle());
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse);
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                setDataFromResponse(apiResponse);
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

    private void setDataFromResponse(@NonNull ApiResponse apiResponse) {
        if (apiResponse.data instanceof Podcast) {
            podcast = (Podcast) apiResponse.data;
            setPodcastData();
        } else if (apiResponse.data instanceof AccountPodcast) {
            accountPodcast = (AccountPodcast) apiResponse.data;
            binding.likeButton.setSelected(accountPodcast.getLikeStatus() == LikeStatus.LIKE.getValue());
            binding.dislikeButton.setSelected(accountPodcast.getLikeStatus() == LikeStatus.DISLIKE.getValue());
            if (podcast != null) {
                podcast.setMarkAsPlayed(accountPodcast.getMarkAsPlayed() == 1);
            }
        } else if (apiResponse.data instanceof List) {
            if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                return;
            }
            if (((List) apiResponse.data).get(0) instanceof Podcast) {
                podcast = ((List<Podcast>) apiResponse.data).get(0);
                setPodcastData();
            } else {
                viewModel.setPodcastCommentsInAdapter((List<Comment>) apiResponse.data);
                setAccountComments((List<Comment>) apiResponse.data);
            }
        }
    }

    private void setAccountComments(List<Comment> comments) {
        if (!Strings.isEmptyOrWhitespace(token.getValue())) {
            List<String> commentIds = new ArrayList<>();
            for (Comment comment : comments) {
                commentIds.add(comment.getId());
            }

            accountCommentsResponse = viewModel.accountComments(token.getValue(), commentIds);
            accountCommentsResponse.observe(this, accountComments -> consumeAccountCommentsResponse(accountComments, accountCommentsResponse));
        }
    }

    private void consumeAccountCommentsResponse(ApiResponse apiResponse, LiveData liveData) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (!CollectionUtils.isEmpty(viewModel.getComments()) && !CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                    for (Comment comment : viewModel.getComments()) {
                        for (AccountComment accountComment : (List<AccountComment>) apiResponse.data) {
                            if (comment.getId().equals(accountComment.getCommentId())) {
                                comment.setLiked(accountComment.getLikeStatus() == LikeStatus.LIKE.getValue());
                                comment.setDisliked(accountComment.getLikeStatus() == LikeStatus.DISLIKE.getValue());
                                break;
                            }
                        }
                    }
                }
                break;
            case ERROR:
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
        }
    }

    private void setPlayingStatus(boolean playingStatus) {
        if (playingPodcastId != null && playingPodcastId.equals(id)) {
            binding.playButton.setSelected(playingStatus);
        }
    }

    private View.OnClickListener onLikeClickListener = v ->
            sendLikeDislikeRequest(LikeStatus.LIKE.getValue(), R.string.successfully_liked, R.string.successful_like_status_change);

    private View.OnClickListener onDislikeClickListener = v ->
            sendLikeDislikeRequest(LikeStatus.DISLIKE.getValue(), R.string.successful_dislike, R.string.successful_dislike_status_change);

    private void sendLikeDislikeRequest(int likeStatus, int successfulChangeMessage, int successfulDefaultMessage) {
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setPodcastId(podcast.getId());
        if (accountPodcast != null && accountPodcast.getLikeStatus() == likeStatus) {
            accountPodcastRequest.setLikeStatus(LikeStatus.DEFAULT.getValue());
        } else {
            accountPodcastRequest.setLikeStatus(likeStatus);
        }
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token.getValue(), accountPodcastRequest);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LikeStatusUtil.updateLikeStatus(viewModel.getPodcast(),
                            likeStatus, accountPodcast != null ? accountPodcast.getLikeStatus() : likeStatus);
                    accountPodcast = response.body();
                    if (likeStatus == LikeStatus.LIKE.getValue()) {
                        binding.likeButton.setSelected(accountPodcast.getLikeStatus() == likeStatus);
                        binding.dislikeButton.setSelected(accountPodcast.getLikeStatus() != likeStatus &&
                                accountPodcast.getLikeStatus() != LikeStatus.DEFAULT.getValue());
                    } else {
                        binding.dislikeButton.setSelected(accountPodcast.getLikeStatus() == likeStatus);
                        binding.likeButton.setSelected(accountPodcast.getLikeStatus() != likeStatus &&
                                accountPodcast.getLikeStatus() != LikeStatus.DEFAULT.getValue());
                    }
                    if (accountPodcast.getLikeStatus() == likeStatus) {
                        Toasty.success(getContext(), getString(successfulChangeMessage), Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.success(getContext(), getString(successfulDefaultMessage), Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, getContext());
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, getContext());
            }
        });
    }

    private void setAccountClickListener() {
        viewModel.getSelectedAccountId().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedAccountId().get() != null) {
                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAccountId().get()));
                    viewModel.getSelectedAccountId().set(null);
                }
            }
        });
    }

    private void setPodcastData() {
        updateTitle();
        viewModel.setPodcast(podcast);
    }

}
