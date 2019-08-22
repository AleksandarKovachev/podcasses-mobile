package com.podcasses.viewmodel;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.podcasses.constant.PodcastType;
import com.podcasses.model.entity.base.Podcast;
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

    public LiveData<ApiResponse> getPodcastsByType(LifecycleOwner lifecycleOwner, String token, Integer likeStatus,
                                                   PodcastType podcastTypeEnum, boolean isSwipedToRefresh, int page) {
        return repository.getPodcastsByPodcastType(lifecycleOwner, token, likeStatus, podcastTypeEnum, isSwipedToRefresh, page);
    }

    public LiveData<List<Podcast>> getDownloadedPodcasts(int page) {
        return repository.getPodcasts(PodcastType.DOWNLOADED, page);
    }

    public LiveData<ApiResponse> getPodcastsFromSubscriptions(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh, int page) {
        return repository.getPodcastsFromSubscriptions(lifecycleOwner, token, isSwipedToRefresh, page);
    }

}
