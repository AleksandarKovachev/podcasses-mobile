package com.podcasses.repository;

import android.content.Context;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.request.AccountPodcastType;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.AccountList;
import com.podcasses.model.response.Comment;
import com.podcasses.model.response.Language;
import com.podcasses.model.response.Nomenclature;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.LogErrorResponseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getSubscribedPodcastChannels(String token, IDataCallback<List<PodcastChannel>> callback) {
        Call<List<String>> call = apiCallInterface.getSubscriptions("Bearer " + token);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    if (CollectionUtils.isEmpty(response.body())) {
                        callback.onSuccess(null, response.raw().request().url().toString());
                        return;
                    }
                    apiCallInterface.podcastChannels("Bearer " + token, response.body(), null, null).enqueue(new Callback<List<PodcastChannel>>() {
                        @Override
                        public void onResponse(Call<List<PodcastChannel>> call, Response<List<PodcastChannel>> response) {
                            if (response.isSuccessful()) {
                                callback.onSuccess(response.body(), response.raw().request().url().toString());
                            } else {
                                callback.onSuccess(null, response.raw().request().url().toString());
                                LogErrorResponseUtil.logErrorResponse(response, context);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<PodcastChannel>> call, Throwable t) {
                            callback.onFailure(t, call.request().url().toString());
                        }
                    });
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getPodcastsFromList(String token, Long id, int page, IDataCallback<List<Podcast>> callback) {
        Call<List<String>> call = apiCallInterface.getPodcastsByListId("Bearer " + token, id);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    if (CollectionUtils.isEmpty(response.body())) {
                        callback.onSuccess(null, response.raw().request().url().toString());
                        return;
                    }
                    apiCallInterface.podcast(null, null, null, response.body(), page).enqueue(new Callback<List<Podcast>>() {
                        @Override
                        public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                            if (response.isSuccessful()) {
                                callback.onSuccess(response.body(), response.raw().request().url().toString());
                            } else {
                                callback.onSuccess(null, response.raw().request().url().toString());
                                LogErrorResponseUtil.logErrorResponse(response, context);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Podcast>> call, Throwable t) {
                            callback.onFailure(t, call.request().url().toString());
                        }
                    });
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getPodcastChannels(String token, String userId, String name, IDataCallback<List<PodcastChannel>> callback) {
        Call<List<PodcastChannel>> call = apiCallInterface.podcastChannels(token != null ? "Bearer " + token : null, null, userId, name);
        call.enqueue(new Callback<List<PodcastChannel>>() {
            @Override
            public void onResponse(Call<List<PodcastChannel>> call, Response<List<PodcastChannel>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<PodcastChannel>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getPodcastChannel(String id, IDataCallback<PodcastChannel> callback) {
        Call<PodcastChannel> call = apiCallInterface.podcastChannel(id);
        call.enqueue(new Callback<PodcastChannel>() {
            @Override
            public void onResponse(Call<PodcastChannel> call, Response<PodcastChannel> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<PodcastChannel> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void podcastChannelEpisodes(String token, String channelId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.podcastChannelEpisodes(Strings.isEmptyOrWhitespace(token) ? null : "Bearer " + token, channelId);
        processIntegerCallback(callback, call);
    }

    void podcastChannelSubscribes(String channelId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.podcastChannelSubscribes(channelId);
        processIntegerCallback(callback, call);
    }

    void podcastChannelViews(String channelId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.podcastChannelViews(channelId);
        processIntegerCallback(callback, call);
    }

    void checkPodcastChannelSubscribe(String token, String channelId, IDataCallback<Integer> callback) {
        Call<Integer> call = apiCallInterface.checkPodcastChannelSubscribe("Bearer " + token, channelId);
        processIntegerCallback(callback, call);
    }

    void getPodcasts(String podcast, String podcastId, List<String> channelId, List<String> ids, int page, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> call = apiCallInterface.podcast(podcast, podcastId, channelId, ids, page);
        call.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
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
                                callback.onSuccess(response.body(), response.raw().request().url().toString());
                            } else {
                                callback.onSuccess(null, response.raw().request().url().toString());
                                LogErrorResponseUtil.logErrorResponse(response, context);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Podcast>> call, Throwable t) {
                            callback.onFailure(t, call.request().url().toString());
                        }
                    });
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountPodcast>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getPodcastsFromSubscriptions(String token, int page, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> podcastsCall = apiCallInterface.podcastSubscription("Bearer " + token, page);
        podcastsCall.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getTrendingPodcasts(TrendingFilter trendingFilter, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> call = apiCallInterface.trendingPodcasts(trendingFilter.toQueryMap());
        call.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getNewPodcasts(String token, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> call = apiCallInterface.newPodcasts(Strings.isEmptyOrWhitespace(token) ? null : "Bearer " + token);
        call.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getAccountPodcast(String token, String podcastId, IDataCallback<AccountPodcast> callback) {
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, podcastId);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
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
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountPodcast>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getPodcastFiles(String token, IDataCallback<List<PodcastFile>> callback) {
        Call<List<PodcastFile>> call = apiCallInterface.podcastFiles("Bearer " + token);
        call.enqueue(new Callback<List<PodcastFile>>() {
            @Override
            public void onResponse(Call<List<PodcastFile>> call, Response<List<PodcastFile>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<PodcastFile>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
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

    void getLocales(IDataCallback<List<Language>> callback) {
        Call<List<Language>> call = apiCallInterface.locales();
        call.enqueue(nomenclatureCallback(callback));
    }

    void getTermsOfService(IDataCallback<String> callback) {
        Call<Map<String, String>> call = apiCallInterface.termsOfService();
        call.enqueue(agreementCallback(callback));
    }

    void getPrivacyPolicy(IDataCallback<String> callback) {
        Call<Map<String, String>> call = apiCallInterface.privacyPolicy();
        call.enqueue(agreementCallback(callback));
    }

    void getComments(String podcastId, IDataCallback<List<Comment>> callback) {
        Call<List<Comment>> call = apiCallInterface.getComments(podcastId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getAccountComments(String token, List<String> commentIds, IDataCallback<List<AccountComment>> callback) {
        Call<List<AccountComment>> call = apiCallInterface.accountComments("Bearer " + token, commentIds);
        call.enqueue(new Callback<List<AccountComment>>() {
            @Override
            public void onResponse(Call<List<AccountComment>> call, Response<List<AccountComment>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountComment>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void syncAccountPodcast(String token, AccountPodcast accountPodcast, IDataCallback<AccountPodcast> callback) {
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setPodcastId(accountPodcast.getPodcastId());
        accountPodcastRequest.setLikeStatus(accountPodcast.getLikeStatus());
        accountPodcastRequest.setMarkAsPlayed(accountPodcast.getMarkAsPlayed());
        accountPodcastRequest.setTimeIndex(accountPodcast.getTimeIndex());
        if (accountPodcast.getViewTimestamp() != null) {
            accountPodcastRequest.setViewTimestamp(accountPodcast.getViewTimestamp().getTime());
        }
        if (accountPodcast.getMarkAsPlayedTimestamp() != null) {
            accountPodcastRequest.setMarkAsPlayedTimestamp(accountPodcast.getMarkAsPlayedTimestamp().getTime());
        }
        if (accountPodcast.getCreatedTimestamp() != null) {
            accountPodcastRequest.setCreatedTimestamp(accountPodcast.getCreatedTimestamp().getTime());
        }
        if (accountPodcast.getLikeTimestamp() != null) {
            accountPodcastRequest.setLikeTimestamp(accountPodcast.getLikeTimestamp().getTime());
        }
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, accountPodcastRequest);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                callback.onSuccess(response.body(), response.raw().request().url().toString());
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

    void getAccountLists(String token, IDataCallback<List<AccountList>> callback) {
        Call<List<AccountList>> call = apiCallInterface.getAccountLists("Bearer " + token);
        call.enqueue(getAccountListsCallback(callback));
    }

    void getAccountLists(String token, String podcastId, IDataCallback<List<AccountList>> callback) {
        Call<List<AccountList>> call = apiCallInterface.getAccountLists("Bearer " + token, podcastId);
        call.enqueue(getAccountListsCallback(callback));
    }

    private Callback<List<AccountList>> getAccountListsCallback(IDataCallback<List<AccountList>> callback) {
        return new Callback<List<AccountList>>() {
            @Override
            public void onResponse(Call<List<AccountList>> call, Response<List<AccountList>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<AccountList>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        };
    }

    private Callback<Map<String, String>> agreementCallback(IDataCallback<String> callback) {
        return new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().get("agreement"), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        };
    }

    private <T> Callback<List<T>> nomenclatureCallback(IDataCallback<List<T>> callback) {
        return new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<List<T>> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        };
    }

    private void processIntegerCallback(IDataCallback<Integer> callback, Call<Integer> call) {
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body(), response.raw().request().url().toString());
                } else {
                    callback.onSuccess(null, response.raw().request().url().toString());
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure(t, call.request().url().toString());
            }
        });
    }

}
