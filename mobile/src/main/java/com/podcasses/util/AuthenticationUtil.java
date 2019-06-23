package com.podcasses.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.authentication.InvalidateToken;
import com.podcasses.authentication.KeycloakToken;

import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class AuthenticationUtil {

    public static MutableLiveData<String> getAuthenticationToken(Context context) {
        if (AccountManager.get(context).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE).length == 0) {
            return null;
        }

        MutableLiveData<String> token = new MutableLiveData<>();
        if (!ConnectivityUtil.checkInternetConnection(context)) {
            token.setValue(null);
        }
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            String authToken = accountManager.peekAuthToken(accounts[0], AUTH_TOKEN_TYPE);
            if (KeycloakToken.isValidToken(authToken)) {
                token.setValue(authToken);
            } else if (ConnectivityUtil.checkInternetConnection(context)) {
                InvalidateToken invalidateToken = new InvalidateToken(accountManager, accounts[0]);
                try {
                    token.setValue(invalidateToken.execute(authToken).get());
                } catch (Exception e) {
                    Log.e("AuthenticationUtil", "getAuthenticationToken: ", e);
                }
            }
        }
        return token;
    }

}
