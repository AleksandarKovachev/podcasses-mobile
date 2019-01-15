package com.podcasses.manager;

import android.content.SharedPreferences;

import com.podcasses.constant.SharedPreferencesConstant;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class SharedPreferencesManager {

    private SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setLoginState(boolean loginState, String username, String password, String userId) {
        sharedPreferences.edit()
                .putBoolean(SharedPreferencesConstant.IS_LOGGED_IN, loginState)
                .putString(SharedPreferencesConstant.USERNAME, username)
                .putString(SharedPreferencesConstant.PASSWORD, password)
                .putString(SharedPreferencesConstant.USER_ID, userId)
                .apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(SharedPreferencesConstant.IS_LOGGED_IN, false);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void clearLoginState() {
        sharedPreferences.edit()
                .remove(SharedPreferencesConstant.IS_LOGGED_IN)
                .remove(SharedPreferencesConstant.USERNAME)
                .remove(SharedPreferencesConstant.PASSWORD)
                .remove(SharedPreferencesConstant.USER_ID)
                .apply();
    }

}
