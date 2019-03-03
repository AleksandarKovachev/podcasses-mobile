package com.podcasses.model.repository;

import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.Language;
import com.podcasses.retrofit.ApiCallInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
class NetworkDataSource {

    private ApiCallInterface apiCallInterface;

    NetworkDataSource(ApiCallInterface apiCallInterface) {
        this.apiCallInterface = apiCallInterface;
    }

    void getUserAccount(String username, IDataCallback<Account> callback) {
        Call<Account> call = apiCallInterface.account(username);
        call.enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                callback.onSuccess(response.body());
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
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getPodcasts(String podcast, String podcastId, String userId, IDataCallback<List<Podcast>> callback) {
        Call<List<Podcast>> call = apiCallInterface.podcast(podcast, podcastId, userId);
        call.enqueue(new Callback<List<Podcast>>() {
            @Override
            public void onResponse(Call<List<Podcast>> call, Response<List<Podcast>> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Podcast>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    void getAccountPodcast(String token, String podcastId, IDataCallback<AccountPodcast> callback){
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, podcastId);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
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

    private <T> Callback<List<T>> nomenclatureCallback(IDataCallback<List<T>> callback) {
        return new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<T>> call, Throwable t) {
                callback.onFailure(t);
            }
        };
    }
}
