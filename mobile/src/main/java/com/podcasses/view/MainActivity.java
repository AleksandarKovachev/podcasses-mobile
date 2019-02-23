package com.podcasses.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;
import com.ncapdevi.fragnav.tabhistory.UniqueTabHistoryStrategy;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.MainActivityBinding;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.view.base.BaseFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;
import static com.podcasses.authentication.AccountAuthenticator.REFRESH_TOKEN;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, BaseFragment.FragmentNavigation, FragNavController.RootFragmentListener, FragNavController.TransactionListener {

    private static final int INDEX_HOME = FragNavController.TAB1;
    private static final int INDEX_TRENDING = FragNavController.TAB2;
    private static final int INDEX_NOTIFICATION = FragNavController.TAB3;
    private static final int INDEX_UPLOAD = FragNavController.TAB4;
    private static final int INDEX_SEARCH = FragNavController.TAB5;
    private static final int INDEX_ACCOUNT = FragNavController.TAB6;
    public static final int FRAGMENTS_COUNT = 6;

    public static final String SERVICE_STATUS = "serviceStatus";
    private boolean serviceBound;

    @Inject
    SharedPreferencesManager sharedPreferencesManager;

    @Inject
    AuthenticationCallInterface authenticationCall;

    private FragNavController fragNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivityBinding binder = DataBindingUtil.setContentView(this, R.layout.main_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        fragNavController = new FragNavController(getSupportFragmentManager(), R.id.container);

        fragNavController.setRootFragmentListener(this);
        fragNavController.setTransactionListener(this);

        fragNavController.setDefaultTransactionOptions(
                new FragNavTransactionOptions.Builder().transition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).build());

        fragNavController.setFragmentHideStrategy(FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH);
        fragNavController.setNavigationStrategy(new UniqueTabHistoryStrategy((i, fragNavTransactionOptions) -> {
            switch (i) {
                case INDEX_HOME:
                    binder.bottomNavigation.setSelectedItemId(R.id.navigation_home);
                    break;
                case INDEX_TRENDING:
                    binder.bottomNavigation.setSelectedItemId(R.id.navigation_trending);
                    break;
                case INDEX_NOTIFICATION:
                    binder.bottomNavigation.setSelectedItemId(R.id.navigation_notifications);
                    break;
            }
        }));

        fragNavController.initialize(INDEX_HOME, savedInstanceState);
        if (savedInstanceState != null) {
            binder.bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }

        binder.bottomNavigation.setOnNavigationItemSelectedListener(this);

        initChannels(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fragNavController != null) {
            fragNavController.onSaveInstanceState(outState);
        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                fragNavController.switchTab(INDEX_HOME);
                break;
            case R.id.navigation_trending:
                fragNavController.switchTab(INDEX_TRENDING);
                break;
            case R.id.navigation_notifications:
                fragNavController.switchTab(INDEX_NOTIFICATION);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_account:
                fragNavController.switchTab(INDEX_ACCOUNT);
                break;
            case R.id.navigation_upload:
                fragNavController.switchTab(INDEX_UPLOAD);
                break;
            case R.id.navigation_search:
                fragNavController.switchTab(INDEX_SEARCH);
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
            case R.id.navigation_logout:
                handleLogout();
                break;
            default:
                return fragNavController.popFragment();
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

    @Override
    public int getNumberOfRootFragments() {
        return FRAGMENTS_COUNT;
    }

    @NotNull
    @Override
    public Fragment getRootFragment(int i) {
        switch (i) {
            case INDEX_HOME:
                return HomeFragment.newInstance(0);
            case INDEX_TRENDING:
                return TrendingFragment.newInstance(0);
            case INDEX_NOTIFICATION:
                return NotificationsFragment.newInstance(0);
            case INDEX_UPLOAD:
                return UploadFragment.newInstance(0);
            case INDEX_SEARCH:
                return SearchFragment.newInstance(0);
            case INDEX_ACCOUNT:
                return AccountFragment.newInstance(0);
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    @Override
    public void onFragmentTransaction(@Nullable Fragment fragment, @NotNull FragNavController.TransactionType transactionType) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!fragNavController.isRootFragment());
    }

    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!fragNavController.isRootFragment());
    }

    @Override
    public void pushFragment(Fragment fragment) {
        fragNavController.pushFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        if (!fragNavController.popFragment()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(SERVICE_STATUS, serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean(SERVICE_STATUS);
    }

    private void handleLogout() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            String authToken = accountManager.peekAuthToken(accounts[0], AUTH_TOKEN_TYPE);
            String refreshToken = accountManager.getUserData(accounts[0], REFRESH_TOKEN);
            authenticationCall.logout(AuthenticationCallInterface.TOKEN_TYPE + authToken,
                    AuthenticationCallInterface.CLIENT_ID,
                    AuthenticationCallInterface.CLIENT_SECRET,
                    refreshToken).enqueue(logoutRequest(accounts[0], accountManager));
        }
    }

    private Callback<Void> logoutRequest(Account account, AccountManager accountManager) {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                accountManager.removeAccount(account, null, null);
                Toast.makeText(MainActivity.this, getString(R.string.successful_logout), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, getString(R.string.error_response), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(BuildConfig.APPLICATION_ID, "Podcasses", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

}
