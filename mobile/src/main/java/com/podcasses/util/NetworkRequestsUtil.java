package com.podcasses.util;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.R;
import com.podcasses.constant.LikeStatus;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Comment;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class NetworkRequestsUtil {

    public static MutableLiveData<AccountPodcast> sendMarkAsPlayedRequest(MainDataRepository repository, Context context,
                                                                          ApiCallInterface apiCallInterface, Podcast podcast, String token) {
        MutableLiveData<AccountPodcast> accountPodcast = new MutableLiveData<>();
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setPodcastId(podcast.getId());
        accountPodcastRequest.setMarkAsPlayed(podcast.isMarkAsPlayed() ? 0 : 1);
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, accountPodcastRequest);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountPodcast.setValue(response.body());
                    podcast.setMarkAsPlayed(response.body().getMarkAsPlayed() == 1);
                    repository.saveAccountPodcast(response.body());
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                accountPodcast.setValue(null);
                Toasty.error(context, context.getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
        return accountPodcast;
    }

    public static void sendAccountCommentRequest(Context context, ApiCallInterface apiCallInterface, String token, Comment comment, AccountCommentRequest accountCommentRequest) {
        Call<AccountComment> call = apiCallInterface.accountComment("Bearer " + token, accountCommentRequest);
        call.enqueue(new Callback<AccountComment>() {
            @Override
            public void onResponse(Call<AccountComment> call, Response<AccountComment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toasty.success(context, context.getString(R.string.successful_response), Toast.LENGTH_SHORT, true).show();
                    int previousLikeStatus = getPreviousLikeStatus(comment);
                    LikeStatusUtil.updateLikeStatus(comment, response.body().getLikeStatus(), previousLikeStatus);
                    comment.setLiked(response.body().getLikeStatus() == LikeStatus.LIKE.getValue());
                    comment.setDisliked(response.body().getLikeStatus() == LikeStatus.DISLIKE.getValue());
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<AccountComment> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, context);
            }
        });
    }

    public static LiveData<ApiResponse> sendPodcastViewRequest(Context context, ApiCallInterface apiCallInterface,
                                                               String token, String podcastId, long timeIndex, boolean isNewView) {
        MutableLiveData<ApiResponse> accountPodcastResponse = new MutableLiveData<>(ApiResponse.loading());
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setTimeIndex(timeIndex);
        accountPodcastRequest.setPodcastId(podcastId);
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, accountPodcastRequest);
        call.enqueue(new Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountPodcastResponse.setValue(ApiResponse.success(response.body(), response.raw().request().url().toString()));
                } else {
                    accountPodcastResponse.setValue(ApiResponse.error(null, response.raw().request().url().toString()));
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                accountPodcastResponse.setValue(ApiResponse.error(t, call.request().url().toString()));
                LogErrorResponseUtil.logFailure(t, context);
            }
        });

        if (isNewView) {
            sendPodcastViewRequest(apiCallInterface, podcastId);
        }
        return accountPodcastResponse;
    }

    public static void sendPodcastViewRequest(ApiCallInterface apiCallInterface, String podcastId) {
        apiCallInterface.podcastView(podcastId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private static int getPreviousLikeStatus(Comment comment) {
        int previousLikeStatus = LikeStatus.DEFAULT.getValue();
        if (comment.isLiked()) {
            previousLikeStatus = LikeStatus.LIKE.getValue();
        } else if (comment.isDisliked()) {
            previousLikeStatus = LikeStatus.DISLIKE.getValue();
        }
        return previousLikeStatus;
    }

}
