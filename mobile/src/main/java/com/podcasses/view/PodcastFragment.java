package com.podcasses.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import com.podcasses.util.DialogUtil;
import com.podcasses.util.LikeStatusUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;
import com.podcasses.viewmodel.PodcastViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        token = AuthenticationUtil.isAuthenticated(getContext(), this);
        viewModel.setPodcastImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PODCAST_IMAGE + id);

        if (podcast != null) {
            viewModel.setPodcast(podcast);
        } else {
            podcastResponse = viewModel.podcast(this, id, false);
            podcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastResponse));
        }
        setAccountPodcast(null);

        token.observe(this, s -> {
            if (!Strings.isEmptyOrWhitespace(s)) {
                JWT jwt = new JWT(s);
                viewModel.setAccountId(jwt.getSubject());
                setAccountPodcast(s);
            }
        });

        commentsResponse = viewModel.comments(id);
        commentsResponse.observe(this, apiResponse -> consumeResponse(apiResponse, commentsResponse));

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.podcast_options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_share:
                Uri bmpUri = getLocalBitmapUri(binding.podcastImage);
                if (bmpUri != null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_SUBJECT, podcast.getTitle());
                    intent.putExtra(Intent.EXTRA_TEXT, podcast.getTitle() + "\n\n" + podcast.getDescription());
                    intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    startActivity(Intent.createChooser(intent, getString(R.string.share_text)));
                } else {
                    Toasty.error(getContext(), getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.navigation_mark_as_played:
                NetworkRequestsUtil.sendMarkAsPlayedRequest(item, getContext(), apiCallInterface, podcast, token.getValue());
                break;
            case R.id.navigation_report:
                DialogUtil.createReportDialog(getContext(), podcast.getId(), apiCallInterface, token.getValue(), true);
                break;
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            binder = ((FragmentCallback) context).getBinder();
        } catch (ClassCastException e) {
            Log.e(getTag(), "Activity (Context) must implement FragmentCallback");
            throw new RuntimeException();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playingPodcastId = service.getPodcastId();
        setPlayingStatus(playWhenReady);

        if (playbackState == Player.STATE_READY) {
            if (accountPodcast == null || accountPodcast.getViewTimestamp() == null) {
                LiveData<ApiResponse> accountPodcastResponse =
                        NetworkRequestsUtil.sendPodcastViewRequest(getContext(), apiCallInterface, token.getValue(), id);
                accountPodcastResponse.observe(this, response -> {
                    switch (response.status) {
                        case SUCCESS:
                            accountPodcastResponse.removeObservers(this);
                            accountPodcast = (AccountPodcast) response.data;
                            viewModel.saveAccountPodcast(accountPodcast);
                            break;
                        case ERROR:
                            accountPodcastResponse.removeObservers(this);
                            break;
                        default:
                            break;
                    }
                });
            }
        }
    }

    private void setAccountPodcast(String token) {
        accountPodcastResponse = viewModel.accountPodcasts(this, token, id);
        accountPodcastResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountPodcastResponse));
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
        if (apiResponse.data instanceof Podcast) {
            podcast = (Podcast) apiResponse.data;
            viewModel.setPodcast(podcast);
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
                viewModel.setPodcast(podcast);
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
                            likeStatus, accountPodcast != null ? accountPodcast.getLikeStatus() : LikeStatus.DEFAULT.getValue());
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

    private Uri getLocalBitmapUri(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        Uri bmpUri = null;
        try {
            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            if (Build.VERSION.SDK_INT >= 24) {
                bmpUri = FileProvider.getUriForFile(getContext(), "com.podcasses", file);
            } else {
                bmpUri = Uri.fromFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


}
