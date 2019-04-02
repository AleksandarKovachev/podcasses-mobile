package com.podcasses.util;


import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by aleksandar.kovachev.
 */
public class LoadingUtil {

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}
