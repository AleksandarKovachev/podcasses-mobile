package com.podcasses.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.podcasses.R;
import com.podcasses.model.request.CommentReportRequest;
import com.podcasses.model.request.PodcastReportRequest;
import com.podcasses.retrofit.ApiCallInterface;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class DialogUtil {

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static void createReportDialog(Context context, String id, ApiCallInterface apiCallInterface, String token, boolean isPodcast) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.report));

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_report, null);
        builder.setView(view);

        EditText input = view.findViewById(R.id.report);

        builder.setPositiveButton(context.getString(R.string.send), (dialog, which) ->
                sendReport(context, apiCallInterface, token, id, input.getText().toString(), isPodcast));
        builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private static void sendReport(Context context, ApiCallInterface apiCallInterface, String token, String id, String report, boolean isPodcast) {
        if (isPodcast) {
            PodcastReportRequest podcastReportRequest = new PodcastReportRequest();
            podcastReportRequest.setPodcastId(id);
            podcastReportRequest.setReport(report);
            apiCallInterface.podcastReport("Bearer " + token, podcastReportRequest).enqueue(reportCallback(context));
        } else {
            CommentReportRequest commentReportRequest = new CommentReportRequest();
            commentReportRequest.setCommentId(id);
            commentReportRequest.setReport(report);
            apiCallInterface.commentReport("Bearer " + token, commentReportRequest).enqueue(reportCallback(context));
        }
    }

    private static Callback<Void> reportCallback(Context context) {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toasty.success(context, context.getString(R.string.successful_report), Toast.LENGTH_SHORT, true).show();
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, context);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, context);
            }
        };
    }

}
