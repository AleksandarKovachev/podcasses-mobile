package com.podcasses.model.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Comment;
import com.podcasses.model.response.Language;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.ConnectivityUtil;

import java.net.ConnectException;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class MainDataRepository {

    private final Application context;

    private final LocalDataSource localDataSource;
    private final NetworkDataSource networkDataSource;

    private final MutableLiveData<ApiResponse> accountResponse;
    private final MutableLiveData<ApiResponse> accountSubscribesResponse;
    private final MutableLiveData<ApiResponse> checkAccountSubscribeResponse;
    private final MutableLiveData<ApiResponse> podcastResponse;
    private final MutableLiveData<ApiResponse> trendingPodcastsResponse;
    private final MutableLiveData<ApiResponse> historyPodcasts;
    private final MutableLiveData<ApiResponse> likedPodcasts;
    private final MutableLiveData<ApiResponse> podcastFilesResponse;
    private final MutableLiveData<ApiResponse> accountPodcastResponse;
    private final MutableLiveData<ApiResponse> accountPodcastsResponse;
    private final MutableLiveData<ApiResponse> commentsResponse;
    private final MutableLiveData<ApiResponse> accountCommentsResponse;

    private LiveData<Podcast> podcastLiveData;
    private LiveData<List<Podcast>> podcastsLiveData;
    private LiveData<AccountPodcast> accountPodcastLiveData;
    private LiveData<List<PodcastFile>> podcastFilesListLiveData;
    private LiveData<Account> accountLiveData;

    private MutableLiveData<List<Nomenclature>> categories;
    private MutableLiveData<List<Language>> languages;
    private MutableLiveData<List<Nomenclature>> privacies;
    private MutableLiveData<List<Nomenclature>> countries;

    @Inject
    public MainDataRepository(ApiCallInterface apiCallInterface, LocalDataSource localDataSource, Application context) {
        this.context = context;
        this.localDataSource = localDataSource;
        networkDataSource = new NetworkDataSource(apiCallInterface, context);
        accountResponse = new MutableLiveData<>();
        accountSubscribesResponse = new MutableLiveData<>();
        checkAccountSubscribeResponse = new MutableLiveData<>();
        podcastResponse = new MutableLiveData<>();
        trendingPodcastsResponse = new MutableLiveData<>();
        historyPodcasts = new MutableLiveData<>();
        likedPodcasts = new MutableLiveData<>();
        podcastFilesResponse = new MutableLiveData<>();
        accountPodcastResponse = new MutableLiveData<>();
        accountPodcastsResponse = new MutableLiveData<>();
        commentsResponse = new MutableLiveData<>();
        accountCommentsResponse = new MutableLiveData<>();
        categories = new MutableLiveData<>();
        languages = new MutableLiveData<>();
        privacies = new MutableLiveData<>();
        countries = new MutableLiveData<>();
    }

    public void savePodcast(Podcast podcast) {
        localDataSource.insertPodcasts(podcast);
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

    public LiveData<ApiResponse> getAccountById(String id) {
        accountResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getUserAccountById(id, new IDataCallback<Account>() {
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

    public LiveData<ApiResponse> checkAccountSubscribe(String accountId) {
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

    public LiveData<ApiResponse> checkAccountSubscribe(String token, String accountId) {
        checkAccountSubscribeResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.checkAccountSubscribe(token, accountId, new IDataCallback<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    checkAccountSubscribeResponse.setValue(ApiResponse.success(data != null && data == 1));
                }

                @Override
                public void onFailure(Throwable error) {
                    checkAccountSubscribeResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountSubscribesResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return checkAccountSubscribeResponse;
    }

    public LiveData<ApiResponse> getPodcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId, String userId, boolean isSwipedToRefresh, boolean saveData) {
        podcastResponse.setValue(ApiResponse.loading());

        if (isSwipedToRefresh) {
            fetchPodcastsOnNetwork(podcast, podcastId, userId, saveData);
        } else {
            if (!Strings.isEmptyOrWhitespace(userId)) {
                podcastsLiveData = localDataSource.getUserPodcasts(userId);
                podcastsLiveData.observe(lifecycleOwner, podcasts -> onPodcastsFetched(lifecycleOwner, podcasts, podcast, podcastId, userId, saveData));
            } else if (!Strings.isEmptyOrWhitespace(podcastId)) {
                podcastLiveData = localDataSource.getPodcastById(podcastId);
                podcastLiveData.observe(lifecycleOwner, p -> onPodcastsFetched(lifecycleOwner, p, podcast, podcastId, userId, saveData));
            }
        }

        return podcastResponse;
    }

    public LiveData<ApiResponse> getTrendingPodcasts(TrendingFilter trendingFilter) {
        trendingPodcastsResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getTrendingPodcasts(trendingFilter, new IDataCallback<List<Podcast>>() {
                @Override
                public void onSuccess(List<Podcast> data) {
                    trendingPodcastsResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    trendingPodcastsResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            podcastResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return trendingPodcastsResponse;
    }

    public LiveData<ApiResponse> getHistoryPodcasts(String token, Integer likeStatus) {
        if (likeStatus == null) {
            historyPodcasts.setValue(ApiResponse.loading());
        } else {
            likedPodcasts.setValue(ApiResponse.loading());
        }

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getPodcasts(token, likeStatus, new IDataCallback<List<Podcast>>() {
                @Override
                public void onSuccess(List<Podcast> data) {
                    if (likeStatus == null) {
                        historyPodcasts.setValue(ApiResponse.success(data));
                    } else {
                        likedPodcasts.setValue(ApiResponse.success(data));
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    if (likeStatus == null) {
                        historyPodcasts.setValue(ApiResponse.error(error));
                    } else {
                        likedPodcasts.setValue(ApiResponse.error(error));
                    }
                }
            });
        } else {
            podcastResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return likeStatus == null ? historyPodcasts : likedPodcasts;
    }

    public LiveData<ApiResponse> getPodcastFiles(LifecycleOwner lifecycleOwner, String token, String userId, boolean isSwipedToRefresh) {
        podcastFilesResponse.setValue(ApiResponse.loading());

        if (isSwipedToRefresh) {
            fetchPodcastFilesOnNetwork(token);
        } else {
            podcastFilesListLiveData = localDataSource.getUserPodcastFiles(userId);
            podcastFilesListLiveData.observe(lifecycleOwner, podcastFiles -> onPodcastFilesFetched(lifecycleOwner, podcastFiles, token));
        }

        return podcastFilesResponse;
    }

    public LiveData<ApiResponse> getAccountPodcasts(LifecycleOwner lifecycleOwner, String token, String accountId, String podcastId, boolean isSwipedToRefresh) {
        accountPodcastResponse.setValue(ApiResponse.loading());

        if (isSwipedToRefresh) {
            fetchAccountPodcastOnNetwork(token, podcastId);
        } else {
            accountPodcastLiveData = localDataSource.getAccountPodcast(accountId, podcastId);
            accountPodcastLiveData.observe(lifecycleOwner, accountPodcast -> onAccountPodcastFetched(lifecycleOwner, accountPodcast, token, podcastId));
        }

        return accountPodcastResponse;
    }

    public LiveData<ApiResponse> getAccountPodcasts(String token, List<String> podcastIds) {
        accountPodcastsResponse.setValue(ApiResponse.loading());

        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getAccountPodcasts(token, podcastIds, new IDataCallback<List<AccountPodcast>>() {
                @Override
                public void onSuccess(List<AccountPodcast> data) {
                    accountPodcastsResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    accountPodcastsResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            podcastResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return accountPodcastsResponse;
    }

    public LiveData<List<Nomenclature>> getCategories() {
        if (!CollectionUtils.isEmpty(categories.getValue())) {
            return categories;
        }
        networkDataSource.getCategories(getNomenclaturesCallback(categories, "getCategories"));
        return categories;
    }

    public LiveData<List<Language>> getLanguages() {
        if (!CollectionUtils.isEmpty(languages.getValue())) {
            return languages;
        }
        networkDataSource.getLanguages(getNomenclaturesCallback(languages, "getLanguages"));
        return languages;
    }

    public LiveData<List<Nomenclature>> getPrivacies() {
        if (!CollectionUtils.isEmpty(privacies.getValue())) {
            return privacies;
        }
        networkDataSource.getPrivacies(getNomenclaturesCallback(privacies, "getPrivacies"));
        return privacies;
    }

    public LiveData<List<Nomenclature>> getCountries() {
        if (!CollectionUtils.isEmpty(countries.getValue())) {
            return countries;
        }
        networkDataSource.getCountries(getNomenclaturesCallback(countries, "getCountries"));
        return countries;
    }

    public void deletePodcastFile(String id) {
        localDataSource.deletePodcastFile(id);
    }

    public LiveData<ApiResponse> getComments(String podcastId) {
        commentsResponse.setValue(ApiResponse.loading());
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getComments(podcastId, new IDataCallback<List<Comment>>() {
                @Override
                public void onSuccess(List<Comment> data) {
                    commentsResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    commentsResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountSubscribesResponse.setValue(ApiResponse.error(new ConnectException()));
        }
        return commentsResponse;
    }

    public LiveData<ApiResponse> getAccountComments(String token, List<String> commentIds) {
        accountCommentsResponse.setValue(ApiResponse.loading());
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getAccountComments(token, commentIds, new IDataCallback<List<AccountComment>>() {
                @Override
                public void onSuccess(List<AccountComment> data) {
                    accountCommentsResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    accountCommentsResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountSubscribesResponse.setValue(ApiResponse.error(new ConnectException()));
        }
        return accountCommentsResponse;
    }

    private void onPodcastsFetched(LifecycleOwner lifecycleOwner, Podcast podcast, String podcastTitle, String podcastId, String userId, boolean saveData) {
        podcastLiveData.removeObservers(lifecycleOwner);
        if (podcast == null) {
            fetchPodcastsOnNetwork(podcastTitle, podcastId, userId, saveData);
        } else {
            podcastResponse.setValue(ApiResponse.success(podcast));
        }
    }

    private void onPodcastFilesFetched(LifecycleOwner lifecycleOwner, List<PodcastFile> podcastFiles, String token) {
        podcastFilesListLiveData.removeObservers(lifecycleOwner);
        if (CollectionUtils.isEmpty(podcastFiles)) {
            fetchPodcastFilesOnNetwork(token);
        } else {
            podcastFilesResponse.setValue(ApiResponse.success(podcastFiles));
        }
    }

    private void onAccountPodcastFetched(LifecycleOwner lifecycleOwner, AccountPodcast accountPodcast, String token, String podcastId) {
        accountPodcastLiveData.removeObservers(lifecycleOwner);
        if (accountPodcast == null) {
            fetchAccountPodcastOnNetwork(token, podcastId);
        } else {
            accountPodcastResponse.setValue(ApiResponse.success(accountPodcast));
        }
    }

    private void onPodcastsFetched(LifecycleOwner lifecycleOwner, List<Podcast> podcasts, String podcast, String podcastId, String userId, boolean saveData) {
        podcastsLiveData.removeObservers(lifecycleOwner);
        if (CollectionUtils.isEmpty(podcasts)) {
            fetchPodcastsOnNetwork(podcast, podcastId, userId, saveData);
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

    private void fetchAccountOnNetwork(String username) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getUserAccount(username, new IDataCallback<Account>() {
                @Override
                public void onSuccess(Account data) {
                    accountResponse.setValue(ApiResponse.success(data));

                    if (data != null) {
                        localDataSource.insertAccount(data);
                    }
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

    private void fetchPodcastsOnNetwork(String podcast, String podcastId, String userId, boolean saveData) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getPodcasts(podcast, podcastId, userId, new IDataCallback<List<Podcast>>() {
                @Override
                public void onSuccess(List<Podcast> data) {
                    podcastResponse.setValue(ApiResponse.success(data));

                    if (saveData && !CollectionUtils.isEmpty(data)) {
                        localDataSource.deleteAllPodcasts();
                        localDataSource.insertPodcasts(data.toArray(new Podcast[0]));
                    }
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

    private void fetchPodcastFilesOnNetwork(String token) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getPodcastFiles(token, new IDataCallback<List<PodcastFile>>() {
                @Override
                public void onSuccess(List<PodcastFile> data) {
                    podcastFilesResponse.setValue(ApiResponse.success(data));

                    if (!CollectionUtils.isEmpty(data)) {
                        localDataSource.deletePodcastFiles();
                        localDataSource.insertPodcastFiles(data.toArray(new PodcastFile[0]));
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    podcastFilesResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            accountSubscribesResponse.setValue(ApiResponse.error(new ConnectException()));
        }
    }

    private void fetchAccountPodcastOnNetwork(String token, String podcastId) {
        if (ConnectivityUtil.checkInternetConnection(context)) {
            networkDataSource.getAccountPodcast(token, podcastId, new IDataCallback<AccountPodcast>() {
                @Override
                public void onSuccess(AccountPodcast data) {
                    accountPodcastResponse.setValue(ApiResponse.success(data));
                }

                @Override
                public void onFailure(Throwable error) {
                    accountPodcastResponse.setValue(ApiResponse.error(error));
                }
            });
        } else {
            podcastResponse.setValue(ApiResponse.error(new ConnectException()));
        }
    }

    private <T> IDataCallback<List<T>> getNomenclaturesCallback(MutableLiveData<List<T>> nomenclatures, String method) {
        return new IDataCallback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                nomenclatures.setValue(data);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e("MainDataRepository", method, error);
            }
        };
    }

}
