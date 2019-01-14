package com.podcasses.dagger;

import android.app.Application;

import com.podcasses.constant.ApiUrl;

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
                .netModule(new NetModule(ApiUrl.BASE_URL))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
