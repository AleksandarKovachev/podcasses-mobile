package com.podcasses.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.auth0.android.jwt.JWT;
import com.podcasses.BuildConfig;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.view.AuthenticatorActivity;

import java.io.IOException;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;

    public static final String AUTH_TOKEN_TYPE = "AUTH_TOKEN_TYPE";

    public static final String REFRESH_TOKEN = "refreshToken";

    private Context context;
    private AuthenticationCallInterface authenticationCall;

    public AccountAuthenticator(Context context, AuthenticationCallInterface authenticationCall) {
        super(context);
        this.context = context;
        this.authenticationCall = authenticationCall;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                             String authTokenType, String[] requiredFeatures, Bundle options) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(context, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        AccountManager accountManager = AccountManager.get(context);
        String authToken = accountManager.peekAuthToken(account, authTokenType);
        String refreshToken = accountManager.getUserData(account, REFRESH_TOKEN);
        if (TextUtils.isEmpty(authToken) || new JWT(authToken).isExpired(0)) {
            try {
                KeycloakToken token = null;
                if (refreshToken != null && !new JWT(refreshToken).isExpired(0)) {
                    token = authenticationCall.refreshToken(
                            AuthenticationCallInterface.CLIENT_ID,
                            refreshToken,
                            AuthenticationCallInterface.REFRESH_TOKEN_GRANT_TYPE)
                            .execute().body();
                } else {
                    String password = accountManager.getPassword(account);
                    if (password != null) {
                        token = authenticationCall.accessToken(
                                account.name,
                                password,
                                AuthenticationCallInterface.ACCESS_TOKEN_GRANT_TYPE)
                                .execute().body();
                    }
                }
                if (token != null) {
                    authToken = token.getAccessToken();
                    refreshToken = token.getRefreshToken();
                }
            } catch (IOException e) {
                Log.e(AccountAuthenticator.class.getName(), "getAuthToken", e);
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, authToken);
            accountManager.setUserData(account, REFRESH_TOKEN, refreshToken);
            return result;
        }

        Intent intent = new Intent(context, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        intent.putExtra(AUTH_TOKEN_TYPE, authTokenType);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return authTokenType.equals(AUTH_TOKEN_TYPE) ? authTokenType : null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

}
