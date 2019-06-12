package com.podcasses.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.authentication.InvalidateToken;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.view.base.AuthenticationTokenTask;

import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class AuthenticationUtil {

    private static MutableLiveData<String> token = new MutableLiveData<>();

    public static LiveData<String> isAuthenticated(Context context, AuthenticationTokenTask authenticationTokenTask) {
        if (!ConnectivityUtil.checkInternetConnection(context)) {
            token.setValue(null);
            return token;
        }
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);

        if (accounts.length == 0) {
            if (authenticationTokenTask != null) {
                authenticationTokenTask.startAuthenticationActivity(token);
            }
            token.setValue(null);
        } else {
            String authToken = accountManager.peekAuthToken(accounts[0], AUTH_TOKEN_TYPE);
            if (KeycloakToken.isValidToken(authToken)) {
                token.setValue(authToken);
            } else if (ConnectivityUtil.checkInternetConnection(context)) {
                InvalidateToken invalidateToken = new InvalidateToken(accountManager, accounts[0]);
                try {
                    token.setValue(invalidateToken.execute(authToken).get());
                } catch (Exception e) {
                    Log.e("AuthenticationUtil", "isAuthenticated: ", e);
                }
            }
        }
        return token;
    }

}
