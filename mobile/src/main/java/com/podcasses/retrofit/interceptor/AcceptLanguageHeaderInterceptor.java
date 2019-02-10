package com.podcasses.retrofit.interceptor;

import android.os.Build;
import android.os.LocaleList;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class AcceptLanguageHeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithHeaders = originalRequest.newBuilder()
                .header("Accept-Language", getLanguage())
                .build();
        return chain.proceed(requestWithHeaders);
    }

    private String getLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleList.getDefault().toLanguageTags();
        } else {
            return Locale.getDefault().getLanguage();
        }
    }

}