package com.podcasses.viewmodel;

import androidx.databinding.Observable;

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

}
