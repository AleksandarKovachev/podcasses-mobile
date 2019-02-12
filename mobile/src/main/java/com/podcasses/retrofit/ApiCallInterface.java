package com.podcasses.retrofit;

import com.podcasses.BuildConfig;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiCallInterface {

    String API_GATEWAY_URL = BuildConfig.API_GATEWAY_URL;

    @GET("/account/{username}")
    Call<Account> account(@Path("username") String username);

    @GET("/account/subscribes/{accountId}")
    Call<Integer> accountSubscribes(@Path("accountId") String accountId);

    @GET("/podcast")
    Call<List<Podcast>> podcast(
            @Query(value = "podcast", encoded = true) String podcast,
            @Query(value = "podcastId", encoded = true) String podcastId,
            @Query(value = "userId", encoded = true) String userId);

    @GET("/nomenclature/categories")
    Call<List<Nomenclature>> categories();

    @GET("/nomenclature/languages")
    Call<List<Nomenclature>> languages();

    @GET("/nomenclature/privacies")
    Call<List<Nomenclature>> privacies();

    @POST("/podcast")
    Call<BaseResponse> podcast(@Field("podcast") Podcast podcast);

}
