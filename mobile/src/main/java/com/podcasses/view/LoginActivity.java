package com.podcasses.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.view.base.BaseActivity;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class LoginActivity extends BaseActivity {

    @Inject
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);
    }

}
