package com.podcasses.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.retrofit.AuthenticationCallInterface;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountAuthenticatorService extends Service {

    @Inject
    AuthenticationCallInterface apiCallInterface;

    @Override
    public void onCreate() {
        ((BaseApplication) this.getApplication()).getAppComponent().inject(this);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AccountAuthenticator(this, apiCallInterface).getIBinder();
    }

}
