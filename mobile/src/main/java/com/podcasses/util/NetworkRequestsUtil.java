package com.podcasses.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.button.MaterialButton;
import com.podcasses.R;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.request.AccountCommentRequest;
import com.podcasses.model.request.AccountPodcastRequest;
import com.podcasses.model.request.AccountRequest;
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

    public static void rssFeedVerify(ApiCallInterface apiCallInterface, String rssFeed,
                                     AppCompatTextView rssFeedEmail,
                                     ContentLoadingProgressBar progressBar,
                                     MaterialButton submitButton,
                                     AccountRequest accountRequest) {
        rssFeedEmail.setText("");
        submitButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        submitButton.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
        Call<String> call = apiCallInterface.rssFeedVerify(rssFeed);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    rssFeedEmail.setText(response.body());
                    submitButton.setClickable(true);
                    submitButton.getBackground().setColorFilter(null);
                    accountRequest.setRssFeedEmail(response.body());
                } else {
                    rssFeedEmail.setText(R.string.error_invalid_rss_feed);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                rssFeedEmail.setText(R.string.error_invalid_rss_feed);
            }
        });
    }

    public static LiveData<Account> sendUpdateAccountRequest(ApiCallInterface apiCallInterface, String token, AccountRequest accountRequest, Context context) {
        MutableLiveData<Account> accountResponse = new MutableLiveData<>();
        ProgressDialog progressDialog = DialogUtil.getProgressDialog(context);
        progressDialog.show();
        if (ConnectivityUtil.checkInternetConnection(context)) {
            Call<Account> call = apiCallInterface.account("Bearer " + token, accountRequest);
            call.enqueue(new Callback<Account>() {
                @Override
                public void onResponse(Call<Account> call, Response<Account> response) {
                    progressDialog.hide();
                    if (response.isSuccessful()) {
                        Toasty.success(context, context.getString(R.string.successful_response), Toast.LENGTH_SHORT, true).show();
                        accountResponse.setValue(response.body());
                    } else {
                        LogErrorResponseUtil.logErrorResponse(response, context);
                    }
                    accountResponse.setValue(null);
                }

                @Override
                public void onFailure(Call<Account> call, Throwable t) {
                    LogErrorResponseUtil.logFailure(t, context);
                    progressDialog.hide();
                    accountResponse.setValue(null);
                }
            });
        } else {
            progressDialog.hide();
            Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
        }
        return accountResponse;
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
