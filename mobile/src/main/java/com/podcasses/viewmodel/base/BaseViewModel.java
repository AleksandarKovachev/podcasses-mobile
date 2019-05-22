package com.podcasses.viewmodel.base;

import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;

import com.podcasses.model.repository.MainDataRepository;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseViewModel extends ViewModel implements Observable {

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

}
