package com.podcasses.viewmodel;

import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import androidx.databinding.Observable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class SearchViewModel extends BasePodcastViewModel implements Observable {

    SearchViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<ApiResponse> podcasts(LifecycleOwner lifecycleOwner, String podcast, boolean isSwipedToRefresh, boolean saveData) {
        return repository.getPodcasts(lifecycleOwner, podcast, null, null, isSwipedToRefresh, saveData);
    }

}
