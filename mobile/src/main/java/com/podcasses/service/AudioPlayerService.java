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

import androidx.annotation.Nullable;

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
import com.podcasses.model.entity.Podcast;

import org.parceler.Parcels;

/**
 * Created by aleksandar.kovachev.
 */
public class AudioPlayerService extends Service {

    private final IBinder binder = new LocalBinder();
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;
    private Podcast podcast;
    private MediaSessionConnector mediaSessionConnector;
    private MediaSessionCompat mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = this;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this;
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("player");
            if (bundle != null) {
                podcast = Parcels.unwrap(bundle.getParcelable("podcast"));
            }

            DataSource.Factory dataSourceFactory = ((BaseApplication) getApplication()).buildDataSourceFactory();
            player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource((Uri.parse(podcast.getPodcastUrl()))));
            player.setPlayWhenReady(true);

            playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context,
                    "playback_channel",
                    R.string.app_name,
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
            playerNotificationManager.setSmallIcon(R.drawable.ic_iconfinder_podcast_287666_white);
            playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
            playerNotificationManager.setPlayer(player);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            if (playerNotificationManager != null) {
                playerNotificationManager.setPlayer(null);
            }
            mediaSessionConnector.setPlayer(null);
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

    public SimpleExoPlayer getPlayerInstance() {
        return player;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }

    }

    private MediaDescriptionCompat getMediaDescription() {
        String image = podcast.getImageUrl();
        Bundle extras = new Bundle();
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse(image));
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse(image));
        return new MediaDescriptionCompat.Builder()
                .setMediaId(podcast.getId())
                .setIconUri(Uri.parse(image))
                .setTitle(podcast.getTitle())
                .setDescription(podcast.getDescription())
                .setExtras(extras)
                .build();
    }

}
