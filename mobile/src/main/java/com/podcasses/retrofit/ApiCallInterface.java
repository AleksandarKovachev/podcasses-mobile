package com.podcasses.retrofit;

import com.google.gson.JsonObject;
import com.podcasses.BuildConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiCallInterface {

    String API_GATEWAY_URL = BuildConfig.API_GATEWAY_URL;

    @GET("/account/{username}")
    Call<JsonObject> account(@Path("username") String username);

}
