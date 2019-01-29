package com.podcasses.model.repository;

import android.app.Application;

import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.retrofit.util.ConnectivityUtil;

import java.net.ConnectException;
import java.util.List;

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

    private MutableLiveData<ApiResponse> accountSubscribesResponse = new MutableLiveData<>();

    private MutableLiveData<ApiResponse> podcastResponse = new MutableLiveData<>();

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
        } else {
            accountResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return accountResponse;
    }

    public LiveData<ApiResponse> getAccountSubscribes(String accountId) {
        accountSubscribesResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getAccountSubscribes(accountId, new IDataCallback<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    accountSubscribesResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    accountSubscribesResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountSubscribesResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return accountSubscribesResponse;
    }

    public LiveData<ApiResponse> getPodcasts(String podcast) {
        podcastResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getPodcasts(podcast, new IDataCallback<List<Podcast>>() {
                @Override
                public void onSuccess(List<Podcast> data) {
                    podcastResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    podcastResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            podcastResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return podcastResponse;
    }

}
