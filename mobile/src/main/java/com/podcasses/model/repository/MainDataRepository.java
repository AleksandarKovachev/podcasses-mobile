package com.podcasses.model.repository;

import android.app.Application;

import com.podcasses.model.entity.Account;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.retrofit.util.ConnectivityUtil;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class MainDataRepository {

    private LocalDataSource localDataSource;

    private NetworkDataSource networkDataSource;

    private MutableLiveData<ApiResponse> accountResponse = new MutableLiveData<>();

    private MutableLiveData<ApiResponse> accountSubscrbiesResponse = new MutableLiveData<>();

    private Application context;

    @Inject
    public MainDataRepository(ApiCallInterface apiCallInterface, Application context) {
        networkDataSource = new NetworkDataSource(apiCallInterface);
        this.context = context;
    }

    public LiveData<ApiResponse> getAccount(String username) {
        accountResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getUserAccount(username, new IDataCallback<Account>() {
                @Override
                public void onSuccess(Account data) {
                    accountResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    accountResponse.setValue(ApiResponse.error(error));
                }
            });
        }

        return accountResponse;
    }

    public LiveData<ApiResponse> getAccountSubscribes(String accountId) {
        accountSubscrbiesResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getAccountSubscribes(accountId, new IDataCallback<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    accountSubscrbiesResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    accountSubscrbiesResponse.setValue(ApiResponse.error(error));
                }
            });
        }

        return accountSubscrbiesResponse;
    }
}
