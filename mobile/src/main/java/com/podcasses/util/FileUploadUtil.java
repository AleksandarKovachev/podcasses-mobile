package com.podcasses.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.podcasses.BuildConfig;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.util.UUID;

/**
 * Created by aleksandar.kovachev.
 */
public class FileUploadUtil {

    public static void uploadFileToServer(Context context, String token, File file, String url, String multipartName) {
        try {
            MultipartUploadRequest request = new MultipartUploadRequest(
                    context,
                    UUID.randomUUID().toString(),
                    BuildConfig.API_GATEWAY_URL.concat(url));

            request.addHeader("Authorization", "Bearer " + token);
            request.addFileToUpload(file.getPath(), multipartName);
            request.setNotificationConfig(new UploadNotificationConfig());
            request.setMaxRetries(2);
            request.startUpload();
        } catch (Exception e) {
            Log.e(FileUploadUtil.class.getSimpleName(), "onFailure: ", e);
        }
    }

    public static String getRealPathFromURIPath(Uri contentURI, Context context) {
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

}
