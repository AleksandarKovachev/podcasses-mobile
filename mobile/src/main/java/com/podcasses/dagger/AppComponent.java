package com.podcasses.dagger;

import com.podcasses.view.login.LoginActivity;
import com.podcasses.view.main.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(LoginActivity activity);

}
