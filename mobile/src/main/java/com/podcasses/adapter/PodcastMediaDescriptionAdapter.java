package com.podcasses.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.podcasses.view.MainActivity;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastMediaDescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

    private Context context;
    private String podcastTitle;
    private String podcastImageUrl;
    private String podcastDuration;
    private String podcastChannel;

    public PodcastMediaDescriptionAdapter(Context context,
                                          String podcastTitle, String podcastImageUrl,
                                          String podcastDuration, String podcastChannel) {
        this.context = context;
        this.podcastTitle = podcastTitle;
        this.podcastImageUrl = podcastImageUrl;
        this.podcastDuration = podcastDuration;
        this.podcastChannel = podcastChannel;
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        return podcastTitle;
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return podcastChannel;
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        Glide.with(context)
                .asBitmap()
                .load(podcastImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callback.onBitmap(resource);
                    }
                });
        return null;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public String getCurrentSubText(Player player) {
        return podcastDuration;
    }

}
