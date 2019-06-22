package com.podcasses.repository;

import android.content.Context;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.request.AccountPodcastType;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.Comment;
import com.podcasses.model.response.Language;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.LogErrorResponseUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
class NetworkDataSource {

    private ApiCallInterface apiCallInterface;
    private Context context;

    NetworkDataSource(ApiCallInterface apiCallInterface, Context context) {
        this.apiCallInterface = apiCallInterface;
        this.context = context;
    }

    void getUserAccount(String username, String id, IDataCallback<Account> callback) {
        Call<Account> call;
        if (username != null) {
            call = apiCallInterface.account(username);
        } else {
            call = apiCallInterface.accountById(id);
        }

        call.enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getAccountSubscribes(String accountId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.accountSubscribes(accountId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getAccountPodcastsCount(String accountId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.accountPodcastsCount(accountId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void checkAccountSubscribe(String token, String accountId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.checkAccountSubscribe("Bearer " + token, accountId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getPodcasts(String podcast, String podcastId, List<String> userId, List<String> ids, int page, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> call = apiCallInterface.podcast(podcast, podcastId, userId, ids, page);
        call.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getPodcasts(String token, String podcastType, Integer likeStatus, int page, IDataCallback<List<Podcast>> callback) {
        Call<List<AccountPodcast>> accountPodcastsCall = apiCallInterface.accountPodcasts("Bearer " + token,
                podcastType, null, likeStatus, page);
        accountPodcastsCall.enqueue(new Callback<List<AccountPodcast>>() {
            @Override
            public void onResponse(Call<List<AccountPodcast>> call, Response<List<AccountPodcast>> response) {
                if (response.isSuccessful() && !CollectionUtils.isEmpty(response.body())) {
                    List<String> podcastIds = new ArrayList<>();
                    for (AccountPodcast accountPodcast : response.body()) {
                        podcastIds.add(accountPodcast.getPodcastId());
                    }
                    Call<List<Podcast>> podcastsCall = apiCallInterface.podcast(null, null, null, podcastIds, page);
                    podcastsCall.enqueue(new Callback<List<Podcast>>() {
                        @Override
                        public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                            if (response.isSuccessful()) {
                                callback.onSuccess(response.body());
                            } else {
                                callback.onSuccess(null);
                                LogErrorResponseUtil.logErrorResponse(response, context);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Podcast>> call, Throwable t) {
                            callback.onFailure(t);
                        }
                    });
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountPodcast>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getPodcastsFromSubscriptions(String token, int page, IDataCallback<List<Podcast>> callback) {
        Call<List<String>> subscriptionsCall = apiCallInterface.getSubscriptions("Bearer " + token);
        subscriptionsCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && !CollectionUtils.isEmpty(response.body())) {
                    Call<List<Podcast>> podcastsCall = apiCallInterface.podcast(null, null, response.body(), null, page);
                    podcastsCall.enqueue(new Callback<List<Podcast>>() {
                        @Override
                        public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                            if (response.isSuccessful()) {
                                callback.onSuccess(response.body());
                            } else {
                                callback.onSuccess(null);
                                LogErrorResponseUtil.logErrorResponse(response, context);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Podcast>> call, Throwable t) {
                            callback.onFailure(t);
                        }
                    });
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getTrendingPodcasts(TrendingFilter trendingFilter, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> call = apiCallInterface.trendingPodcasts(trendingFilter.toQueryMap());
        call.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getAccountPodcast(String token, String podcastId, IDataCallback<AccountPodcast> callback) {
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, podcastId);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getAccountPodcasts(String token, List<String> podcastIds, IDataCallback<List<AccountPodcast>> callback) {
        Call<List<AccountPodcast>> call = apiCallInterface.accountPodcasts("Bearer " + token,
                AccountPodcastType.ID.name(), podcastIds, null, null);
        call.enqueue(new Callback<List<AccountPodcast>>() {
            @Override
            public void onResponse(Call<List<AccountPodcast>> call, Response<List<AccountPodcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountPodcast>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getPodcastFiles(String token, IDataCallback<List<PodcastFile>> callback) {
        Call<List<PodcastFile>> call = apiCallInterface.podcastFiles("Bearer " + token);
        call.enqueue(new Callback<List<PodcastFile>>() {
            @Override
            public void onResponse(Call<List<PodcastFile>> call, Response<List<PodcastFile>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<PodcastFile>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getCategories(IDataCallback<List<Nomenclature>> callback) {
        Call<List<Nomenclature>> call = apiCallInterface.categories();
        call.enqueue(nomenclatureCallback(callback));
    }

    void getLanguages(IDataCallback<List<Language>> callback) {
        Call<List<Language>> call = apiCallInterface.languages();
        call.enqueue(nomenclatureCallback(callback));
    }

    void getPrivacies(IDataCallback<List<Nomenclature>> callback) {
        Call<List<Nomenclature>> call = apiCallInterface.privacies();
        call.enqueue(nomenclatureCallback(callback));
    }

    void getCountries(IDataCallback<List<Nomenclature>> callback) {
        Call<List<Nomenclature>> call = apiCallInterface.countries();
        call.enqueue(nomenclatureCallback(callback));
    }

    void getComments(String podcastId, IDataCallback<List<Comment>> callback) {
        Call<List<Comment>> call = apiCallInterface.getComments(podcastId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getAccountComments(String token, List<String> commentIds, IDataCallback<List<AccountComment>> callback) {
        Call<List<AccountComment>> call = apiCallInterface.accountComments("Bearer " + token, commentIds);
        call.enqueue(new Callback<List<AccountComment>>() {
            @Override
            public void onResponse(Call<List<AccountComment>> call, Response<List<AccountComment>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountComment>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private <T> Callback<List<T>> nomenclatureCallback(IDataCallback<List<T>> callback) {
        return new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(null);
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<T>> call, Throwable t) {
                callback.onFailure(t);
            }
        };
    }
}
