package com.podcasses.manager;

import android.content.SharedPreferences;

import javax.inject.Singleton;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class SharedPreferencesManager {

    private static final String PLAYBACK_SPEED = "playback_speed";
    private static final String TRIM_SILENCE = "trim_silence";
    private static final String LOCALE = "locale";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public float getPlaybackSpeed() {
        return sharedPreferences.getFloat(PLAYBACK_SPEED, 1f);
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        sharedPreferences.edit().putFloat(PLAYBACK_SPEED, playbackSpeed).apply();
    }

    public boolean isTrimSilence() {
        return sharedPreferences.getBoolean(TRIM_SILENCE, false);
    }

    public void setTrimSilence(boolean trimSilence) {
        sharedPreferences.edit().putBoolean(TRIM_SILENCE, trimSilence).apply();
    }

    public String getLocale() {
        return sharedPreferences.getString(LOCALE, null);
    }

    public void setLocale(String locale) {
        sharedPreferences.edit().putString(LOCALE, locale).apply();
    }

}