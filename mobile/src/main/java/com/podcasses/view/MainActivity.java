package com.podcasses.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ferfalk.simplesearchview.SimpleOnQueryTextListener;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;
import com.ncapdevi.fragnav.tabhistory.UniqueTabHistoryStrategy;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.MainActivityBinding;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.view.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.parceler.Parcels;

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

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        BaseFragment.FragmentNavigation,
        FragNavController.RootFragmentListener,
        FragNavController.TransactionListener,
        PodcastFragment.Callback,
        Player.EventListener {

    private static final int INDEX_HOME = FragNavController.TAB1;
    private static final int INDEX_TRENDING = FragNavController.TAB2;
    private static final int INDEX_NOTIFICATION = FragNavController.TAB3;
    private static final int INDEX_UPLOAD = FragNavController.TAB4;
    private static final int INDEX_ACCOUNT = FragNavController.TAB5;
    public static final int FRAGMENTS_COUNT = 5;

    @Inject
    SharedPreferencesManager sharedPreferencesManager;

    @Inject
    AuthenticationCallInterface authenticationCall;

    private FragNavController fragNavController;

    private Intent intent;
    private AudioPlayerService service;
    private SimpleExoPlayer player;

    private MainActivityBinding binder;
    private Podcast podcast;
    private AudioPlayerService.LocalBinder localBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.main_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        setSupportActionBar(binder.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        binder.searchView.setOnQueryTextListener(searchQueryTextListener);
        binder.bottomNavigation.setOnNavigationItemSelectedListener(this);

        intent = new Intent(this, AudioPlayerService.class);
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

        binder.searchView.setMenuItem(menu.findItem(R.id.navigation_search));
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
            case INDEX_ACCOUNT:
                return AccountFragment.newInstance(0);
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    @Override
    public void onFragmentTransaction(@Nullable Fragment fragment, @NotNull FragNavController.TransactionType transactionType) {
        setTitle(fragment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!fragNavController.isRootFragment());
    }

    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        setTitle(fragment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!fragNavController.isRootFragment());
    }

    @Override
    public void pushFragment(Fragment fragment) {
        fragNavController.pushFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        if (binder.searchView.onBackPressed()) {
            return;
        }
        if (!fragNavController.popFragment()) {
            super.onBackPressed();
        }
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            localBinder = (AudioPlayerService.LocalBinder) iBinder;
            service = localBinder.getService();
            podcast = service.getPodcast();
            initializePlayer(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(serviceConnection);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public IBinder getBinder() {
        return localBinder;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (binder.searchView.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPodcast(Podcast podcast) {
        if (this.podcast != null && this.podcast.getId().equals(podcast.getId()) && player.getPlaybackState() == Player.STATE_READY) {
            player.setPlayWhenReady(!player.getPlayWhenReady());
        } else {
            this.podcast = podcast;
            startBackgroundService(podcast);
        }
    }

    private void startBackgroundService(Podcast podcast) {
        Bundle serviceBundle = new Bundle();
        serviceBundle.putParcelable("podcast", Parcels.wrap(podcast));
        intent.putExtra("player", serviceBundle);
        Util.startForegroundService(this, intent);
        initializePlayer(true);
    }

    private void initializePlayer(boolean forceStart) {
        player = service.getPlayerInstance();
        player.addListener(this);
        if (player != null && (player.getPlayWhenReady() || forceStart)) {
            binder.exoplayerView.setPlayer(player);
            binder.exoplayerView.showController();
            binder.exoplayerView.setControllerAutoShow(true);
            binder.exoplayerView.setControllerHideOnTouch(false);
            binder.exoplayerView.setVisibility(View.VISIBLE);
        }
    }

    private SimpleSearchView.OnQueryTextListener searchQueryTextListener = new SimpleOnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            fragNavController.pushFragment(SearchFragment.newInstance(1, query));
            return super.onQueryTextSubmit(query);
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return super.onQueryTextChange(newText);
        }
    };

    private void setTitle(@Nullable Fragment fragment) {
        if (fragment instanceof SearchFragment || fragment instanceof PodcastFragment || fragment instanceof AccountFragment) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            if (fragment instanceof SearchFragment) {
                ((SearchFragment) fragment).updateTitle();
            } else if (fragment instanceof PodcastFragment) {
                ((PodcastFragment) fragment).updateTitle();
            } else {
                ((AccountFragment) fragment).updateTitle();
            }
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_IDLE) {
            binder.exoplayerView.setVisibility(View.GONE);
        }
    }

}
