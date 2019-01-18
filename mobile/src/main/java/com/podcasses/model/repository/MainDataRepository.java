package com.podcasses.model.repository;

import com.google.gson.JsonObject;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class MainDataRepository {

    private LocalDataSource localDataSource;

    private NetworkDataSource networkDataSource;

    private MutableLiveData<ApiResponse> userResponse = new MutableLiveData<>();

    @Inject
    public MainDataRepository(ApiCallInterface apiCallInterface) {
        networkDataSource = new NetworkDataSource(apiCallInterface);
    }

    public LiveData<ApiResponse> getAccount(String username) {
        userResponse.setValue(ApiResponse.loading());

        networkDataSource.getUserAccount(username, new IDataCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject data) {
                userResponse.setValue(ApiResponse.success(data));
            }

            @Override
            public void onFailure(Throwable error) {
                userResponse.setValue(ApiResponse.error(error));
            }
        });

        return userResponse;

    }
}
