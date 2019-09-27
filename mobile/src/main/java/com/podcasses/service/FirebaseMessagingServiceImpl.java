package com.podcasses.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.podcasses.R;
import com.podcasses.view.MainActivity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleksandar.kovachev.
 */
public class FirebaseMessagingServiceImpl extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null && !remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();

            PendingIntent pendingIntent = getPendingIntent(data);

            Bitmap podcastChannelImage = getBitmapImage(data.get("podcastChannelImage"));
            Bitmap podcastImage = getBitmapImage(data.get("podcastImage"));

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, "FCM")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody())
                            .setLargeIcon(podcastImage)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigLargeIcon(podcastChannelImage)
                                    .bigPicture(podcastImage))
                            .setAutoCancel(true)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("FCM",
                        "Firebase Cloud Messaging",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private PendingIntent getPendingIntent(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("podcastId", data.get("podcastId"));
        return PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    private Bitmap getBitmapImage(String url) {
        try {
            return Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .submit().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error during retrieving bitmap image", e);
        }
        return null;
    }

}
