package com.podcasses.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastBinding;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;
import com.podcasses.viewmodel.PodcastViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastFragment extends BaseFragment implements Player.EventListener {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    private FragmentPodcastBinding binding;

    private PodcastViewModel viewModel;

    private static String id;

    private LiveData<ApiResponse> podcastResponse;
    private LiveData<ApiResponse> accountPodcastResponse;
    private LiveData<ApiResponse> commentsResponse;
    private LiveData<String> token;
    private Podcast podcast;
    private AccountPodcast accountPodcast;

    private Podcast playingPodcast;
    private IBinder binder;
    private AudioPlayerService service;

    private PopupMenu popupOptions;
    private MenuPopupHelper menuHelper;

    static PodcastFragment newInstance(int instance, String podcastId) {
        id = podcastId;
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
        token = isAuthenticated();

        binding.refreshLayout.setOnRefreshListener(refreshListener);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel.class);
        binding.setViewModel(viewModel);

        viewModel.setPodcastImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PODCAST_IMAGE + id);

        podcastResponse = viewModel.podcast(this, id, false);
        podcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastResponse, null));

        token.observe(this, s -> {
            if (!Strings.isEmptyOrWhitespace(s)) {
                JWT jwt = new JWT(s);
                accountPodcastResponse = viewModel.accountPodcasts(this, s, jwt.getSubject(), id, false);
                accountPodcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountPodcastResponse, null));
            }
        });

        commentsResponse = viewModel.comments(id);
        commentsResponse.observe(this, apiResponse -> consumeResponse(apiResponse, commentsResponse, null));

        popupOptions = new PopupMenu(getContext(), binding.optionsButton);
        popupOptions.getMenuInflater()
                .inflate(R.menu.podcast_options_menu, popupOptions.getMenu());
        menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupOptions.getMenu(), binding.optionsButton);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        binding.optionsButton.setOnClickListener(onOptionsClickListener);
        binding.likeButton.setOnClickListener(onLikeClickListener);
        binding.dislikeButton.setOnClickListener(onDislikeClickListener);

        service = ((AudioPlayerService.LocalBinder) binder).getService();
        SimpleExoPlayer player = service.getPlayerInstance();

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
        if (podcast != null && getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(podcast.getTitle());
    }

    private OnRefreshListener refreshListener = refreshLayout -> {
        podcastResponse = viewModel.podcast(this, id, true);
        podcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastResponse, refreshLayout));

        if (!Strings.isEmptyOrWhitespace(token.getValue())) {
            JWT jwt = new JWT(token.getValue());
            accountPodcastResponse = viewModel.accountPodcasts(this, token.getValue(), jwt.getSubject(), id, true);
            accountPodcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountPodcastResponse, refreshLayout));
        }
        commentsResponse.observe(this, apiResponse -> consumeResponse(apiResponse, commentsResponse, refreshLayout));
    };

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                if (apiResponse.data instanceof Podcast) {
                    podcast = (Podcast) apiResponse.data;
                    updateTitle();
                    viewModel.setPodcast(podcast);
                } else if (apiResponse.data instanceof AccountPodcast) {
                    accountPodcast = (AccountPodcast) apiResponse.data;
                    binding.likeButton.setSelected(accountPodcast.getLikeStatus() == AccountPodcast.LIKED);
                    binding.dislikeButton.setSelected(accountPodcast.getLikeStatus() == AccountPodcast.DISLIKED);
                    popupOptions.getMenu().getItem(0).setChecked(accountPodcast.getMarkAsPlayed() == 1);
                } else if (apiResponse.data instanceof List) {
                    if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                        return;
                    }
                    if (((List) apiResponse.data).get(0) instanceof Podcast) {
                        updateTitle();
                        podcast = ((List<Podcast>) apiResponse.data).get(0);
                        viewModel.setPodcast(podcast);
                    } else {
                        viewModel.setPodcastCommentsInAdapter((List<Comment>) apiResponse.data);
                    }
                }
                break;
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                logError(apiResponse);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playingPodcast = service.getPodcast();
        setPlayingStatus(playWhenReady);

        if (playbackState == Player.STATE_IDLE) {
            binding.playButton.change(true);
        }
    }

    private void setPlayingStatus(boolean playingStatus) {
        if (playingPodcast != null && playingPodcast.getId().equals(id)) {
            binding.playButton.change(!playingStatus);
        }
    }

    @SuppressLint("RestrictedApi")
    private View.OnClickListener onOptionsClickListener = view -> {
        popupOptions.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.markAsPlayed:
                    if (accountPodcast != null) {
                        sendMarkAsPlayedRequest(item);
                    }
                    break;
                case R.id.report:
                    break;
            }
            return true;
        });
        menuHelper.show();
    };

    private void sendMarkAsPlayedRequest(MenuItem item) {
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setPodcastId(podcast.getId());
        if (accountPodcast.getMarkAsPlayed() == 1) {
            accountPodcastRequest.setMarkAsPlayed(0);
        } else {
            accountPodcastRequest.setMarkAsPlayed(1);
        }
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token.getValue(), accountPodcastRequest);
        call.enqueue(new retrofit2.Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountPodcast = response.body();
                    item.setChecked(accountPodcast.getMarkAsPlayed() == 1);
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                Toasty.error(getContext(), getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private View.OnClickListener onLikeClickListener = v ->
            sendLikeDislikeRequest(AccountPodcast.LIKED, R.string.successfully_liked, R.string.successful_like_status_change);

    private View.OnClickListener onDislikeClickListener = v ->
            sendLikeDislikeRequest(AccountPodcast.DISLIKED, R.string.successful_dislike, R.string.successful_dislike_status_change);

    private void sendLikeDislikeRequest(int likeStatus, int successfulChangeMessage, int successfulDefaultMessage) {
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setPodcastId(podcast.getId());
        if (accountPodcast.getLikeStatus() == likeStatus) {
            accountPodcastRequest.setLikeStatus(AccountPodcast.DEFAULT);
        } else {
            accountPodcastRequest.setLikeStatus(likeStatus);
        }
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token.getValue(), accountPodcastRequest);
        call.enqueue(new retrofit2.Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountPodcast = response.body();
                    if (likeStatus == AccountPodcast.LIKED) {
                        binding.likeButton.setSelected(accountPodcast.getLikeStatus() == likeStatus);
                        binding.dislikeButton.setSelected(accountPodcast.getLikeStatus() != likeStatus &&
                                accountPodcast.getLikeStatus() != AccountPodcast.DEFAULT);
                    } else {
                        binding.dislikeButton.setSelected(accountPodcast.getLikeStatus() == likeStatus);
                        binding.likeButton.setSelected(accountPodcast.getLikeStatus() != likeStatus &&
                                accountPodcast.getLikeStatus() != AccountPodcast.DEFAULT);
                    }
                    if (accountPodcast.getLikeStatus() == likeStatus) {
                        Toasty.success(getContext(), getString(successfulChangeMessage), Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.success(getContext(), getString(successfulDefaultMessage), Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    Toasty.error(getContext(), getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                Toasty.error(getContext(), getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                Log.e(getTag(), "onFailure: ", t);
            }
        });
    }

}
