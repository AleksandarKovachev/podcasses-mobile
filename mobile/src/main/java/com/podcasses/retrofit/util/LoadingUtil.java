package com.podcasses.retrofit.util;


import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by aleksandar.kovachev.
 */
public class LoadingUtil {

    public static ProgressDialog getProgressDialog(Context context, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}
