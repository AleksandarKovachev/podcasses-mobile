package com.podcasses.manager;

import android.content.SharedPreferences;

import javax.inject.Singleton;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class SharedPreferencesManager {

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void setViewedPodcast(String podcastId) {
        sharedPreferences.edit().putBoolean(podcastId, true).apply();
    }

    public boolean isPodcastViewed(String podcastId) {
        return sharedPreferences.getBoolean(podcastId, false);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

}