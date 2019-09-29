package com.podcasses.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ferfalk.simplesearchview.SimpleOnQueryTextListener;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;
import com.ncapdevi.fragnav.tabhistory.UniqueTabHistoryStrategy;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.MainActivityBinding;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.view.base.BaseActivity;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public class MainActivity extends BaseActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        BaseFragment.FragmentNavigation,
        FragNavController.RootFragmentListener,
        FragNavController.TransactionListener,
        FragmentCallback,
        Player.EventListener {

    private static final int INDEX_HOME = FragNavController.TAB1;
    private static final int INDEX_ACCOUNT = FragNavController.TAB2;
    public static final int FRAGMENTS_COUNT = 2;

    @Inject
    AuthenticationCallInterface authenticationCall;

    private FragNavController fragNavController;

    private Intent intent;
    private AudioPlayerService service;
    private SimpleExoPlayer player;

    private MainActivityBinding binder;
    private Podcast playingPodcast;
    private AudioPlayerService.LocalBinder localBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.main_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        setSupportActionBar(binder.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        MobileAds.initialize(this, getString(R.string.admob_app_id));

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
                case INDEX_ACCOUNT:
                    binder.bottomNavigation.setSelectedItemId(R.id.navigation_account);
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
        getMenuInflater().inflate(R.menu.home_navigation, menu);
        binder.searchView.setMenuItem(menu.findItem(R.id.navigation_search));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_upload:
            case R.id.navigation_history:
            case R.id.navigation_logout:
            case R.id.navigation_share:
            case R.id.navigation_mark_as_played:
            case R.id.navigation_report:
            case R.id.navigation_list:
                return super.onOptionsItemSelected(item);
            default:
                return fragNavController.popFragment();
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                fragNavController.switchTab(INDEX_HOME);
                break;
            case R.id.navigation_account:
                fragNavController.switchTab(INDEX_ACCOUNT);
                break;
        }
        return true;
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
            case INDEX_ACCOUNT:
                return AccountFragment.newInstance(0, null, true);
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
    public void popFragment() {
        fragNavController.popFragment();
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            localBinder = (AudioPlayerService.LocalBinder) iBinder;
            service = localBinder.getService();
            playingPodcast = service.getPodcast();
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
        if (playingPodcast != null && podcast.getId().equals(playingPodcast.getId()) && player.getPlaybackState() == Player.STATE_READY) {
            player.setPlayWhenReady(!player.getPlayWhenReady());
        } else {
            playingPodcast = podcast;
            startBackgroundService(podcast);
        }
    }

    private void startBackgroundService(Podcast podcast) {
        Bundle serviceBundle = new Bundle();
        serviceBundle.putSerializable("podcast", podcast);
        intent.putExtra("player", serviceBundle);
        Util.startForegroundService(this, intent);
        initializePlayer(true);
    }

    private void initializePlayer(boolean forceStart) {
        player = service.getPlayerInstance();
        player.addListener(this);
        if (player != null && (player.getPlayWhenReady() || forceStart)) {
            binder.exoplayerView.setPlayer(player);
            binder.exoplayerView.show();
            binder.exoplayerView.showContextMenu();
            binder.exoplayerView.setPodcastTitle(playingPodcast.getTitle());
            binder.exoplayerView.setPodcastImage(playingPodcast.getImageUrl());
            binder.exoplayerView.setVisibility(View.VISIBLE);
            binder.exoplayerView.setOnClickListener(
                    v -> fragNavController.pushFragment(PodcastFragment.newInstance(1, playingPodcast.getId(), playingPodcast)));
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
        if (!(fragment instanceof PodcastFragment) && !(fragment instanceof PodcastChannelFragment)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            setSupportActionBar(binder.toolbar);
            getSupportActionBar().show();
        }
        if (fragment instanceof PodcastFragment) {
            ((PodcastFragment) fragment).updateActionBar();
        }
        if (fragment instanceof PodcastChannelFragment) {
            ((PodcastChannelFragment) fragment).updateActionBar();
        }
        if (fragment instanceof SearchFragment) {
            ((SearchFragment) fragment).updateActionBar();
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_IDLE) {
            binder.exoplayerView.setVisibility(View.GONE);
        }
    }

}
