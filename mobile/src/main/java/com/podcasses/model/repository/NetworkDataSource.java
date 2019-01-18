package com.podcasses.model.repository;

import com.google.gson.JsonObject;
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

    public void getUserAccount(String username, IDataCallback<JsonObject> callback) {
        Call<JsonObject> call = apiCallInterface.account(username);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

}
