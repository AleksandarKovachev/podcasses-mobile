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

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.gms.common.util.Strings;
import com.podcasses.R;
import com.podcasses.adapter.PodcastMediaDescriptionAdapter;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.ConnectivityUtil;
import com.podcasses.util.NetworkRequestsUtil;

import org.jetbrains.annotations.NotNull;

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

    @Inject
    AuthenticationCallInterface authenticationCallInterface;

    private LiveData<String> token;
    private LiveData<ApiResponse> accountPodcastLiveData;
    private AccountPodcast accountPodcast;

    private final IBinder binder = new LocalBinder();
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;
    private MediaSessionConnector mediaSessionConnector;
    private MediaSessionCompat mediaSession;

    private Podcast podcast;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = this;

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        player = new SimpleExoPlayer.Builder(context).build();
        mediaSession = new MediaSessionCompat(context, this.getClass().getName());
        mediaSession.setActive(true);
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @NotNull
            @Override
            public MediaDescriptionCompat getMediaDescription(@NotNull Player player, int windowIndex) {
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
                podcast = (Podcast) bundle.getSerializable("podcast");
            }

            player.setAudioAttributes(AudioAttributes.DEFAULT, true);

            DataSource.Factory dataSourceFactory = ((BaseApplication) getApplication()).buildDataSourceFactory();
            player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource((Uri.parse(podcast.getPodcastUrl()))));
            player.setPlayWhenReady(true);

            playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context,
                    "playback_channel",
                    R.string.app_name,
                    R.string.player_channel_description,
                    1,
                    new PodcastMediaDescriptionAdapter(context, podcast),
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
            playerNotificationManager.setControlDispatcher(new DefaultControlDispatcher() {
                @Override
                public boolean dispatchStop(Player player, boolean reset) {
                    saveTimeIndex();
                    return super.dispatchStop(player, reset);
                }
            });
            playerNotificationManager.setSmallIcon(R.drawable.ic_launcher_foreground);
            playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
            playerNotificationManager.setUseStopAction(true);
            playerNotificationManager.setUsePlayPauseActions(true);
            playerNotificationManager.setUseNavigationActions(false);
            playerNotificationManager.setUseNavigationActionsInCompactView(false);
            playerNotificationManager.setPlayer(player);

            token = AuthenticationUtil.getAuthenticationToken(this, authenticationCallInterface);
            if (token == null) {
                accountPodcastLiveData = mainDataRepository.getAccountPodcast(this, null, podcast.getId());
            } else {
                token.observe(this, t -> accountPodcastLiveData = mainDataRepository.getAccountPodcast(this, t, podcast.getId()));
            }
            accountPodcastLiveData.observe(this, a -> {
                switch (a.status) {
                    case DATABASE:
                        if (token == null) {
                            accountPodcastLiveData.removeObservers(this);
                        }
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
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            saveTimeIndex();
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
        saveTimeIndex();
    }

    @Nullable
    @Override
    public IBinder onBind(@NotNull Intent intent) {
        super.onBind(intent);
        return binder;
    }

    public Podcast getPodcast() {
        return podcast;
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
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse(podcast.getPodcastUrl()));
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse(podcast.getPodcastUrl()));
        return new MediaDescriptionCompat.Builder()
                .setMediaId(podcast.getId())
                .setIconUri(Strings.isEmptyOrWhitespace(podcast.getImageUrl()) ?
                        null :
                        Uri.parse(podcast.getImageUrl()))
                .setTitle(podcast.getTitle())
                .setExtras(extras)
                .build();
    }

    private void saveTimeIndex() {
        if (player.getCurrentPosition() == 0) {
            return;
        }
        if (ConnectivityUtil.checkInternetConnection(this)) {
            if (token == null) {
                saveLocalAccountPodcast();
            } else {
                sendAccountPodcastToServer();
            }
        } else {
            saveLocalAccountPodcast();
        }
    }

    private void sendAccountPodcastToServer() {
        LiveData<ApiResponse> accountPodcastResponse =
                NetworkRequestsUtil.sendPodcastViewRequest(this, apiCallInterface,
                        token.getValue(), podcast.getId(),
                        player.getCurrentPosition(),
                        accountPodcast == null || accountPodcast.getViewTimestamp() == null);
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
    }

    private void saveLocalAccountPodcast() {
        if (accountPodcast == null) {
            accountPodcast = new AccountPodcast();
            accountPodcast.setPodcastId(podcast.getId());
            accountPodcast.setCreatedTimestamp(new Date());
        }
        if (accountPodcast.getViewTimestamp() == null) {
            accountPodcast.setViewTimestamp(new Date());
        }
        accountPodcast.setTimeIndex(player.getCurrentPosition());
        mainDataRepository.saveAccountPodcast(accountPodcast);
    }
}
