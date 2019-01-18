package com.podcasses.dagger;

import com.podcasses.view.AccountFragment;
import com.podcasses.view.LoginActivity;
import com.podcasses.view.MainActivity;

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

    void inject(AccountFragment fragment);

}
