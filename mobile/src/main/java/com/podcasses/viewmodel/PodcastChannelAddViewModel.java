package com.podcasses.viewmodel;

import android.app.ProgressDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.podcasses.R;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.request.PodcastChannelRequest;
import com.podcasses.model.response.ErrorResultResponse;
import com.podcasses.model.response.FieldErrorResponse;
import com.podcasses.model.response.Language;
import com.podcasses.model.response.Nomenclature;
import com.podcasses.model.response.RssFeedResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelAddViewModel extends BaseViewModel implements Observable {

    private PodcastChannelRequest podcastChannelRequest = new PodcastChannelRequest();

    private ApiCallInterface apiCallInterface;

    private Gson gson;

    private ObservableBoolean isSuccessfullyAdded = new ObservableBoolean(false);

    PodcastChannelAddViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface, Gson gson) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
        this.gson = gson;
    }

    public ObservableBoolean getIsSuccessfullyAdded() {
        return isSuccessfullyAdded;
    }

    public LiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public LiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    public PodcastChannelRequest getPodcastChannelRequest() {
        return podcastChannelRequest;
    }

    public void verifyRssFeed(TextInputEditText view, CharSequence rssFeedUrl, TextInputEditText podcastChannelEmail) {
        if (!Strings.isEmptyOrWhitespace(rssFeedUrl.toString()) && Patterns.WEB_URL.matcher(rssFeedUrl.toString()).matches()) {
            ProgressDialog progressDialog = DialogUtil.getProgressDialog(view.getContext());
            progressDialog.show();

            Call<RssFeedResponse> call = apiCallInterface.verifyRssFeed(rssFeedUrl.toString());
            call.enqueue(new Callback<RssFeedResponse>() {
                @Override
                public void onResponse(Call<RssFeedResponse> call, Response<RssFeedResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        RssFeedResponse rssFeedResponse = response.body();
                        podcastChannelRequest.setAuthor(rssFeedResponse.getAuthor());
                        podcastChannelRequest.setTitle(rssFeedResponse.getTitle());
                        podcastChannelRequest.setEmail(rssFeedResponse.getEmail());
                        podcastChannelRequest.setDescription(rssFeedResponse.getDescription());
                        podcastChannelRequest.setImageUrl(rssFeedResponse.getImageUrl());
                        Toasty.success(view.getContext(), view.getContext().getString(R.string.podcast_channel_add_rss_feed_verify), Toast.LENGTH_SHORT, true).show();
                        podcastChannelEmail.setEnabled(false);
                    } else {
                        try {
                            ErrorResultResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResultResponse.class);
                            if (errorResponse != null && errorResponse.getError() != null) {
                                view.setError(errorResponse.getError().getDetails().get(0));
                            }
                        } catch (IOException e) {
                            Log.e(this.getClass().getName(), "onResponse: ", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<RssFeedResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    Toasty.error(view.getContext(), view.getContext().getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                }
            });
        }
    }

    public void onPodcastChannelAddClick(View view, String token) {
        if (Strings.isEmptyOrWhitespace(token)) {
            return;
        }

        ProgressDialog progressDialog = DialogUtil.getProgressDialog(view.getContext());
        progressDialog.show();
        Call<PodcastChannel> call = apiCallInterface.podcastChannel("Bearer " + token, podcastChannelRequest);
        call.enqueue(new Callback<PodcastChannel>() {
            @Override
            public void onResponse(Call<PodcastChannel> call, Response<PodcastChannel> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toasty.success(view.getContext(), view.getContext().getString(R.string.podcast_channel_successfully_added), Toast.LENGTH_SHORT, true).show();
                    repository.insertPodcastChannel(response.body());
                    isSuccessfullyAdded.set(true);
                } else {
                    try {
                        ErrorResultResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResultResponse.class);

                        StringBuilder stringBuilder = new StringBuilder();
                        for (FieldErrorResponse fieldError : errorResponse.getError().getFieldErrors()) {
                            stringBuilder.append(fieldError.getError());
                        }
                        Toasty.error(view.getContext(), stringBuilder.toString(), Toast.LENGTH_SHORT, true).show();
                    } catch (IOException e) {
                        Log.e(this.getClass().getName(), "onResponse: ", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<PodcastChannel> call, Throwable t) {
                progressDialog.dismiss();
                Toasty.error(view.getContext(), view.getContext().getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
    }

}
