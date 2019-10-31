package com.podcasses.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.google.android.material.snackbar.Snackbar;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.authentication.InvalidateToken;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.view.AuthenticatorActivity;
import com.podcasses.view.base.BaseFragment;

import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class AuthenticationUtil {

    public static MutableLiveData<String> getAuthenticationToken(Context context, AuthenticationCallInterface authenticationCallInterface) {
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
                InvalidateToken invalidateToken = new InvalidateToken(accountManager, accounts[0], authenticationCallInterface);
                try {
                    token.setValue(invalidateToken.execute(authToken).get());
                } catch (Exception e) {
                    Log.e("AuthenticationUtil", "getAuthenticationToken: ", e);
                }
            }
        }
        return token;
    }

    static void showAuthenticationSnackbar(View view, Context context) {
        Snackbar.make(view, context.getText(R.string.not_authenticated), Snackbar.LENGTH_LONG)
                .setAction(context.getText(R.string.click_to_authenticate), v -> {
                    Intent intent = new Intent(context, AuthenticatorActivity.class);
                    intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                    intent.putExtra(AUTH_TOKEN_TYPE, AUTH_TOKEN_TYPE);
                    context.startActivity(intent);
                }).show();
    }

    public static void showAuthenticationSnackbar(View view, Context context, BaseFragment fragment) {
        Snackbar.make(view, context.getText(R.string.not_authenticated), Snackbar.LENGTH_LONG)
                .setAction(context.getText(R.string.click_to_authenticate), v -> {
                    Intent intent = new Intent(context, AuthenticatorActivity.class);
                    intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                    intent.putExtra(AUTH_TOKEN_TYPE, AUTH_TOKEN_TYPE);
                    fragment.startActivityForResult(intent, 22);
                }).show();
    }

}
