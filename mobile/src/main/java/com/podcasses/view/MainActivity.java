package com.podcasses.view;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.MainActivityBinding;
import com.podcasses.view.base.BaseActivity;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivityBinding binder = DataBindingUtil.setContentView(this, R.layout.main_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        binder.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setHomeButtonEnabled(false);
                }

                Fragment selectedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        selectedFragment = HomeFragment.newInstance();
                        break;
                    case R.id.navigation_search:
                        selectedFragment = SearchFragment.newInstance();
                        break;
                    case R.id.navigation_account:
                        selectedFragment = AccountFragment.newInstance();
                        break;
                }
                replaceFragment(selectedFragment, R.id.container, false, null);
                return true;
            }
        });
    }

}
