package com.podcasses.manager;

import android.content.SharedPreferences;

import javax.inject.Singleton;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class SharedPreferencesManager {

    public static final String PLAYBACK_SPEED = "playback_speed";
    public static final String TRIM_SILENCE = "trim_silence";

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

    public float getPlaybackSpeed() {
        return sharedPreferences.getFloat(PLAYBACK_SPEED, 1f);
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        sharedPreferences.edit().putFloat(PLAYBACK_SPEED, playbackSpeed).apply();
    }

    public boolean isTrimSilince() {
        return sharedPreferences.getBoolean(TRIM_SILENCE, false);
    }

    public void setTrimSilence(boolean trimSilence) {
        sharedPreferences.edit().putBoolean(TRIM_SILENCE, trimSilence).apply();
    }

}