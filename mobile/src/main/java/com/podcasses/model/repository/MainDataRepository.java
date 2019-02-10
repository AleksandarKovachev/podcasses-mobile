package com.podcasses.model.repository;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Nomenclature;
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

    private LiveData<Podcast> podcastLiveData;
    private LiveData<List<Podcast>> podcastsLiveData;
    private LiveData<Account> accountLiveData;

    private MutableLiveData<List<Nomenclature>> categories;
    private MutableLiveData<List<Nomenclature>> languages;
    private MutableLiveData<List<Nomenclature>> privacies;

    @Inject
    public MainDataRepository(ApiCallInterface apiCallInterface, LocalDataSource localDataSource, Application context) {
        this.context = context;
        this.localDataSource = localDataSource;
        networkDataSource = new NetworkDataSource(apiCallInterface);
        accountResponse = new MutableLiveData<>();
        accountSubscribesResponse = new MutableLiveData<>();
        podcastResponse = new MutableLiveData<>();
        categories = new MutableLiveData<>();
        languages = new MutableLiveData<>();
        privacies = new MutableLiveData<>();
    }

    public LiveData<ApiResponse> getAccount(LifecycleOwner lifecycleOwner, String username, boolean isSwipedToRefresh) {
        accountResponse.setValue(ApiResponse.loading());

        if (isSwipedToRefresh) {
            fetchAccountOnNetwork(username);
        } else if (accountLiveData != null && accountLiveData.getValue() != null) {
            accountResponse.setValue(ApiResponse.success(accountLiveData.getValue()));
        } else {
            accountLiveData = localDataSource.getAccountByUsername(username);
            accountLiveData.observe(lifecycleOwner, account -> onAccountFetched(lifecycleOwner, account, username));
        }

        return accountResponse;
    }

    public LiveData<ApiResponse> getAccountSubscribes(LifecycleOwner lifecycleOwner, String accountId, boolean isSwipedToRefresh) {
        accountSubscribesResponse.setValue(ApiResponse.loading());

        if (isSwipedToRefresh) {
            fetchAccountSubscribesOnNetwork(accountId);
        } else if (accountLiveData != null && accountLiveData.getValue() != null && accountLiveData.getValue().getSubscribes() != null) {
            accountSubscribesResponse.setValue(ApiResponse.success(accountLiveData.getValue().getSubscribes()));
        } else {
            accountLiveData = localDataSource.getAccountById(accountId);
            accountLiveData.observe(lifecycleOwner, account -> onAccountSubscribesFetched(lifecycleOwner, account, accountId));
        }

        return accountSubscribesResponse;
    }

    public LiveData<ApiResponse> getPodcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId, String userId, boolean isSwipedToRefresh) {
        podcastResponse.setValue(ApiResponse.loading());

        if (isSwipedToRefresh) {
            fetchPodcastsOnNetwork(podcast, podcastId, userId);
        } else {
            if (!Strings.isEmptyOrWhitespace(userId)) {
                podcastsLiveData = localDataSource.getUserPodcasts(userId);
                podcastsLiveData.observe(lifecycleOwner, podcasts -> onPodcastsFetched(lifecycleOwner, podcasts, podcast, podcastId, userId));
            } else if (!Strings.isEmptyOrWhitespace(podcastId)) {
                podcastLiveData = localDataSource.getPodcastById(podcastId);
                podcastLiveData.observe(lifecycleOwner, p -> onPodcastsFetched(lifecycleOwner, p, podcast, podcastId, userId));
            }
        }

        return podcastResponse;
    }

    public MutableLiveData<List<Nomenclature>> getCategories() {
        networkDataSource.getCategories(getNomenclaturesCallback(categories, "getCategories"));
        return categories;
    }

    public MutableLiveData<List<Nomenclature>> getLanguages() {
        networkDataSource.getLanguages(getNomenclaturesCallback(languages, "getLanguages"));
        return languages;
    }

    public MutableLiveData<List<Nomenclature>> getPrivacies() {
        networkDataSource.getPrivacies(getNomenclaturesCallback(privacies, "getPrivacies"));
        return privacies;
    }

    private void onPodcastsFetched(LifecycleOwner lifecycleOwner, Podcast podcast, String podcastTitle, String podcastId, String userId) {
        podcastLiveData.removeObservers(lifecycleOwner);
        if (podcast == null) {
            fetchPodcastsOnNetwork(podcastTitle, podcastId, userId);
        } else {
            podcastResponse.setValue(ApiResponse.success(podcast));
        }
    }

    private void onPodcastsFetched(LifecycleOwner lifecycleOwner, List<Podcast> podcasts, String podcast, String podcastId, String userId) {
        podcastsLiveData.removeObservers(lifecycleOwner);
        if (CollectionUtils.isEmpty(podcasts)) {
            fetchPodcastsOnNetwork(podcast, podcastId, userId);
        } else {
            podcastResponse.setValue(ApiResponse.success(podcasts));
        }
    }

    private void onAccountFetched(LifecycleOwner lifecycleOwner, Account account, String username) {
        accountLiveData.removeObservers(lifecycleOwner);
        if (account != null) {
            accountResponse.setValue(ApiResponse.success(account));
        } else {
            fetchAccountOnNetwork(username);
        }
    }

    private void onAccountSubscribesFetched(LifecycleOwner lifecycleOwner, Account account, String accountId) {
        accountLiveData.removeObservers(lifecycleOwner);
        if (account != null && account.getSubscribes() != null) {
            accountResponse.setValue(ApiResponse.success(account.getSubscribes()));
        } else {
            fetchAccountSubscribesOnNetwork(accountId);
        }
    }

    private void fetchAccountOnNetwork(String username) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getUserAccount(username, new IDataCallback<Account>() {
                @Override
                public void onSuccess(Account data) {
                    accountResponse.setValue(ApiResponse.success(data));

                    localDataSource.insertAccount(data);
                }

                @Override
                public void onFailure(Throwable error) {
                    accountResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountResponse.setValue(ApiResponse.error(new ConnectException()));
        }
    }

    private void fetchPodcastsOnNetwork(String podcast, String podcastId, String userId) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getPodcasts(podcast, podcastId, userId, new IDataCallback<List<Podcast>>() {
                @Override
                public void onSuccess(List<Podcast> data) {
                    podcastResponse.setValue(ApiResponse.success(data));

                    localDataSource.insertPodcasts(data.toArray(new Podcast[0]));
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

    private void fetchAccountSubscribesOnNetwork(String accountId) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getAccountSubscribes(accountId, new IDataCallback<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    accountSubscribesResponse.setValue(ApiResponse.success(data));

                    localDataSource.updateAccountSubscribes(data, accountId);
                }

                @Override
                public void onFailure(Throwable error) {
                    accountSubscribesResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountSubscribesResponse.setValue(ApiResponse.error(new ConnectException()));
        }
    }

    private IDataCallback<List<Nomenclature>> getNomenclaturesCallback(MutableLiveData<List<Nomenclature>> nomenclatures, String method) {
        return new IDataCallback<List<Nomenclature>>() {
            @Override
            public void onSuccess(List<Nomenclature> data) {
                nomenclatures.setValue(data);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e("MainDataRepository", method, error);
            }
        };
    }

}
