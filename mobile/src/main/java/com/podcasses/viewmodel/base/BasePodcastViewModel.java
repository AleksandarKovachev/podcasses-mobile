package com.podcasses.viewmodel.base;

import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.adapter.PodcastChannelAdapter;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.response.Account;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.PopupMenuUtil;

import java.util.Collections;
import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BasePodcastViewModel extends BaseViewModel {

    private MutableLiveData<List<Object>> podcasts = new MutableLiveData<>();
    private MutableLiveData<Podcast> selectedPodcast = new MutableLiveData<>();
    private ObservableField<String> selectedChannel = new ObservableField<>();
    private MutableLiveData<List<PodcastChannel>> podcastChannels = new MutableLiveData<>();
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, R.layout.ad_native_account_podcast, this);
    private PodcastAdapter trendingPodcastAdapter = new PodcastAdapter(R.layout.item_trending_podcast, R.layout.ad_native_trending, this);
    private PodcastChannelAdapter podcastChannelAdapter = new PodcastChannelAdapter(this);

    private ApiCallInterface apiCallInterface;
    private String token;

    public BasePodcastViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public LiveData<ApiResponse> podcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId,
                                          String channelId, boolean isSwipedToRefresh, boolean shouldSavePodcasts, int page) {
        if (!isSwipedToRefresh && podcasts.getValue() != null && !podcasts.getValue().isEmpty() && page == 0) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getPodcasts(lifecycleOwner, podcast, podcastId, channelId, shouldSavePodcasts, isSwipedToRefresh, page);
    }

    public LiveData<ApiResponse> accountPodcasts(LifecycleOwner lifecycleOwner, String token, List<String> podcastIds, boolean isSwipedToRefresh) {
        this.token = token;
        return repository.getAccountPodcasts(lifecycleOwner, token, podcastIds, isSwipedToRefresh);
    }

    public PodcastAdapter getTrendingPodcastAdapter() {
        return trendingPodcastAdapter;
    }

    public void setTrendingPodcastsInAdapter(List<Object> podcasts) {
        this.podcasts.setValue(podcasts);
        this.trendingPodcastAdapter.setPodcasts(podcasts);
    }

    public void addElementInTrendingPodcastsAdapter(Object element) {
        this.trendingPodcastAdapter.addElement(element);
    }

    public void addElementInPodcastsAdapter(Object element, int index) {
        this.podcastAdapter.addElement(element, index);
    }

    public PodcastAdapter getPodcastAdapter() {
        return podcastAdapter;
    }

    public PodcastChannelAdapter getPodcastChannelAdapter() {
        return podcastChannelAdapter;
    }

    public void clearPodcastsInAdapter() {
        this.podcasts.setValue(Collections.emptyList());
        this.podcastAdapter.setPodcasts(this.podcasts.getValue());
    }

    public void setPodcastChannelsInAdapter(List<PodcastChannel> podcastChannels) {
        if (CollectionUtils.isEmpty(this.podcastChannels.getValue())) {
            this.podcastChannels.setValue(podcastChannels);
        } else {
            podcastChannels.removeAll(this.podcastChannels.getValue());
            this.podcastChannels.getValue().addAll(podcastChannels);
        }
        this.podcastChannelAdapter.setPodcastChannels(Collections.singletonList(this.podcastChannels.getValue()));
    }

    public void setPodcastsInAdapter(List<Object> podcasts) {
        if (CollectionUtils.isEmpty(this.podcasts.getValue())) {
            this.podcasts.setValue(podcasts);
        } else {
            podcasts.removeAll(this.podcasts.getValue());
            this.podcasts.getValue().addAll(podcasts);
        }
        this.podcastAdapter.setPodcasts(this.podcasts.getValue());
    }

    public MutableLiveData<Podcast> getSelectedPodcast() {
        return selectedPodcast;
    }

    public void onPodcastClick(Integer index) {
        Podcast podcast = (Podcast) podcasts.getValue().get(index);
        selectedPodcast.setValue(podcast);
    }

    public ObservableField<String> getSelectedChannel() {
        return selectedChannel;
    }

    public void onChannelClick(Integer index) {
        selectedChannel.set(((Podcast) podcasts.getValue().get(index)).getChannelId());
    }

    public List<Object> getPodcasts() {
        return podcasts.getValue();
    }

    public Podcast getPodcastAt(Integer index) {
        if (podcasts.getValue() != null && index != null && podcasts.getValue().size() > index) {
            return (Podcast) podcasts.getValue().get(index);
        }
        return null;
    }

    public PodcastChannel getPodcastChannelAt(Integer index) {
        if (podcastChannels.getValue() != null && index != null && podcastChannels.getValue().size() > index) {
            return podcastChannels.getValue().get(index);
        }
        return null;
    }

    public void onOptionsButtonClick(View view, Integer position) {
        PopupMenuUtil.podcastPopupMenu(this, view, (Podcast) podcasts.getValue().get(position), apiCallInterface, token);
    }

}
