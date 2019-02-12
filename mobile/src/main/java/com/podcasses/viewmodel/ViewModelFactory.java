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
            return (T) new SearchViewModel(repository);
        } else if (modelClass.isAssignableFrom(AccountViewModel.class)) {
            return (T) new AccountViewModel(repository);
        } else if (modelClass.isAssignableFrom(TrendingViewModel.class)) {
            return (T) new TrendingViewModel(repository);
        } else if (modelClass.isAssignableFrom(NotificationsViewModel.class)) {
            return (T) new NotificationsViewModel(repository);
        } else if (modelClass.isAssignableFrom(UploadViewModel.class)) {
            return (T) new UploadViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(PodcastViewModel.class)) {
            return (T) new PodcastViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown class name");
    }

}
