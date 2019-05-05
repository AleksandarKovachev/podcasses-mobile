package com.podcasses.viewmodel;

import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Created by aleksandar.kovachev.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private MainDataRepository repository;
    private ApiCallInterface apiCallInterface;

    @Inject
    public ViewModelFactory(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        this.repository = repository;
        this.apiCallInterface = apiCallInterface;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(repository);
        } else if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(AccountViewModel.class)) {
            return (T) new AccountViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(UploadViewModel.class)) {
            return (T) new UploadViewModel(repository);
        } else if (modelClass.isAssignableFrom(PodcastViewModel.class)) {
            return (T) new PodcastViewModel(repository, apiCallInterface);
        }
        throw new IllegalArgumentException("Unknown class name");
    }

}
