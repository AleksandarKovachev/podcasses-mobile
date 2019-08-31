package com.podcasses.viewmodel.base;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;

import com.podcasses.BR;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.repository.MainDataRepository;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseViewModel extends ViewModel implements Observable {

    private ObservableBoolean isLoading = new ObservableBoolean(false);

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    protected MainDataRepository repository;

    public BaseViewModel(MainDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    protected void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    @Bindable
    public Boolean getIsLoading() {
        return isLoading.get();
    }

    public void setIsLoading(Boolean isLoading) {
        this.isLoading.set(isLoading);
        notifyPropertyChanged(BR.isLoading);
    }

    public void saveAccountPodcast(AccountPodcast accountPodcast) {
        repository.saveAccountPodcast(accountPodcast);
    }

}
