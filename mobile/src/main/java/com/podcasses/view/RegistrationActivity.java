package com.podcasses.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.RegistrationActivityBinding;
import com.podcasses.model.request.UserRegistrationRequest;
import com.podcasses.model.response.ErrorResultResponse;
import com.podcasses.model.response.FieldErrorResponse;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseActivity;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends BaseActivity {

    @Inject
    ApiCallInterface apiCallInterface;

    @Inject
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RegistrationActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.registration_activity);
        ((BaseApplication) getApplication()).getAppComponent().inject(this);
        binding.close.setOnClickListener(c -> {
            finishActivity(RESULT_CANCELED);
            finish();
        });
        binding.login.setOnClickListener(l -> {
            finishActivity(RESULT_CANCELED);
            finish();
        });

        ProgressDialog progressDialog = DialogUtil.getProgressDialog(this);
        binding.register.setOnClickListener(r -> {
            UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest();
            userRegistrationRequest.setUsername(binding.username.getText().toString());
            userRegistrationRequest.setDisplayName(Strings.isEmptyOrWhitespace(binding.displayName.getText().toString()) ?
                    binding.username.getText().toString() : binding.displayName.getText().toString());
            userRegistrationRequest.setPassword(binding.password.getText().toString());
            userRegistrationRequest.setEmail(binding.email.getText().toString());

            progressDialog.show();
            Call<Void> call = apiCallInterface.registration(userRegistrationRequest);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    progressDialog.hide();
                    if (response.isSuccessful()) {
                        Intent intent = new Intent();
                        intent.putExtra("username", binding.username.getText().toString());
                        intent.putExtra("password", binding.password.getText().toString());
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        try {
                            ErrorResultResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResultResponse.class);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (FieldErrorResponse fieldError : errorResponse.getError().getFieldErrors()) {
                                stringBuilder.append(fieldError.getError());
                            }
                            binding.errorTextMessage.setText(stringBuilder.toString());
                        } catch (IOException e) {
                            Log.e(RegistrationActivity.class.getSimpleName(), "onResponse: ", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.hide();
                    LogErrorResponseUtil.logFailure(t, RegistrationActivity.this);
                }
            });
        });
    }

}
