package com.podcasses.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiCallInterface {

    String BASE_URL = "http://192.168.0.102:9090";

    @GET("/account/{username}")
    Call<JsonObject> account(@Path("username") String username);

}
