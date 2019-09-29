package com.podcasses.retrofit;

import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.AccountListRequest;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.request.CommentReportRequest;
import com.podcasses.model.request.CommentRequest;
import com.podcasses.model.request.PodcastChannelRequest;
import com.podcasses.model.request.PodcastReportRequest;
import com.podcasses.model.request.UserRegistrationRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.AccountList;
import com.podcasses.model.response.Comment;
import com.podcasses.model.response.Language;
import com.podcasses.model.response.Nomenclature;
import com.podcasses.model.response.RssFeedResponse;

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

    @GET("/api-gateway/keycloak/{username}")
    Call<Account> account(@Path("username") String username);

    @GET("/api-gateway/keycloak/id/{id}")
    Call<Account> accountById(@Path("id") String id);

    @POST("/api-gateway/keycloak/registration")
    Call<Void> registration(@Body UserRegistrationRequest registrationRequest);

    @GET("/api-gateway/podcast/podcastChannel/views/{id}")
    Call<Integer> podcastChannelViews(@Path("id") String id);

    @GET("/api-gateway/account/subscribes/{id}")
    Call<Integer> podcastChannelSubscribes(@Path("id") String id);

    @POST("/api-gateway/account/subscribe/{channelId}")
    Call<Integer> podcastChannelSubscribe(@Header("Authorization") String token,
                                          @Path("channelId") String channelId,
                                          @Query(value = "deviceId") String deviceId);

    @GET("/api-gateway/podcast/count")
    Call<Integer> podcastChannelEpisodes(@Header("Authorization") String token, @Query("channelId") String channelId);

    @GET("/api-gateway/account/subscribe/{id}")
    Call<Integer> checkPodcastChannelSubscribe(@Header("Authorization") String token, @Path("id") String id);

    @GET("/api-gateway/podcast/feed/verify")
    Call<RssFeedResponse> verifyRssFeed(@Query(value = "url", encoded = true) String url);

    @POST("/api-gateway/podcast/podcastChannel")
    Call<PodcastChannel> podcastChannel(@Header("Authorization") String token, @Body PodcastChannelRequest request);

    @GET("/api-gateway/podcast/podcastChannels")
    Call<List<PodcastChannel>> podcastChannels(@Header("Authorization") String token,
                                               @Query(value = "id", encoded = true) List<String> id,
                                               @Query(value = "userId", encoded = true) String userId,
                                               @Query(value = "name", encoded = true) String name);

    @GET("/api-gateway/podcast/podcastChannel/{id}")
    Call<PodcastChannel> podcastChannel(@Path("id") String id);

    @GET("/api-gateway/podcast")
    Call<List<Podcast>> podcast(
            @Query(value = "podcast", encoded = true) String podcast,
            @Query(value = "podcastId", encoded = true) String podcastId,
            @Query(value = "channelId", encoded = true) List<String> channelId,
            @Query(value = "id", encoded = true) List<String> id,
            @Query(value = "page") Integer page);

    @GET("/api-gateway/nomenclature/categories")
    Call<List<Nomenclature>> categories();

    @GET("/api-gateway/nomenclature/privacies")
    Call<List<Nomenclature>> privacies();

    @GET("/api-gateway/nomenclature/languages")
    Call<List<Language>> languages();

    @GET("/api-gateway/nomenclature/locales")
    Call<List<Language>> locales();

    @GET("/api-gateway/nomenclature/countries")
    Call<List<Nomenclature>> countries();

    @POST("/api-gateway/podcast")
    Call<Podcast> podcast(@Header("Authorization") String token, @Body Podcast podcast);

    @POST("/api-gateway/account/podcast")
    Call<AccountPodcast> accountPodcast(@Header("Authorization") String token, @Body AccountPodcastRequest accountPodcastRequest);

    @GET("/api-gateway/account/podcast/{podcastId}")
    Call<AccountPodcast> accountPodcast(@Header("Authorization") String token, @Path("podcastId") String podcastId);

    @GET("/api-gateway/account/podcast")
    Call<List<AccountPodcast>> accountPodcasts(@Header("Authorization") String token,
                                               @Query("type") String type,
                                               @Query("id") List<String> ids,
                                               @Query("likeStatus") Integer likeStatus,
                                               @Query("page") Integer page);

    @GET("/api-gateway/podcast/files")
    Call<List<PodcastFile>> podcastFiles(@Header("Authorization") String token);

    @DELETE("/api-gateway/podcast/file/{id}")
    Call<Void> deletePodcastFile(@Header("Authorization") String token, @Path("id") String id);

    @GET("/api-gateway/podcast/comment/{podcastId}")
    Call<List<Comment>> getComments(@Path("podcastId") String podcastId);

    @POST("/api-gateway/account/comment")
    Call<AccountComment> accountComment(@Header("Authorization") String token, @Body AccountCommentRequest accountCommentRequest);

    @GET("/api-gateway/account/comment/id")
    Call<List<AccountComment>> accountComments(@Header("Authorization") String token, @Query("id") List<String> ids);

    @POST("/api-gateway/podcast/comment")
    Call<Comment> accountComment(@Header("Authorization") String token, @Body CommentRequest commentRequest);

    @POST("/api-gateway/contact/feedback/comment")
    Call<Void> commentReport(@Header("Authorization") String token, @Body CommentReportRequest commentReportRequest);

    @POST("/api-gateway/contact/feedback/podcast")
    Call<Void> podcastReport(@Header("Authorization") String token, @Body PodcastReportRequest podcastReportRequest);

    @GET("/api-gateway/podcast/trending")
    Call<List<Podcast>> trendingPodcasts(@QueryMap Map<String, Object> query);

    @POST("/api-gateway/podcast/view/{podcastId}")
    Call<Void> podcastView(@Path("podcastId") String podcastId);

    @GET("/api-gateway/account/subscribes")
    Call<List<String>> getSubscriptions(@Header("Authorization") String token);

    @GET("/api-gateway/contact/termsofservice")
    Call<Map<String, String>> termsOfService();

    @GET("/api-gateway/contact/privacypolicy")
    Call<Map<String, String>> privacyPolicy();

    @GET("/api-gateway/account/list")
    Call<List<AccountList>> getAccountLists(@Header("Authorization") String token);

    @GET("/api-gateway/account/list/{podcastId}")
    Call<List<AccountList>> getAccountLists(@Header("Authorization") String token, @Path("podcastId") String podcastId);

    @POST("/api-gateway/account/list")
    Call<AccountList> accountList(@Header("Authorization") String token, @Body AccountListRequest accountListRequest);

}
