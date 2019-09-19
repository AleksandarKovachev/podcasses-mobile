package com.podcasses.viewmodel;

import androidx.databinding.Observable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

/**
 * Created by aleksandar.kovachev.
 */
public class SearchViewModel extends BasePodcastViewModel implements Observable {

    SearchViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<ApiResponse> getPodcastChannel(LifecycleOwner lifecycleOwner, String name) {
        return repository.getPodcastChannel(lifecycleOwner, null, null, name, false, false);
    }

}
