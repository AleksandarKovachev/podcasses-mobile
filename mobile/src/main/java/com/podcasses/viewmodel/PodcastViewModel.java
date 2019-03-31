package com.podcasses.viewmodel;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.ohoussein.playpause.PlayPauseView;
import com.podcasses.BR;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastCommentAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.service.AudioDownloadService;
import com.podcasses.util.LikeStatus;
import com.podcasses.viewmodel.base.BaseViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
        return repository.getAccountPodcasts(lifecycleOwner, token, accountId, podcastId, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> comments(@NonNull String podcastId) {
        return repository.getComments(podcastId);
    }

    public LiveData<ApiResponse> accounts(@NonNull List<String> ids) {
        return repository.getAccount(ids);
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

    public void setPodcast(Podcast podcast) {
        this.podcast.setValue(podcast);
        notifyPropertyChanged(BR.podcast);
    }

    public void setPodcastImage(String podcastImage) {
        this.podcastImage.set(podcastImage);
        notifyPropertyChanged(BR.podcastImage);
    }

    @BindingAdapter(value = {"isLiked"})
    public static void isLiked(View view, boolean isLiked) {
        view.setSelected(isLiked);
    }

    @BindingAdapter(value = {"isDisliked"})
    public static void isDisliked(View view, boolean isDisliked) {
        view.setSelected(isDisliked);
    }

    public void likeComment(View view, int position) {
        if (!Strings.isEmptyOrWhitespace(token)) {
            Comment comment = getCommentAt(position);
            AccountCommentRequest accountCommentRequest = new AccountCommentRequest();
            accountCommentRequest.setCommentId(comment.getId());
            if (comment.isLiked()) {
                accountCommentRequest.setLikeStatus(LikeStatus.DEFAULT.getValue());
            } else {
                accountCommentRequest.setLikeStatus(LikeStatus.LIKED.getValue());
            }
            sendAccountCommentRequest(view, comment, accountCommentRequest);
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
                accountCommentRequest.setLikeStatus(LikeStatus.DISLIKED.getValue());
            }
            sendAccountCommentRequest(view, comment, accountCommentRequest);
        }
    }

    private void sendAccountCommentRequest(View view, Comment comment, AccountCommentRequest accountCommentRequest) {
        Call<AccountComment> call = apiCallInterface.accountComment("Bearer " + token, accountCommentRequest);
        call.enqueue(new Callback<AccountComment>() {
            @Override
            public void onResponse(Call<AccountComment> call, Response<AccountComment> response) {
                if (response.isSuccessful()) {
                    Toasty.success(view.getContext(), view.getContext().getString(R.string.successful_response), Toast.LENGTH_SHORT, true).show();
                    comment.setLiked(response.body().getLikeStatus() == LikeStatus.LIKED.getValue());
                    comment.setDisliked(response.body().getLikeStatus() == LikeStatus.DISLIKED.getValue());
                } else {
                    Toasty.error(view.getContext(), view.getContext().getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onFailure(Call<AccountComment> call, Throwable t) {
                Toasty.error(view.getContext(), view.getContext().getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                Log.e("PodcastViewModel", "accountComment: ", t);
            }
        });
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
