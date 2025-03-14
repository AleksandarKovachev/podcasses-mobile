package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.podcasses.BR;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastCommentAdapter;
import com.podcasses.constant.LikeStatus;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.manager.DownloadTracker;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.CommentRequest;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Comment;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
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
public class PodcastViewModel extends BaseViewModel {

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
        return repository.getPodcasts(lifecycleOwner, null, podcastId, null, false, isSwipedToRefresh, 0);
    }

    public LiveData<ApiResponse> accountPodcasts(@NonNull LifecycleOwner lifecycleOwner, String token, @NonNull String podcastId) {
        this.token = token;
        return repository.getAccountPodcast(lifecycleOwner, token, podcastId);
    }

    public LiveData<ApiResponse> comments(@NonNull String podcastId) {
        return repository.getComments(podcastId);
    }

    public LiveData<ApiResponse> accountComments(@NonNull String token, @NonNull List<String> commentIds) {
        this.token = token;
        return repository.getAccountComments(token, commentIds);
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
        view.setSelected(!view.isSelected());
        EventBus.getDefault().post(podcast.getValue());
    }

    public void onDownloadButtonClick(View view) {
        DownloadTracker downloadTracker = ((BaseApplication) view.getContext().getApplicationContext()).getDownloadTracker();
        downloadTracker.toggleDownload(repository, podcast.getValue());
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

}
