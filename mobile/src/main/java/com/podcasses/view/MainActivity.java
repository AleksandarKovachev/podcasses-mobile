package com.podcasses.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.MainActivityBinding;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.view.base.BaseActivity;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class MainActivity extends BaseActivity {

    @Inject
    SharedPreferencesManager sharedPreferencesManager;

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.navigation_dark_theme);
        checkable.setChecked(sharedPreferencesManager.isDarkTheme());
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
            case R.id.navigation_dark_theme:
                if (!item.isChecked()) {
                    item.setChecked(true);
                } else {
                    item.setChecked(false);
                }
                sharedPreferencesManager.setIsDarkTheme(item.isChecked());
                this.recreate();
                break;
            default:
                return false;
        }
        if (selectedFragment != null) {
            replaceFragment(selectedFragment, R.id.container, false, null);
        }
        return true;
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SharedPreferencesManager.IS_DARK_THEME, true);

        if (isDarkTheme) {
            theme.applyStyle(R.style.DarkAppTheme, true);
        } else {
            theme.applyStyle(R.style.LightAppTheme, true);
        }
        return theme;
    }

}
