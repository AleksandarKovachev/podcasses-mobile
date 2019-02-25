package com.podcasses.service;

import android.app.Notification;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.podcasses.R;
import com.podcasses.util.DownloadUtil;

import androidx.annotation.Nullable;

/**
 * Created by aleksandar.kovachev.
 */
public class AudioDownloadService extends DownloadService {

    public static final int DOWNLOAD_NOTIFICATION_ID = 2;
    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";

    public AudioDownloadService() {
        super(DOWNLOAD_NOTIFICATION_ID, DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_CHANNEL_ID, R.string.exo_download_notification_channel_name);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return DownloadUtil.getDownloadManager(this);
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return null;
    }

    @Override
    protected Notification getForegroundNotification(DownloadManager.TaskState[] taskStates) {
        return DownloadNotificationUtil.buildProgressNotification(this, R.drawable.exo_icon_play, DOWNLOAD_CHANNEL_ID,
                null, null, taskStates);
    }
}
