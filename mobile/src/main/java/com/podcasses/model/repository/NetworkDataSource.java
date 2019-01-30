package com.podcasses.model.repository;

import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
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

}
