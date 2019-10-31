package com.podcasses.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.podcasses.retrofit.AuthenticationCallInterface;

import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;
import static com.podcasses.authentication.AccountAuthenticator.REFRESH_TOKEN;

/**
 * Created by aleksandar.kovachev.
 */
public class InvalidateToken extends AsyncTask<String, Void, String> {

    private Account account;
    private AccountManager accountManager;
    private AuthenticationCallInterface authenticationCallInterface;

    public InvalidateToken(AccountManager accountManager, Account account, AuthenticationCallInterface authenticationCallInterface) {
        this.accountManager = accountManager;
        this.account = account;
        this.authenticationCallInterface = authenticationCallInterface;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            accountManager.invalidateAuthToken(AccountAuthenticator.ACCOUNT_TYPE, params[0]);
            AccountManagerFuture<Bundle> futureBundle = accountManager.getAuthToken(
                    account,
                    AUTH_TOKEN_TYPE,
                    null,
                    false,
                    null,
                    null
            );
            String token = futureBundle.getResult().getString(AccountManager.KEY_AUTHTOKEN);

            if (token != null) {
                return token;
            }

            KeycloakToken keycloakToken = null;
            String password = accountManager.getPassword(account);
            if (password != null) {
                keycloakToken = authenticationCallInterface.accessToken(
                        account.name,
                        password,
                        AuthenticationCallInterface.ACCESS_TOKEN_GRANT_TYPE)
                        .execute().body();
            }
            if (keycloakToken != null) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, keycloakToken.getAccessToken());
                accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, keycloakToken.getAccessToken());
                accountManager.setUserData(account, REFRESH_TOKEN, keycloakToken.getRefreshToken());
            } else {
                accountManager.getAuthToken(
                        account,
                        AUTH_TOKEN_TYPE,
                        null,
                        true,
                        null,
                        null
                );
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "invalidateToken: ", e);
        }
        return null;
    }

}
