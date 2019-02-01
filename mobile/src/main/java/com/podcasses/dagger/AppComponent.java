package com.podcasses.dagger;

import com.podcasses.service.AccountAuthenticatorService;
import com.podcasses.view.AccountFragment;
import com.podcasses.view.AuthenticatorActivity;
import com.podcasses.view.HomeFragment;
import com.podcasses.view.MainActivity;
import com.podcasses.view.NotificationsFragment;
import com.podcasses.view.SearchFragment;
import com.podcasses.view.TrendingFragment;
import com.podcasses.view.UploadFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(HomeFragment fragment);

    void inject(SearchFragment fragment);

    void inject(AccountFragment fragment);

    void inject(UploadFragment fragment);

    void inject(TrendingFragment fragment);

    void inject(NotificationsFragment fragment);

    void inject(AuthenticatorActivity activity);

    void inject(AccountAuthenticatorService service);

}
