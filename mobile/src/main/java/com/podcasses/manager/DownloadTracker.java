package com.podcasses.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.util.Util;
import com.podcasses.service.AudioDownloadService;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by aleksandar.kovachev.
 */
public class DownloadTracker {

    private static final String TAG = "DownloadTracker";

    public interface Listener {
        void onDownloadsChanged();
    }

    private final Context context;
    private final HashMap<Uri, Download> downloads;
    private final DownloadIndex downloadIndex;
    private final CopyOnWriteArraySet<Listener> listeners;

    public DownloadTracker(Context context, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public Collection<Download> getAllDownloads() {
        return downloads.values();
    }

    public boolean isDownloaded(String url) {
        Download download = downloads.get(Uri.parse(url));
        return download != null && download.state != Download.STATE_FAILED;
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    public void toggleDownload(String url, String id, String name) {
        Uri uri = Uri.parse(url);
        Download download = downloads.get(uri);
        if (download != null) {
            DownloadService.sendRemoveDownload(context, AudioDownloadService.class, download.request.id, false);
        } else {
            DownloadRequest downloadRequest = DownloadHelper.forProgressive(uri).getDownloadRequest(id, Util.getUtf8Bytes(name));
            DownloadService.sendAddDownload(context, AudioDownloadService.class, downloadRequest, false);
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {
        @Override
        public void onDownloadChanged(DownloadManager downloadManager, Download download) {
            downloads.put(download.request.uri, download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
            downloads.remove(download.request.uri);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }

}
