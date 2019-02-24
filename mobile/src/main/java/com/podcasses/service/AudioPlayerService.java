package com.podcasses.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastMediaDescriptionAdapter;
import com.podcasses.model.entity.Podcast;

import org.parceler.Parcels;

import androidx.annotation.Nullable;

/**
 * Created by aleksandar.kovachev.
 */
public class AudioPlayerService extends Service {

    private final IBinder binder = new LocalBinder();
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;
    private Podcast podcast;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            playerNotificationManager.setPlayer(null);
            mediaSessionConnector.setPlayer(null, null);
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public SimpleExoPlayer initPlayerInstance() {
        if (player == null) {
            startPlayer();
        }
        return player;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getBundleExtra("player");
        if (bundle != null) {
            podcast = Parcels.unwrap(bundle.getParcelable("podcast"));
        }
        if (player == null) {
            startPlayer();
        }
        return START_STICKY;
    }

    private void startPlayer() {
        final Context context = this;
        Uri uri = Uri.parse(BuildConfig.API_GATEWAY_URL.concat("/listen/podcast/").concat(podcast.getId()));
        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, getString(R.string.app_name)));
        Cache cache = new SimpleCache(context.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 10));
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                cache,
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context,
                BuildConfig.APPLICATION_ID,
                R.string.app_name,
                224,
                new PodcastMediaDescriptionAdapter(context, podcast)
        );
        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });
        playerNotificationManager.setPlayer(player);

        mediaSession = new MediaSessionCompat(context, this.getClass().getName());
        mediaSession.setActive(true);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return AudioPlayerService.this.getMediaDescription();
            }
        });

        mediaSessionConnector.setPlayer(player, null);
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    private MediaDescriptionCompat getMediaDescription() {
        String image = BuildConfig.API_GATEWAY_URL.concat("/podcast/image/").concat(podcast.getId());
        Bundle extras = new Bundle();
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse(image));
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse(image));
        return new MediaDescriptionCompat.Builder()
                .setMediaId(podcast.getId())
                .setIconUri(Uri.parse(image))
                .setTitle(podcast.getTitle())
                .setDescription(podcast.getQuote())
                .setExtras(extras)
                .build();


    }

}
