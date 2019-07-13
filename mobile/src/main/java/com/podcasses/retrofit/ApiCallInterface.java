package com.podcasses.retrofit;

import com.podcasses.BuildConfig;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.request.AccountRequest;
import com.podcasses.model.request.CommentReportRequest;
import com.podcasses.model.request.CommentRequest;
import com.podcasses.model.request.PodcastReportRequest;
import com.podcasses.model.request.UserRegistrationRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.Comment;
import com.podcasses.model.response.Language;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiCallInterface {

    String API_GATEWAY_URL = BuildConfig.API_GATEWAY_URL;

    @GET("/keycloak/{username}")
    Call<Account> account(@Path("username") String username);

    @GET("/keycloak/id/{id}")
    Call<Account> accountById(@Path("id") String id);

    @POST("/keycloak/account")
    Call<Account> account(@Header("Authorization") String token, @Body AccountRequest accountRequest);

    @GET("/keycloak/account")
    Call<List<Account>> accounts(@Query("name") String name, @Query("id") List<String> ids);

    @POST("/keycloak/registration")
    Call<Void> registration(@Body UserRegistrationRequest registrationRequest);

    @GET("/account/subscribes/{accountId}")
    Call<Integer> accountSubscribes(@Path("accountId") String accountId);

    @GET("/podcast/count")
    Call<Integer> accountPodcastsCount(@Header("Authorization") String token, @Query("userId") String userId);

    @GET("/account/subscribe/{accountId}")
    Call<Integer> checkAccountSubscribe(@Header("Authorization") String token, @Path("accountId") String accountId);

    @POST("/account/subscribe/{accountId}")
    Call<Integer> accountSubscribe(@Header("Authorization") String token, @Path("accountId") String accountId);

    @GET("/podcast")
    Call<List<Podcast>> podcast(
            @Query(value = "podcast", encoded = true) String podcast,
            @Query(value = "podcastId", encoded = true) String podcastId,
            @Query(value = "userId", encoded = true) List<String> userId,
            @Query(value = "id", encoded = true) List<String> id,
            @Query(value = "page") Integer page);

    @GET("/nomenclature/categories")
    Call<List<Nomenclature>> categories();

    @GET("/nomenclature/privacies")
    Call<List<Nomenclature>> privacies();

    @GET("/nomenclature/languages")
    Call<List<Language>> languages();

    @GET("/nomenclature/locales")
    Call<List<Language>> locales();

    @GET("/nomenclature/countries")
    Call<List<Nomenclature>> countries();

    @POST("/podcast")
    Call<Podcast> podcast(@Header("Authorization") String token, @Body Podcast podcast);

    @POST("/account/podcast")
    Call<AccountPodcast> accountPodcast(@Header("Authorization") String token, @Body AccountPodcastRequest accountPodcastRequest);

    @GET("/account/podcast/{podcastId}")
    Call<AccountPodcast> accountPodcast(@Header("Authorization") String token, @Path("podcastId") String podcastId);

    @GET("/account/podcast")
    Call<List<AccountPodcast>> accountPodcasts(@Header("Authorization") String token,
                                               @Query("type") String type,
                                               @Query("id") List<String> ids,
                                               @Query("likeStatus") Integer likeStatus,
                                               @Query("page") Integer page);

    @GET("/podcast/files")
    Call<List<PodcastFile>> podcastFiles(@Header("Authorization") String token);

    @DELETE("/podcast/file/{id}")
    Call<Void> deletePodcastFile(@Header("Authorization") String token, @Path("id") String id);

    @GET("/podcast/comment/{podcastId}")
    Call<List<Comment>> getComments(@Path("podcastId") String podcastId);

    @POST("/account/comment")
    Call<AccountComment> accountComment(@Header("Authorization") String token, @Body AccountCommentRequest accountCommentRequest);

    @GET("/account/comment/id")
    Call<List<AccountComment>> accountComments(@Header("Authorization") String token, @Query("id") List<String> ids);

    @POST("/podcast/comment")
    Call<Comment> accountComment(@Header("Authorization") String token, @Body CommentRequest commentRequest);

    @POST("/contact/feedback/comment")
    Call<Void> commentReport(@Header("Authorization") String token, @Body CommentReportRequest commentReportRequest);

    @POST("/contact/feedback/podcast")
    Call<Void> podcastReport(@Header("Authorization") String token, @Body PodcastReportRequest podcastReportRequest);

    @GET("/podcast/trending")
    Call<List<Podcast>> trendingPodcasts(@QueryMap Map<String, Object> query);

    @POST("/podcast/view/{podcastId}")
    Call<Void> podcastView(@Path("podcastId") String podcastId);

    @GET("/podcast/feed/verify")
    Call<String> rssFeedVerify(@Query("url") String url);

    @POST("/podcast/feed/sync")
    Call<Void> rssFeedSync(@Header("Authorization") String token);

    @GET("/account/subscribes")
    Call<List<String>> getSubscriptions(@Header("Authorization") String token);

    @GET("/contact/termsofservice")
    Call<Map<String, String>> termsOfService();

    @GET("/contact/privacypolicy")
    Call<Map<String, String>> privacyPolicy();

}
