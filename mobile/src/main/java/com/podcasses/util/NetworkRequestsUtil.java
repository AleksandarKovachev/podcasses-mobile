package com.podcasses.util;

import android.content.Context;
import android.view.MenuItem;
import android.widget.Toast;

import com.podcasses.R;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.response.AccountComment;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.ApiCallInterface;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class NetworkRequestsUtil {

    public static void sendMarkAsPlayedRequest(MenuItem item, Context context, ApiCallInterface apiCallInterface, Podcast podcast, String token) {
        AccountPodcastRequest accountPodcastRequest = new AccountPodcastRequest();
        accountPodcastRequest.setPodcastId(podcast.getId());
        accountPodcastRequest.setMarkAsPlayed(podcast.isMarkAsPlayed() ? 0 : 1);
        Call<AccountPodcast> call = apiCallInterface.accountPodcast("Bearer " + token, accountPodcastRequest);
        call.enqueue(new retrofit2.Callback<AccountPodcast>() {
            @Override
            public void onResponse(Call<AccountPodcast> call, Response<AccountPodcast> response) {
                if (response.isSuccessful() && response.body() != null) {
                    item.setChecked(response.body().getMarkAsPlayed() == 1);
                    podcast.setMarkAsPlayed(response.body().getMarkAsPlayed() == 1);
                }
            }

            @Override
            public void onFailure(Call<AccountPodcast> call, Throwable t) {
                Toasty.error(context, context.getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
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

    public static void sendPodcastViewRequest(ApiCallInterface apiCallInterface, SharedPreferencesManager sharedPreferencesManager, String podcastId) {
        Call<Void> call = apiCallInterface.podcastView(podcastId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                sharedPreferencesManager.setViewedPodcast(podcastId);
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
