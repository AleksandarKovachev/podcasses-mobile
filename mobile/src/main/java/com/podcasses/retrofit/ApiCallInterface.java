package com.podcasses.retrofit;

import com.podcasses.BuildConfig;
import com.podcasses.model.entity.Account;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiCallInterface {

    String API_GATEWAY_URL = BuildConfig.API_GATEWAY_URL;

    @GET("/account/{username}")
    Call<Account> account(@Path("username") String username);

    @GET("/account/subscribes/{accountId}")
    Call<Integer> accountSubscribes(@Path("accountId") String accountId);

}
