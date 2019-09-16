package com.podcasses.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;

import javax.inject.Inject;

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
            return (T) new HomeViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(AccountViewModel.class)) {
            return (T) new AccountViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(UploadViewModel.class)) {
            return (T) new UploadViewModel(repository);
        } else if (modelClass.isAssignableFrom(PodcastViewModel.class)) {
            return (T) new PodcastViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(PodcastsPageViewModel.class)) {
            return (T) new PodcastsPageViewModel(repository, apiCallInterface);
        } else if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            return (T) new HistoryViewModel(repository);
        } else if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(repository);
        } else if (modelClass.isAssignableFrom(AgreementViewModel.class)) {
            return (T) new AgreementViewModel(repository);
        } else if (modelClass.isAssignableFrom(PodcastChannelViewModel.class)) {
            return (T) new PodcastChannelViewModel(repository, apiCallInterface);
        }
        throw new IllegalArgumentException("Unknown class name");
    }

}
