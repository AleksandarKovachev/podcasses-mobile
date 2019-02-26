package com.podcasses.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.podcasses.model.entity.Podcast;
import com.podcasses.view.MainActivity;

import androidx.annotation.Nullable;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastMediaDescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

    private Podcast podcast;
    private Context context;

    public PodcastMediaDescriptionAdapter(Context context, Podcast podcast) {
        this.context = context;
        this.podcast = podcast;
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        return podcast.getTitle();
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return podcast.getQuote();
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        return null;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
