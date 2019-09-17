package com.podcasses.viewmodel;

import android.view.View;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelViewModel extends BasePodcastViewModel {

    private MutableLiveData<PodcastChannel> podcastChannel = new MutableLiveData<>();
    private ObservableField<Integer> podcastsCount = new ObservableField<>(0);
    private ObservableField<Integer> subscribes = new ObservableField<>(0);
    private ObservableField<Integer> views = new ObservableField<>(0);
    private ObservableField<Boolean> isMyPodcastChannel = new ObservableField();
    private ObservableField<Boolean> isSubscribed = new ObservableField();

    PodcastChannelViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<ApiResponse> podcastChannel(String id) {
        return repository.getPodcastChannel(id);
    }

    public LiveData<ApiResponse> checkPodcastChannelSubscribe(String token, String channelId) {
        return repository.checkPodcastChannelSubscribe(token, channelId);
    }

    @Bindable
    public PodcastChannel getPodcastChannel() {
        return podcastChannel.getValue();
    }

    @Bindable
    public String getPodcastsCount() {
        return podcastsCount.get().toString();
    }

    @Bindable
    public String getSubscribes() {
        return subscribes.get().toString();
    }

    @Bindable
    public String getViews() {
        return views.get().toString();
    }

    @Bindable
    public Boolean getIsMyPodcastChannel() {
        return isMyPodcastChannel.get();
    }

    @Bindable
    public Boolean getIsSubscribed() {
        return isSubscribed.get();
    }

    public void setPodcastChannel(PodcastChannel podcastChannel) {
        this.podcastChannel.setValue(podcastChannel);
        notifyPropertyChanged(com.podcasses.BR.podcastChannel);
    }

    public void setPodcastsCount(Integer podcastsCount) {
        this.podcastsCount.set(podcastsCount);
        notifyPropertyChanged(com.podcasses.BR.podcastsCount);
    }

    public void setSubscribes(Integer subscribes) {
        this.subscribes.set(subscribes);
        notifyPropertyChanged(BR.subscribes);
    }

    public void setViews(Integer views) {
        this.views.set(views);
        notifyPropertyChanged(BR.views);
    }

    public void setIsMyPodcastChannel(Boolean isMyPodcastChannel) {
        this.isMyPodcastChannel.set(isMyPodcastChannel);
        notifyPropertyChanged(BR.isMyPodcastChannel);
    }

    public void setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed.set(isSubscribed);
        notifyPropertyChanged(BR.isSubscribed);
    }

    public void onSubscribeClick(View view, String token) {

    }

}
