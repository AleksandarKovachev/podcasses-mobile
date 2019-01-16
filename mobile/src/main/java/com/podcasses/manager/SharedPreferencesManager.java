package com.podcasses.manager;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class SharedPreferencesManager {

    private static final String IS_LOGGED_IN = "isLoggedIn";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    private static final String USER_ID = "userId";

    private SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setLoginState(boolean loginState, String username, String password, String userId) {
        sharedPreferences.edit()
                .putBoolean(IS_LOGGED_IN, loginState)
                .putString(USERNAME, username)
                .putString(PASSWORD, password)
                .putString(USER_ID, userId)
                .apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void clearLoginState() {
        sharedPreferences.edit()
                .remove(IS_LOGGED_IN)
                .remove(USERNAME)
                .remove(PASSWORD)
                .remove(USER_ID)
                .apply();
    }

}
