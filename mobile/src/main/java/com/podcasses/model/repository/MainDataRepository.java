package com.podcasses.model.repository;

import android.app.Application;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.retrofit.util.ConnectivityUtil;

import java.net.ConnectException;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class MainDataRepository {

    private final Application context;

    private final LocalDataSource localDataSource;
    private final NetworkDataSource networkDataSource;

    private final MutableLiveData<ApiResponse> accountResponse;
    private final MutableLiveData<ApiResponse> accountSubscribesResponse;
    private final MutableLiveData<ApiResponse> podcastResponse;

    @Inject
    public MainDataRepository(ApiCallInterface apiCallInterface, LocalDataSource localDataSource, Application context) {
        this.context = context;
        this.localDataSource = localDataSource;
        networkDataSource = new NetworkDataSource(apiCallInterface);
        accountResponse = new MutableLiveData<>();
        accountSubscribesResponse = new MutableLiveData<>();
        podcastResponse = new MutableLiveData<>();
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

    public LiveData<ApiResponse> getPodcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId, String userId) {
        podcastResponse.setValue(ApiResponse.loading());

        if (!Strings.isEmptyOrWhitespace(userId)) {
            localDataSource.getUserPodcasts(userId).observe(lifecycleOwner,
                    podcasts -> onPodcastsFetched(podcasts, podcast, podcastId, userId));
        } else if (!Strings.isEmptyOrWhitespace(podcastId)) {
            localDataSource.getPodcastById(podcastId).observe(lifecycleOwner,
                    p -> onPodcastsFetched(p, podcast, podcastId, userId));
        }

        return podcastResponse;
    }

    private void onPodcastsFetched(Podcast podcast, String podcastTitle, String podcastId, String userId) {
        if (podcast == null) {
            fetchPodcastsOnNewtork(podcastTitle, podcastId, userId);
        } else {
            podcastResponse.setValue(ApiResponse.success(podcast));
        }
    }

    private void onPodcastsFetched(List<Podcast> podcasts, String podcast, String podcastId, String userId) {
        if (CollectionUtils.isEmpty(podcasts)) {
            fetchPodcastsOnNewtork(podcast, podcastId, userId);
        } else {
            podcastResponse.setValue(ApiResponse.success(podcasts));
        }
    }

    private void fetchPodcastsOnNewtork(String podcast, String podcastId, String userId) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getPodcasts(podcast, podcastId, userId, new IDataCallback<List<Podcast>>() {
                @Override
                public void onSuccess(List<Podcast> data) {
                    podcastResponse.setValue(ApiResponse.success(data));

                    localDataSource.insertPodcasts(data.toArray(new Podcast[data.size()]));
                }

                @Override
                public void onFailure(Throwable error) {
                    podcastResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            podcastResponse.setValue(ApiResponse.error(new ConnectException()));
        }
    }

}
