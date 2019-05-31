package com.podcasses.viewmodel;

import androidx.lifecycle.LiveData;

import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountPodcastsViewModel extends BasePodcastViewModel {

    AccountPodcastsViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<ApiResponse> getHistoryPodcasts(String token, Integer likeStatus) {
        return repository.getHistoryPodcasts(token, likeStatus);
    }

}
