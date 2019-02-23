package com.podcasses.dagger;

import android.app.Application;
import android.preference.PreferenceManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.podcasses.database.AppDatabase;
import com.podcasses.database.dao.AccountDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.repository.LocalDataSource;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.retrofit.interceptor.AcceptLanguageHeaderInterceptor;
import com.podcasses.retrofit.interceptor.BasicAuthInterceptor;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Named;
import javax.inject.Singleton;

import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by aleksandar.kovachev.
 */
@Module
public class NetModule {

    private String baseUrl;

    private String authenticationUrl;

    public NetModule(String baseUrl, String authenticationUrl) {
        this.baseUrl = baseUrl;
        this.authenticationUrl = authenticationUrl;
    }

    @Provides
    @Singleton
    SharedPreferencesManager provideSharedPreferencesManager(Application application) {
        return new SharedPreferencesManager(PreferenceManager.getDefaultSharedPreferences(application));
    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        return new OkHttpClient.Builder()
                .addInterceptor(new AcceptLanguageHeaderInterceptor()).cache(cache).build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    ApiCallInterface provideApiCallInterface(Retrofit retrofit) {
        return retrofit.create(ApiCallInterface.class);
    }

    @Provides
    @Singleton
    @Named("authenticationOkHttp")
    OkHttpClient provideAuthenticationOkHttpClient(Cache cache) {
        return new OkHttpClient.Builder()
                .addInterceptor(
                        new BasicAuthInterceptor(AuthenticationCallInterface.CLIENT_ID, AuthenticationCallInterface.CLIENT_SECRET))
                .addInterceptor(new AcceptLanguageHeaderInterceptor())
                .cache(cache).build();
    }

    @Provides
    @Singleton
    @Named("authenticationRetrofit")
    Retrofit provideAuthenticationRetrofit(Gson gson, @Named("authenticationOkHttp") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(authenticationUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    AuthenticationCallInterface provideAuthenticationCallInterface(@Named("authenticationRetrofit") Retrofit retrofit) {
        return retrofit.create(AuthenticationCallInterface.class);
    }

    @Provides
    @Singleton
    MainDataRepository provideMainDataRepository(ApiCallInterface apiCallInterface, LocalDataSource localDataSource, Application application) {
        return new MainDataRepository(apiCallInterface, localDataSource, application);
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory provideViewModelFactory(MainDataRepository repository) {
        return new ViewModelFactory(repository);
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase(Application context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, AppDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    @Singleton
    @Provides
    LocalDataSource provideLocalDataSource(PodcastDao podcastDao, AccountDao accountDao) {
        return new LocalDataSource(podcastDao, accountDao);
    }

    @Singleton
    @Provides
    PodcastDao providePodcastDao(AppDatabase appDatabase) {
        return appDatabase.podcastDao();
    }

    @Singleton
    @Provides
    AccountDao provideAccountDao(AppDatabase appDatabase) {
        return appDatabase.accountDao();
    }

}
