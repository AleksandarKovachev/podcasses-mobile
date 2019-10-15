package com.podcasses.dagger;

import com.podcasses.service.AccountAuthenticatorService;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.view.AccountFragment;
import com.podcasses.view.AgreementActivity;
import com.podcasses.view.AuthenticatorActivity;
import com.podcasses.view.HistoryFragment;
import com.podcasses.view.HomeFragment;
import com.podcasses.view.MainActivity;
import com.podcasses.view.PodcastChannelAddFragment;
import com.podcasses.view.PodcastChannelFragment;
import com.podcasses.view.PodcastFragment;
import com.podcasses.view.PodcastListDialogFragment;
import com.podcasses.view.PodcastsPageFragment;
import com.podcasses.view.RegistrationActivity;
import com.podcasses.view.SearchFragment;
import com.podcasses.view.SettingsFragment;
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

    void inject(AuthenticatorActivity activity);

    void inject(AccountAuthenticatorService service);

    void inject(PodcastFragment fragment);

    void inject(PodcastsPageFragment fragment);

    void inject(HistoryFragment historyFragment);

    void inject(AudioPlayerService service);

    void inject(RegistrationActivity registrationActivity);

    void inject(SettingsFragment settingsFragment);

    void inject(AgreementActivity agreementActivity);

    void inject(CustomGlideModule customGlideModule);

    void inject(PodcastChannelFragment podcastChannelFragment);

    void inject(PodcastChannelAddFragment podcastChannelAddFragment);

    void inject(PodcastListDialogFragment podcastListDialogFragment);
}
