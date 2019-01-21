package com.podcasses.dagger;

import android.app.Application;

import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.AuthenticationCallInterface;

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
                .netModule(new NetModule(ApiCallInterface.BASE_URL, AuthenticationCallInterface.BASE_URL))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
