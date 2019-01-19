package com.podcasses.dagger;

import com.podcasses.view.AccountFragment;
import com.podcasses.view.HomeFragment;
import com.podcasses.view.MainActivity;
import com.podcasses.view.SearchFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(HomeFragment fragment);

    void inject(SearchFragment fragment);

    void inject(AccountFragment fragment);

}
