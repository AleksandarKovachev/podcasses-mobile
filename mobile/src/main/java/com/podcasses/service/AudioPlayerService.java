package com.podcasses.service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.podcasses.R;
import com.podcasses.adapter.PodcastMediaDescriptionAdapter;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.ConnectivityUtil;
import com.podcasses.util.NetworkRequestsUtil;

import java.util.Date;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class AudioPlayerService extends LifecycleService implements Player.EventListener {

    @Inject
    MainDataRepository mainDataRepository;

    @Inject
    ApiCallInterface apiCallInterface;

    private LiveData<String> token;
    private LiveData<ApiResponse> accountPodcastLiveData;
    private AccountPodcast accountPodcast;

    private final IBinder binder = new LocalBinder();
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;
    private MediaSessionConnector mediaSessionConnector;
    private MediaSessionCompat mediaSession;

    private String podcastId;
    private String podcastTitle;
    private String podcastUrl;
    private String podcastImageUrl;
    private String podcastDuration;
    private String displayName;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = this;

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        mediaSession = new MediaSessionCompat(context, this.getClass().getName());
        mediaSession.setActive(true);
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return AudioPlayerService.this.getMediaDescription();
            }
        });

        mediaSessionConnector.setPlayer(player);
        player.addListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this;
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("player");
            if (bundle != null) {
                podcastId = bundle.getString("podcastId");
                podcastTitle = bundle.getString("podcastTitle");
                podcastImageUrl = bundle.getString("podcastImageUrl");
                podcastUrl = bundle.getString("podcastUrl");
                podcastDuration = bundle.getString("podcastDuration");
                displayName = bundle.getString("displayName");
            }

            DataSource.Factory dataSourceFactory = ((BaseApplication) getApplication()).buildDataSourceFactory();
            player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource((Uri.parse(podcastUrl))));
            player.setPlayWhenReady(true);

            playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context,
                    "playback_channel",
                    R.string.app_name,
                    1,
                    new PodcastMediaDescriptionAdapter(context, podcastTitle, podcastImageUrl, podcastDuration, displayName),
                    new PlayerNotificationManager.NotificationListener() {
                        @Override
                        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                            stopSelf();
                            stopForeground(true);
                        }

                        @Override
                        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                            startForeground(notificationId, notification);
                        }
                    }
            );
            playerNotificationManager.setSmallIcon(R.drawable.ic_iconfinder_podcast_287666_white);
            playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
            playerNotificationManager.setUseStopAction(true);
            playerNotificationManager.setUsePlayPauseActions(true);
            playerNotificationManager.setUseNavigationActions(false);
            playerNotificationManager.setUseNavigationActionsInCompactView(false);
            playerNotificationManager.setPlayer(player);

            token = AuthenticationUtil.isAuthenticated(this, null);
            token.observe(this, t -> {
                accountPodcastLiveData = mainDataRepository.getAccountPodcast(this, t, podcastId);
                accountPodcastLiveData.observe(this, a -> {
                    switch (a.status) {
                        case DATABASE:
                            accountPodcast = (AccountPodcast) a.data;
                            if (accountPodcast != null) {
                                player.seekTo(accountPodcast.getTimeIndex());
                            }
                            break;
                        case SUCCESS:
                            accountPodcastLiveData.removeObservers(this);
                            if (a.data != null) {
                                accountPodcast = (AccountPodcast) a.data;
                                player.seekTo(accountPodcast.getTimeIndex());
                            }
                            break;
                        case ERROR:
                            accountPodcastLiveData.removeObservers(this);
                            break;
                    }
                });
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            if (player.getCurrentPosition() != 0) {
                saveTimeIndex();
            }
            if (playerNotificationManager != null) {
                playerNotificationManager.setPlayer(null);
            }
            mediaSessionConnector.setPlayer(null);
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (player.getCurrentPosition() != 0) {
            saveTimeIndex();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public SimpleExoPlayer getPlayerInstance() {
        return player;
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }

    }

    private MediaDescriptionCompat getMediaDescription() {
        Bundle extras = new Bundle();
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse(podcastUrl));
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse(podcastUrl));
        return new MediaDescriptionCompat.Builder()
                .setMediaId(podcastId)
                .setIconUri(Uri.parse(podcastImageUrl))
                .setTitle(podcastTitle)
                .setExtras(extras)
                .build();
    }

    private void saveTimeIndex() {
        if (ConnectivityUtil.checkInternetConnection(this)) {
            LiveData<ApiResponse> accountPodcastResponse =
                    NetworkRequestsUtil.sendPodcastViewRequest(this, apiCallInterface,
                            token != null ? token.getValue() : null, podcastId,
                            player.getCurrentPosition(), true);
            accountPodcastResponse.observe(this, response -> {
                switch (response.status) {
                    case SUCCESS:
                        accountPodcastResponse.removeObservers(this);
                        accountPodcast = (AccountPodcast) response.data;
                        mainDataRepository.saveAccountPodcast(accountPodcast);
                        break;
                    case ERROR:
                        accountPodcastResponse.removeObservers(this);
                        break;
                    default:
                        break;
                }
            });
        } else {
            if (accountPodcast == null) {
                accountPodcast = new AccountPodcast();
                accountPodcast.setPodcastId(podcastId);
                accountPodcast.setViewTimestamp(new Date());
                accountPodcast.setCreatedTimestamp(new Date());
            }
            accountPodcast.setTimeIndex(player.getCurrentPosition());
            mainDataRepository.saveAccountPodcast(accountPodcast);
        }
    }
}
