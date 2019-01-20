package com.podcasses.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.MainActivityBinding;
import com.podcasses.view.base.BaseActivity;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivityBinding binder = DataBindingUtil.setContentView(this, R.layout.main_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        binder.bottomNavigation.setOnNavigationItemSelectedListener(menuItem -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
            }

            Fragment selectedFragment = null;
            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = HomeFragment.newInstance();
                    break;
                case R.id.navigation_trending:
                    selectedFragment = TrendingFragment.newInstance();
                    break;
                case R.id.navigation_notifications:
                    selectedFragment = NotificationsFragment.newInstance();
                    break;
            }
            replaceFragment(selectedFragment, R.id.container, false, null);
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_account:
                selectedFragment = AccountFragment.newInstance();
                break;
            case R.id.navigation_upload:
                selectedFragment = UploadFragment.newInstance();
                break;
            case R.id.navigation_search:
                selectedFragment = SearchFragment.newInstance();
                break;
        }
        replaceFragment(selectedFragment, R.id.container, false, null);
        return true;
    }

}
