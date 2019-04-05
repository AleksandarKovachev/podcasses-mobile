package com.podcasses.retrofit;

import com.podcasses.BuildConfig;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.request.CommentRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.Comment;
import com.podcasses.model.response.Language;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("/account/subscribe/{accountId}")
    Call<Integer> checkAccountSubscribe(@Header("Authorization") String token, @Path("accountId") String accountId);

    @POST("/account/subscribe/{accountId}")
    Call<Integer> accountSubscribe(@Header("Authorization") String token, @Path("accountId") String accountId);

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

    @GET("/podcast/files")
    Call<List<PodcastFile>> podcastFiles(@Header("Authorization") String token);

    @DELETE("/podcast/file/{id}")
    Call<Void> deletePodcastFile(@Header("Authorization") String token, @Path("id") String id);

    @GET("/podcast/comment/{podcastId}")
    Call<List<Comment>> getComments(@Path("podcastId") String podcastId);

    @GET("/account/id")
    Call<List<Account>> getAccounts(@Query("id") List<String> ids);

    @POST("/account/comment")
    Call<AccountComment> accountComment(@Header("Authorization") String token, @Body AccountCommentRequest accountCommentRequest);

    @GET("/account/comment/id")
    Call<List<AccountComment>> accountComments(@Header("Authorization") String token, @Query("id") List<String> ids);

    @POST("/podcast/comment")
    Call<Comment> accountComment(@Header("Authorization") String token, @Body CommentRequest commentRequest);

}
