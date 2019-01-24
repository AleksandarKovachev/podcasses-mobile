package com.podcasses.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by aleksandar.kovachev.
 */
public class InvalidateToken extends AsyncTask<String, Void, String> {

    private Account account;
    private AccountManager accountManager;

    public InvalidateToken(AccountManager accountManager, Account account) {
        this.accountManager = accountManager;
        this.account = account;
    }

    @Override
    protected String doInBackground(String... params) {
        String token = null;
        try {
            accountManager.invalidateAuthToken(AccountAuthenticator.ACCOUNT_TYPE, params[0]);
            token = accountManager.blockingGetAuthToken(account,
                    AccountAuthenticator.ACCOUNT_TYPE, true);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "invalidateToken: ", e);
        }
        return token;
    }

}
