package com.podcasses.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.RegistrationActivityBinding;
import com.podcasses.retrofit.AuthenticationCallInterface;

import javax.inject.Inject;

public class RegistrationActivity extends AppCompatActivity {

    @Inject
    AuthenticationCallInterface authenticationCall;

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
    }

}
