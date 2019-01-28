package com.podcasses.model.repository;

import com.podcasses.model.entity.Account;
import com.podcasses.retrofit.ApiCallInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class NetworkDataSource {

    private ApiCallInterface apiCallInterface;

    public NetworkDataSource(ApiCallInterface apiCallInterface) {
        this.apiCallInterface = apiCallInterface;
    }

    public void getUserAccount(String username, IDataCallback<Account> callback) {
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

    public void getAccountSubscribes(String accountId, IDataCallback<Integer> callback) {
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

}
