package com.podcasses.viewmodel.base;

import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.ApiResponse;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseViewModel extends ViewModel {

    protected MainDataRepository repository;

    public BaseViewModel(MainDataRepository repository) {
        this.repository = repository;
    }

    public LiveData<ApiResponse> accounts(@NonNull List<String> ids) {
        return repository.getAccount(ids);
    }

}
