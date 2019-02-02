package com.podcasses.manager;

import android.content.SharedPreferences;

import javax.inject.Singleton;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class SharedPreferencesManager {

    public static final String IS_DARK_THEME = "isDarkTheme";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void setIsDarkTheme(boolean isDarkTheme) {
        sharedPreferences.edit().putBoolean(IS_DARK_THEME, isDarkTheme).apply();
    }

    public boolean isDarkTheme() {
        return sharedPreferences.getBoolean(IS_DARK_THEME, true);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

}
