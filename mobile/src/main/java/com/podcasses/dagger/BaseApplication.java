package com.podcasses.dagger;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.podcasses.BuildConfig;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.AuthenticationCallInterface;

import net.gotev.uploadservice.UploadService;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule(ApiCallInterface.API_GATEWAY_URL, AuthenticationCallInterface.KEYCLOAK_URL))
                .build();

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
