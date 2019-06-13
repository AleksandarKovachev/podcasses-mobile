package com.podcasses.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.constant.PodcastTypeEnum;
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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class MainDataRepository {

    private final Application context;

    private final LocalDataSource localDataSource;
    private final NetworkDataSource networkDataSource;

    private final MutableLiveData<List<Nomenclature>> categories;
    private final MutableLiveData<List<Language>> languages;
    private final MutableLiveData<List<Nomenclature>> privacies;
    private final MutableLiveData<List<Nomenclature>> countries;

    @Inject
    public MainDataRepository(ApiCallInterface apiCallInterface, LocalDataSource localDataSource, Application context) {
        this.context = context;
        this.localDataSource = localDataSource;
        networkDataSource = new NetworkDataSource(apiCallInterface, context);
        categories = new MutableLiveData<>();
        languages = new MutableLiveData<>();
        privacies = new MutableLiveData<>();
        countries = new MutableLiveData<>();
    }

    public void savePodcast(PodcastTypeEnum podcastType, Podcast podcast) {
        localDataSource.insertPodcasts(podcastType.getType(), podcast);
    }

    public void saveAccountPodcast(AccountPodcast accountPodcast) {
        localDataSource.insertAccountPodcasts(accountPodcast);
    }

    public void saveAccount(Account account) {
        localDataSource.insertAccount(account);
    }

    public LiveData<List<Podcast>> getPodcasts(PodcastTypeEnum type, int page) {
        return localDataSource.getPodcasts(type.getType(), page);
    }

    public void deletePodcast(PodcastTypeEnum podcastType, String podcastId) {
        localDataSource.deletePodcast(podcastType.getType(), podcastId);
    }

    public LiveData<ApiResponse> getAccount(LifecycleOwner lifecycleOwner, String username, String id, boolean isMyAccount, boolean isSwipedToRefresh) {
        MutableLiveData<ApiResponse> accountResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchAccountOnLocalDatabase(lifecycleOwner, accountResponse, username, id, isMyAccount);
        }

        if (ConnectivityUtil.checkInternetConnection(context)) {
            fetchAccountOnNetwork(accountResponse, username, id, isMyAccount);
        } else if (isSwipedToRefresh) {
            accountResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return accountResponse;
    }

    public LiveData<ApiResponse> checkAccountSubscribe(String accountId) {
        MutableLiveData<ApiResponse> accountSubscribesResponse = new MutableLiveData<>(ApiResponse.loading());
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
        MutableLiveData<ApiResponse> checkAccountSubscribeResponse = new MutableLiveData<>(ApiResponse.loading());
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
            checkAccountSubscribeResponse.setValue(ApiResponse.error(new ConnectException()));
        }
        return checkAccountSubscribeResponse;
    }

    public LiveData<ApiResponse> getPodcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId, String userId,
                                             boolean isMyAccount, boolean isSwipedToRefresh, int page) {
        MutableLiveData<ApiResponse> podcastsResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchPodcastsOnLocalDatabase(lifecycleOwner, podcastsResponse, podcastId, isMyAccount, page);
        }

        if (ConnectivityUtil.checkInternetConnection(context)) {
            fetchPodcastsOnNetwork(podcastsResponse, podcast, podcastId, userId, isMyAccount, page, isSwipedToRefresh);
        } else if (isSwipedToRefresh) {
            podcastsResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return podcastsResponse;
    }

    public LiveData<ApiResponse> getTrendingPodcasts(LifecycleOwner lifecycleOwner, TrendingFilter trendingFilter, boolean isSwipedToRefresh) {
        MutableLiveData<ApiResponse> podcastsResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchPodcastsOnLocalDatabaseByPodcastType(lifecycleOwner, podcastsResponse, PodcastTypeEnum.TRENDING, 0);
        }

        if (ConnectivityUtil.checkInternetConnection(context)) {
            fetchTrendingPodcastsOnNetwork(podcastsResponse, trendingFilter);
        } else if (isSwipedToRefresh) {
            podcastsResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return podcastsResponse;
    }

    public LiveData<ApiResponse> getPodcastsByPodcastType(LifecycleOwner lifecycleOwner, String token, Integer likeStatus,
                                                          PodcastTypeEnum podcastType, boolean isSwipedToRefresh, int page) {
        MutableLiveData<ApiResponse> podcastsResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchPodcastsOnLocalDatabaseByPodcastType(lifecycleOwner, podcastsResponse, podcastType, page);
        }

        if (ConnectivityUtil.checkInternetConnection(context) && token != null) {
            fetchAccountPodcastsOnNetwork(podcastsResponse, token, likeStatus, podcastType, page);
        } else if (isSwipedToRefresh) {
            podcastsResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return podcastsResponse;
    }

    public LiveData<ApiResponse> getPodcastsFromSubscriptions(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh, int page) {
        MutableLiveData<ApiResponse> podcastsResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchPodcastsOnLocalDatabaseByPodcastType(lifecycleOwner, podcastsResponse, PodcastTypeEnum.FROM_SUBSCRIPTIONS, page);
        }

        if (ConnectivityUtil.checkInternetConnection(context) && token != null) {
            fetchPodcastsFromSubscriptionsOnNetwork(token, page, podcastsResponse);
        } else if (isSwipedToRefresh) {
            podcastsResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return podcastsResponse;
    }

    public LiveData<ApiResponse> getPodcastFiles(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh) {
        MutableLiveData<ApiResponse> podcastFilesResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchPodcastFilesOnLocalDatabase(lifecycleOwner, podcastFilesResponse);
        }

        if (ConnectivityUtil.checkInternetConnection(context)) {
            fetchPodcastFilesOnNetwork(podcastFilesResponse, token);
        } else if (isSwipedToRefresh) {
            podcastFilesResponse.setValue(ApiResponse.error(new ConnectException()));
        }

        return podcastFilesResponse;
    }

    public LiveData<ApiResponse> getAccountPodcast(LifecycleOwner lifecycleOwner, String token, String podcastId) {
        MutableLiveData<ApiResponse> accountPodcastResponse = new MutableLiveData<>(ApiResponse.loading());

        fetchAccountPodcastOnLocalDatabase(lifecycleOwner, podcastId, accountPodcastResponse);
        if (ConnectivityUtil.checkInternetConnection(context) && token != null) {
            fetchAccountPodcastOnNetwork(token, accountPodcastResponse, podcastId);
        }

        return accountPodcastResponse;
    }

    public LiveData<ApiResponse> getAccountPodcasts(LifecycleOwner lifecycleOwner, String token, List<String> podcastIds, boolean isSwipedToRefresh) {
        MutableLiveData<ApiResponse> accountPodcastsResponse = new MutableLiveData<>(ApiResponse.loading());

        if (!isSwipedToRefresh) {
            fetchAccountPodcastsOnLocalDatabase(lifecycleOwner, accountPodcastsResponse, podcastIds);
        }

        if (ConnectivityUtil.checkInternetConnection(context)) {
            fetchAccountPodcastsOnNetwork(token, podcastIds, accountPodcastsResponse);
        } else if (isSwipedToRefresh) {
            accountPodcastsResponse.setValue(ApiResponse.error(new ConnectException()));
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
        MutableLiveData<ApiResponse> commentsResponse = new MutableLiveData<>(ApiResponse.loading());
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
            commentsResponse.setValue(ApiResponse.error(new ConnectException()));
        }
        return commentsResponse;
    }

    public LiveData<ApiResponse> getAccountComments(String token, List<String> commentIds) {
        MutableLiveData<ApiResponse> accountCommentsResponse = new MutableLiveData<>(ApiResponse.loading());
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
            accountCommentsResponse.setValue(ApiResponse.error(new ConnectException()));
        }
        return accountCommentsResponse;
    }

    private void fetchAccountOnNetwork(MutableLiveData<ApiResponse> accountResponse, String username, String id, boolean isMyAccount) {
        networkDataSource.getUserAccount(username, id, new IDataCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                accountResponse.setValue(ApiResponse.success(account));

                if (account != null) {
                    account.setIsMyAccount(isMyAccount ? 1 : 0);
                    if (isMyAccount) {
                        localDataSource.insertAccount(account);
                    }
                }
            }

            @Override
            public void onFailure(Throwable error) {
                accountResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchAccountOnLocalDatabase(LifecycleOwner lifecycleOwner, MutableLiveData<ApiResponse> accountResponse, String username, String id, boolean isMyAccount) {
        LiveData<Account> accountLiveData;
        if (isMyAccount) {
            accountLiveData = localDataSource.getMyAccountData();
        } else {
            accountLiveData = localDataSource.getAccount(username, id);
        }
        accountLiveData.observe(lifecycleOwner, account -> {
            accountLiveData.removeObservers(lifecycleOwner);
            accountResponse.setValue(ApiResponse.database(account));
        });
    }

    private void fetchPodcastsOnNetwork(MutableLiveData<ApiResponse> podcastsResponse, String podcast, String podcastId,
                                        String userId, boolean isMyAccount, int page, boolean isSwipedToRefresh) {
        networkDataSource.getPodcasts(podcast, podcastId, Collections.singletonList(userId), null, page, new IDataCallback<List<Podcast>>() {
            @Override
            public void onSuccess(List<Podcast> data) {
                podcastsResponse.setValue(ApiResponse.success(data));

                if (isMyAccount && !CollectionUtils.isEmpty(data)) {
                    if (isSwipedToRefresh) {
                        localDataSource.deletePodcastsByType(PodcastTypeEnum.MY_PODCASTS.getType());
                    }
                    localDataSource.insertPodcasts(PodcastTypeEnum.MY_PODCASTS.getType(), data.toArray(new Podcast[0]));
                }
            }

            @Override
            public void onFailure(Throwable error) {
                podcastsResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchPodcastsOnLocalDatabase(LifecycleOwner lifecycleOwner, MutableLiveData<ApiResponse> podcastsResponse,
                                              String podcastId, boolean isMyAccount, int page) {
        if (isMyAccount) {
            LiveData<List<Podcast>> podcastsLiveData = getPodcasts(PodcastTypeEnum.MY_PODCASTS, page);
            podcastsLiveData.observe(lifecycleOwner, podcasts -> {
                podcastsLiveData.removeObservers(lifecycleOwner);
                podcastsResponse.setValue(ApiResponse.database(podcasts));
            });
        } else if (!Strings.isEmptyOrWhitespace(podcastId)) {
            LiveData<Podcast> podcastLiveData = localDataSource.getPodcastById(podcastId);
            podcastLiveData.observe(lifecycleOwner, p -> {
                podcastLiveData.removeObservers(lifecycleOwner);
                podcastsResponse.setValue(ApiResponse.database(p));
            });
        }
    }

    private void fetchTrendingPodcastsOnNetwork(MutableLiveData<ApiResponse> podcastsResponse, TrendingFilter trendingFilter) {
        networkDataSource.getTrendingPodcasts(trendingFilter, new IDataCallback<List<Podcast>>() {
            @Override
            public void onSuccess(List<Podcast> data) {
                podcastsResponse.setValue(ApiResponse.success(data));

                if (!CollectionUtils.isEmpty(data)) {
                    localDataSource.insertPodcasts(PodcastTypeEnum.TRENDING.getType(), data.toArray(new Podcast[0]));
                }
            }

            @Override
            public void onFailure(Throwable error) {
                podcastsResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchPodcastsFromSubscriptionsOnNetwork(String token, int page, MutableLiveData<ApiResponse> podcastsResponse) {
        networkDataSource.getPodcastsFromSubscriptions(token, page, new IDataCallback<List<Podcast>>() {
            @Override
            public void onSuccess(List<Podcast> data) {
                podcastsResponse.setValue(ApiResponse.success(data));

                if (!CollectionUtils.isEmpty(data)) {
                    localDataSource.insertPodcasts(PodcastTypeEnum.FROM_SUBSCRIPTIONS.getType(), data.toArray(new Podcast[0]));
                }
            }

            @Override
            public void onFailure(Throwable error) {
                podcastsResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchAccountPodcastsOnNetwork(MutableLiveData<ApiResponse> podcastsResponse, String token,
                                               Integer likeStatus, PodcastTypeEnum podcastType, int page) {
        networkDataSource.getPodcasts(token, podcastType.name(), likeStatus, page, new IDataCallback<List<Podcast>>() {
            @Override
            public void onSuccess(List<Podcast> data) {
                podcastsResponse.setValue(ApiResponse.success(data));

                if (!CollectionUtils.isEmpty(data)) {
                    localDataSource.insertPodcasts(podcastType.getType(), data.toArray(new Podcast[0]));
                }
            }

            @Override
            public void onFailure(Throwable error) {
                podcastsResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchPodcastsOnLocalDatabaseByPodcastType(LifecycleOwner lifecycleOwner, MutableLiveData<ApiResponse> response,
                                                           PodcastTypeEnum podcastType, int page) {
        LiveData<List<Podcast>> podcasts;
        if (podcastType.equals(PodcastTypeEnum.IN_PROGRESS)) {
            podcasts = localDataSource.getPodcastsInProgress(page);
        } else {
            podcasts = getPodcasts(podcastType, page);
        }
        podcasts.observe(lifecycleOwner, p -> {
            podcasts.removeObservers(lifecycleOwner);
            response.setValue(ApiResponse.database(p));
        });
    }

    private void fetchPodcastFilesOnNetwork(MutableLiveData<ApiResponse> podcastFilesResponse, String token) {
        networkDataSource.getPodcastFiles(token, new IDataCallback<List<PodcastFile>>() {
            @Override
            public void onSuccess(List<PodcastFile> data) {
                podcastFilesResponse.setValue(ApiResponse.success(data));

                if (!CollectionUtils.isEmpty(data)) {
                    localDataSource.insertPodcastFiles(data.toArray(new PodcastFile[0]));
                }
            }

            @Override
            public void onFailure(Throwable error) {
                podcastFilesResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchPodcastFilesOnLocalDatabase(LifecycleOwner lifecycleOwner, MutableLiveData<ApiResponse> podcastFilesResponse) {
        LiveData<List<PodcastFile>> podcastFilesListLiveData = localDataSource.getPodcastFiles();
        podcastFilesListLiveData.observe(lifecycleOwner, podcastFiles -> {
            podcastFilesListLiveData.removeObservers(lifecycleOwner);
            podcastFilesResponse.setValue(ApiResponse.database(podcastFiles));
        });
    }

    private void fetchAccountPodcastOnNetwork(String token, MutableLiveData<ApiResponse> accountPodcastResponse, String podcastId) {
        networkDataSource.getAccountPodcast(token, podcastId, new IDataCallback<AccountPodcast>() {
            @Override
            public void onSuccess(AccountPodcast data) {
                accountPodcastResponse.setValue(ApiResponse.success(data));

                if (data != null) {
                    localDataSource.insertAccountPodcasts(data);
                }
            }

            @Override
            public void onFailure(Throwable error) {
                accountPodcastResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchAccountPodcastOnLocalDatabase(LifecycleOwner lifecycleOwner, String podcastId,
                                                    MutableLiveData<ApiResponse> accountPodcastResponse) {
        LiveData<AccountPodcast> accountPodcastLiveData = localDataSource.getAccountPodcast(podcastId);
        accountPodcastLiveData.observe(lifecycleOwner, accountPodcast -> {
            accountPodcastLiveData.removeObservers(lifecycleOwner);
            accountPodcastResponse.setValue(ApiResponse.database(accountPodcast));
        });
    }

    private void fetchAccountPodcastsOnNetwork(String token, List<String> podcastIds, MutableLiveData<ApiResponse> accountPodcastsResponse) {
        networkDataSource.getAccountPodcasts(token, podcastIds, new IDataCallback<List<AccountPodcast>>() {
            @Override
            public void onSuccess(List<AccountPodcast> data) {
                accountPodcastsResponse.setValue(ApiResponse.success(data));
                if (!CollectionUtils.isEmpty(data)) {
                    localDataSource.insertAccountPodcasts(data.toArray(new AccountPodcast[0]));
                }
            }

            @Override
            public void onFailure(Throwable error) {
                accountPodcastsResponse.setValue(ApiResponse.error(error));
            }
        });
    }

    private void fetchAccountPodcastsOnLocalDatabase(LifecycleOwner lifecycleOwner, MutableLiveData<ApiResponse> accountPodcastsResponse,
                                                     List<String> podcastIds) {
        LiveData<List<AccountPodcast>> accountPodcastsLiveData = localDataSource.getAccountPodcasts(podcastIds);
        accountPodcastsLiveData.observe(lifecycleOwner, accountPodcasts -> {
            accountPodcastsLiveData.removeObservers(lifecycleOwner);
            accountPodcastsResponse.setValue(ApiResponse.database(accountPodcasts));
        });
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
