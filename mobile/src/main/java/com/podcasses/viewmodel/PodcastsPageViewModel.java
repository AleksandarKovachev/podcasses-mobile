package com.podcasses.viewmodel;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.podcasses.constant.PodcastTypeEnum;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastsPageViewModel extends BasePodcastViewModel {

    PodcastsPageViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<ApiResponse> getHistoryPodcasts(LifecycleOwner lifecycleOwner, String token, Integer likeStatus,
                                                    PodcastTypeEnum podcastTypeEnum, boolean isSwipedToRefresh) {
        return repository.getHistoryPodcasts(lifecycleOwner, token, likeStatus, podcastTypeEnum, isSwipedToRefresh);
    }

    public LiveData<List<Podcast>> getDownloadedPodcasts() {
        return repository.getPodcasts(PodcastTypeEnum.DOWNLOADED);
    }

    public LiveData<ApiResponse> getPodcastsFromSubscriptions(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh) {
        return repository.getPodcastsFromSubscriptions(lifecycleOwner, token, isSwipedToRefresh);
    }

}
