package com.podcasses.viewmodel;

import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import androidx.databinding.Observable;

/**
 * Created by aleksandar.kovachev.
 */
public class SearchViewModel extends BasePodcastViewModel implements Observable {

    SearchViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

}
