package com.podcasses.viewmodel;

import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.ohoussein.playpause.PlayPauseView;
import com.podcasses.BR;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastCommentAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.CommentRequest;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.service.AudioDownloadService;
import com.podcasses.util.LikeStatus;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.util.PopupMenuUtil;
import com.podcasses.viewmodel.base.BaseViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.podcasses.util.CustomViewBindings.PROFILE_IMAGE;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    private MutableLiveData<Podcast> podcast = new MutableLiveData<>();
    private ObservableField<String> podcastImage = new ObservableField<>();
    private MutableLiveData<List<Comment>> comments = new MutableLiveData<>();
    private ObservableField<String> accountId = new ObservableField<>();
    private ObservableField<String> selectedAccountId = new ObservableField<>();

    private PodcastCommentAdapter podcastCommentsAdapter = new PodcastCommentAdapter(R.layout.item_comment, this);

    private String token;

    private ApiCallInterface apiCallInterface;

    PodcastViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public LiveData<ApiResponse> podcast(@NonNull LifecycleOwner lifecycleOwner, @NonNull String podcastId, boolean isSwipedToRefresh) {
        return repository.getPodcasts(lifecycleOwner, null, podcastId, null, isSwipedToRefresh, false);
    }

    public LiveData<ApiResponse> accountPodcasts(@NonNull LifecycleOwner lifecycleOwner, @NonNull String token, String accountId, @NonNull String podcastId, boolean isSwipedToRefresh) {
        this.token = token;
        return repository.getAccountPodcasts(lifecycleOwner, token, accountId, podcastId, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> comments(@NonNull String podcastId) {
        return repository.getComments(podcastId);
    }

    public LiveData<ApiResponse> accountComments(@NonNull String token, @NonNull List<String> commentIds) {
        this.token = token;
        return repository.getAccountComments(token, commentIds);
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    @Bindable
    public Podcast getPodcast() {
        return podcast.getValue();
    }

    @Bindable
    public String getPodcastImage() {
        return podcastImage.get();
    }

    public ObservableField<String> getSelectedAccountId() {
        return selectedAccountId;
    }

    @Bindable
    public String getAccountId() {
        if (accountId.get() != null) {
            return BuildConfig.API_GATEWAY_URL.concat(PROFILE_IMAGE).concat(accountId.get());
        }
        return null;
    }

    public PodcastCommentAdapter getPodcastCommentsAdapter() {
        return podcastCommentsAdapter;
    }

    public void setPodcastCommentsInAdapter(List<Comment> comments) {
        this.comments.setValue(comments);
        this.podcastCommentsAdapter.setComments(comments);
        this.podcastCommentsAdapter.notifyDataSetChanged();
    }

    public List<Comment> getComments() {
        return comments.getValue();
    }

    public Comment getCommentAt(Integer index) {
        if (comments.getValue() != null && index != null && comments.getValue().size() > index) {
            return comments.getValue().get(index);
        }
        return null;
    }

    public String accountImage(Integer position) {
        if (!CollectionUtils.isEmpty(comments.getValue())) {
            Comment comment = comments.getValue().get(position);
            return BuildConfig.API_GATEWAY_URL.concat(PROFILE_IMAGE).concat(comment.getUserId());
        }
        return null;
    }

    public void onPlayPauseButtonClick(View view) {
        ((PlayPauseView) view).toggle();
        EventBus.getDefault().post(podcast.getValue());
    }

    public void onDownloadButtonClick(View view) {
        ProgressiveDownloadAction progressiveDownloadAction = ProgressiveDownloadAction.createDownloadAction(
                Uri.parse(BuildConfig.API_GATEWAY_URL.concat("/podcast/download/").concat(podcast.getValue().getId())),
                null, null);
        DownloadService.startWithAction(view.getContext(), AudioDownloadService.class, progressiveDownloadAction, false);
    }

    public void onOptionsButtonClick(View view) {
        PopupMenuUtil.podcastPopupMenu(view, podcast.getValue(), apiCallInterface, token);
    }

    public void setPodcast(Podcast podcast) {
        this.podcast.setValue(podcast);
        notifyPropertyChanged(BR.podcast);
    }

    public void setPodcastImage(String podcastImage) {
        this.podcastImage.set(podcastImage);
        notifyPropertyChanged(BR.podcastImage);
    }

    public void setAccountId(String accountId) {
        this.accountId.set(accountId);
        notifyPropertyChanged(BR.accountId);
    }

    public void addComment(View view, String podcastId, TextInputEditText comment) {
        if (comment.getText() == null || Strings.isEmptyOrWhitespace(comment.getText().toString()) || token == null) {
            return;
        }

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setComment(comment.getText().toString());
        commentRequest.setPodcastId(podcastId);
        Call<Comment> commentCall = apiCallInterface.accountComment("Bearer " + token, commentRequest);
        commentCall.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    Toasty.success(view.getContext(), view.getContext().getString(R.string.successfully_added_comment), Toast.LENGTH_SHORT, true).show();
                    comment.setText("");
                    List<Comment> commentsList = comments.getValue();
                    if (CollectionUtils.isEmpty(commentsList)) {
                        commentsList = new ArrayList<>();
                    }
                    commentsList.add(0, response.body());
                    setPodcastCommentsInAdapter(commentsList);
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, view.getContext());
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, view.getContext());
            }
        });
    }

    public void likeComment(View view, int position) {
        if (!Strings.isEmptyOrWhitespace(token)) {
            Comment comment = getCommentAt(position);
            AccountCommentRequest accountCommentRequest = new AccountCommentRequest();
            accountCommentRequest.setCommentId(comment.getId());
            if (comment.isLiked()) {
                accountCommentRequest.setLikeStatus(LikeStatus.DEFAULT.getValue());
            } else {
                accountCommentRequest.setLikeStatus(LikeStatus.LIKE.getValue());
            }
            NetworkRequestsUtil.sendAccountCommentRequest(view.getContext(), apiCallInterface, token, comment, accountCommentRequest);
        }
    }

    public void dislikeComment(View view, int position) {
        if (!Strings.isEmptyOrWhitespace(token)) {
            Comment comment = getCommentAt(position);
            AccountCommentRequest accountCommentRequest = new AccountCommentRequest();
            accountCommentRequest.setCommentId(comment.getId());
            if (comment.isDisliked()) {
                accountCommentRequest.setLikeStatus(LikeStatus.DEFAULT.getValue());
            } else {
                accountCommentRequest.setLikeStatus(LikeStatus.DISLIKE.getValue());
            }
            NetworkRequestsUtil.sendAccountCommentRequest(view.getContext(), apiCallInterface, token, comment, accountCommentRequest);
        }
    }

    public void commentOptions(View view, int position) {
        PopupMenuUtil.commentPopupMenu(view, comments.getValue().get(position), apiCallInterface, token);
    }

    public void openAccount(Integer position) {
        selectedAccountId.set(comments.getValue().get(position).getUserId());
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
