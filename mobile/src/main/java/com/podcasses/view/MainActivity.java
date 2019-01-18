package com.podcasses.view;

import android.os.Bundle;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.view.base.BaseActivity;

import javax.inject.Inject;

public class MainActivity extends BaseActivity {

    @Inject
    SharedPreferencesManager sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        checkLogin();

        if (savedInstanceState == null) {
            replaceFragment(AccountFragment.newInstance(), R.id.container, false, null);
        }
    }

    @Override
    protected void onRestart() {
        checkLogin();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        checkLogin();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void checkLogin() {
//        if(!sharedPreferences.isLoggedIn()) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            finish();
//        }
    }

}
