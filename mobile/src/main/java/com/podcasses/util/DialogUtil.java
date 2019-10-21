package com.podcasses.util;

import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.databinding.DialogTrendingFilterBinding;
import com.podcasses.model.request.CommentReportRequest;
import com.podcasses.model.request.PodcastReportRequest;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.request.TrendingReport;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.view.AuthenticatorActivity;
import com.podcasses.viewmodel.HomeViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class DialogUtil {

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static void createReportDialog(Context context, String id, ApiCallInterface apiCallInterface, String token, boolean isPodcast) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.report));

        View view;
        if (Strings.isEmptyOrWhitespace(token)) {
            view = LayoutInflater.from(context).inflate(R.layout.dialog_not_authenticated, null);
            view.findViewById(R.id.not_authenticated_view).setOnClickListener(v -> {
                Intent intent = new Intent(context, AuthenticatorActivity.class);
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                intent.putExtra(AUTH_TOKEN_TYPE, AccountAuthenticator.AUTH_TOKEN_TYPE);
                context.startActivity(intent);
            });
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.dialog_report, null);
            EditText input = view.findViewById(R.id.report);

            builder.setPositiveButton(context.getString(R.string.send), (dialog, which) ->
                    sendReport(context, apiCallInterface, token, id, input.getText().toString(), isPodcast));
            builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        }
        builder.setView(view);
        builder.show();
    }

    public static void createTrendingFilterDialog(Context context, MutableLiveData<TrendingFilter> trendingFilterMutableLiveData, HomeViewModel viewModel, LifecycleOwner lifecycleOwner) {
        DialogTrendingFilterBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_trending_filter, null, false);
        binding.setLifecycleOwner(lifecycleOwner);
        binding.setViewModel(viewModel);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(binding.getRoot());

        binding.trendingFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == binding.trendingFilterSpinner.getCount() - 1) {
                    binding.fromDate.setVisibility(View.VISIBLE);
                    binding.toDate.setVisibility(View.VISIBLE);
                } else {
                    binding.fromDate.setVisibility(View.GONE);
                    binding.toDate.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        alertDialogBuilder
                .setPositiveButton(context.getString(R.string.set),
                        (dialog, id) -> {
                            TrendingReport trendingReport = null;
                            Date fromDate = null, toDate = null;
                            Integer categoryId = null, languageId = null;
                            if (viewModel.getCategoryId() != null && viewModel.getCategoryId() != -1) {
                                categoryId = viewModel.getCategoryId();
                            }
                            if (viewModel.getLanguageId() != null && viewModel.getLanguageId() != -1) {
                                languageId = viewModel.getLanguageId();
                            }
                            if (binding.trendingFilterSpinner.getSelectedItemPosition() != 0) {
                                trendingReport = TrendingReport.valueOf((String) binding.trendingFilterSpinner.getSelectedItem());
                            }
                            if (!Strings.isEmptyOrWhitespace(binding.fromDate.getText().toString())
                                    && !Strings.isEmptyOrWhitespace(binding.toDate.getText().toString())) {
                                try {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    fromDate = simpleDateFormat.parse(binding.fromDate.getText().toString());
                                    toDate = simpleDateFormat.parse(binding.toDate.getText().toString());
                                } catch (ParseException e) {
                                    Log.e(TAG, "createTrendingFilterDialog: ", e);
                                }
                            }
                            if (trendingReport != null || fromDate != null || toDate != null) {
                                trendingFilterMutableLiveData.setValue(new TrendingFilter(trendingReport, fromDate, toDate, categoryId, languageId));
                            }
                        })
                .setNegativeButton(context.getString(R.string.cancel),
                        (dialog, id) -> dialog.cancel());
        alertDialogBuilder.create().show();
    }

    public static void openDatePicker(AppCompatEditText view) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(view.getContext(), android.R.style.Theme_Material_Dialog_Alert, (view1, year, monthOfYear, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (monthOfYear + 1)
                    + "/" + year;
            view.setText(date);
        }, yy, mm, dd);
        datePicker.setOnCancelListener(dialog -> view.setText(""));
        datePicker.show();
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
                if (response.isSuccessful()) {
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
