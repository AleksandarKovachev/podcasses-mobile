package com.podcasses.viewmodel;

import android.net.Uri;
import android.view.View;

import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.gms.common.util.CollectionUtils;
import com.ohoussein.playpause.PlayPauseView;
import com.podcasses.BR;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastCommentAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.service.AudioDownloadService;
import com.podcasses.viewmodel.base.BaseViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    PodcastViewModel(MainDataRepository repository) {
        super(repository);
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

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
