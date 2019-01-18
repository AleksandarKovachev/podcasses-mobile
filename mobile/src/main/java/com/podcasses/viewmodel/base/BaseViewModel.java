package com.podcasses.viewmodel.base;

import com.podcasses.model.repository.MainDataRepository;

import androidx.lifecycle.ViewModel;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseViewModel extends ViewModel {

    protected MainDataRepository repository;

    public BaseViewModel(MainDataRepository repository) {
        this.repository = repository;
    }

}
