package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.ConnectivityUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelViewModel extends BasePodcastViewModel {

    private MutableLiveData<PodcastChannel> podcastChannel = new MutableLiveData<>();
    private ObservableField<String> selectedAuthor = new ObservableField<>();
    private ObservableField<Integer> podcastsCount = new ObservableField<>(0);
    private ObservableField<Integer> subscribes = new ObservableField<>(0);
    private ObservableField<Integer> views = new ObservableField<>(0);
    private ObservableField<Boolean> isMyPodcastChannel = new ObservableField();
    private ObservableField<Boolean> isSubscribed = new ObservableField();

    private ApiCallInterface apiCallInterface;

    PodcastChannelViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
        this.apiCallInterface = apiCallInterface;
    }

    public LiveData<ApiResponse> podcastChannel(LifecycleOwner lifecycleOwner, String id) {
        return repository.getPodcastChannel(lifecycleOwner, id);
    }

    public LiveData<ApiResponse> podcastChannelViews(String channelId) {
        return repository.podcastChannelViews(channelId);
    }

    public LiveData<ApiResponse> podcastChannelSubscribes(String channelId) {
        return repository.podcastChannelSubscribes(channelId);
    }

    public LiveData<ApiResponse> podcastChannelEpisodes(String token, String channelId) {
        return repository.podcastChannelEpisodes(token, channelId);
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
        if (podcastsCount.get() != null)
            return podcastsCount.get().toString();
        return "0";
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

    public ObservableField<String> getSelectedAuthor() {
        return selectedAuthor;
    }

    public void onAuthorClick(View view) {
        selectedAuthor.set(podcastChannel.getValue().getUserId());
    }

    public void onSubscribeClick(View view, String token, String deviceId) {
        if (token == null || !ConnectivityUtil.checkInternetConnection(view.getContext())) {
            return;
        }

        Call<Integer> subscribeAccountCall = apiCallInterface.podcastChannelSubscribe("Bearer " + token, podcastChannel.getValue().getId(), deviceId);
        subscribeAccountCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body() == 1) {
                        setIsSubscribed(true);
                        Toasty.success(view.getContext(), view.getContext().getString(R.string.successful_subscribing), Toast.LENGTH_SHORT, true).show();
                    } else {
                        setIsSubscribed(false);
                        Toasty.success(view.getContext(), view.getContext().getString(R.string.successful_unsubscribing), Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, view.getContext());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, view.getContext());
            }
        });
    }

}
