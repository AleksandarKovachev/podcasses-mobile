package com.podcasses.retrofit.interceptor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.authentication.InvalidateToken;
import com.podcasses.retrofit.AuthenticationCallInterface;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class UnauthorizedInterceptor implements Interceptor {

    private Context context;
    private AuthenticationCallInterface authenticationCallInterface;

    public UnauthorizedInterceptor(Context context, AuthenticationCallInterface authenticationCallInterface) {
        this.context = context;
        this.authenticationCallInterface = authenticationCallInterface;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() == 401) {
            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
            if (accounts.length != 0) {
                InvalidateToken invalidateToken = new InvalidateToken(accountManager, accounts[0], authenticationCallInterface);
                invalidateToken.execute(accountManager.peekAuthToken(accounts[0], AUTH_TOKEN_TYPE));
            }
        }
        return response;
    }

}
