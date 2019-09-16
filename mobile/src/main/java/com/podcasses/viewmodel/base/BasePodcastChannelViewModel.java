package com.podcasses.viewmodel.base;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.R;
import com.podcasses.adapter.PodcastChannelAdapter;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;

import java.util.Collections;
import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class BasePodcastChannelViewModel extends BaseViewModel {

    private MutableLiveData<List<Object>> podcastChannels = new MutableLiveData<>();
    private PodcastChannelAdapter podcastChannelAdapter = new PodcastChannelAdapter(R.layout.item_podcast_channel,
            R.layout.ad_native_account_podcast, this);

    private ApiCallInterface apiCallInterface;

    public BasePodcastChannelViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public PodcastChannelAdapter getPodcastChannelAdapter() {
        return podcastChannelAdapter;
    }

    public void setPodcastChannelsInAdapter(List<Object> podcastChannels) {
        if (CollectionUtils.isEmpty(this.podcastChannels.getValue())) {
            this.podcastChannels.setValue(podcastChannels);
        } else {
            podcastChannels.removeAll(this.podcastChannels.getValue());
            this.podcastChannels.getValue().addAll(podcastChannels);
        }
        this.podcastChannelAdapter.setPodcastChannels(this.podcastChannels.getValue());
    }

    public void clearPodcastChannelsInAdapter() {
        this.podcastChannels.setValue(Collections.emptyList());
        this.podcastChannelAdapter.setPodcastChannels(this.podcastChannels.getValue());
    }

    public LiveData<ApiResponse> podcastChannels(LifecycleOwner lifecycleOwner, String token, String userId, String name,
                                                 boolean isMyAccount, boolean isSwipedToRefresh) {
        if (!isSwipedToRefresh && podcastChannels.getValue() != null && !podcastChannels.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getPodcastChannels(lifecycleOwner, token, userId, name, isMyAccount, isSwipedToRefresh);
    }

    public PodcastChannel getPodcastChannelAt(Integer index) {
        if (podcastChannels.getValue() != null && index != null && podcastChannels.getValue().size() > index) {
            return (PodcastChannel) podcastChannels.getValue().get(index);
        }
        return null;
    }

    public void onPodcastChannelClick(Integer index) {

    }

    public void onAuthorClick(Integer index) {

    }

}
