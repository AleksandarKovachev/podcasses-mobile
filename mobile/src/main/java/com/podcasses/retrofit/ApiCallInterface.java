package com.podcasses.retrofit;

import com.podcasses.BuildConfig;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.response.Language;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @GET("/nomenclature/privacies")
    Call<List<Nomenclature>> privacies();

    @GET("/nomenclature/languages")
    Call<List<Language>> languages();

    @POST("/podcast")
    Call<Podcast> podcast(@Header("Authorization") String token, @Body Podcast podcast);

    @POST("/account/podcast")
    Call<AccountPodcast> accountPodcast(@Header("Authorization") String token, @Body AccountPodcastRequest accountPodcastRequest);

    @GET("/account/podcast/{podcastId}")
    Call<AccountPodcast> accountPodcast(@Header("Authorization") String token, @Path("podcastId") String podcastId);

}
