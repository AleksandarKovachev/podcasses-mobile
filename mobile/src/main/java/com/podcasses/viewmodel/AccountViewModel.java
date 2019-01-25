package com.podcasses.viewmodel;

import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.viewmodel.base.BaseViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountViewModel extends BaseViewModel {

    public AccountViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<ApiResponse> account(@NonNull String username) {
        return repository.getAccount(username);
    }

}
