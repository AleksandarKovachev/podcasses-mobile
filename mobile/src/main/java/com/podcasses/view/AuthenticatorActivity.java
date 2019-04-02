package com.podcasses.view;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.podcasses.R;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.AuthenticatorActivityBinding;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.util.LoadingUtil;

import java.util.Objects;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.podcasses.authentication.AccountAuthenticator.ACCOUNT_TYPE;
import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;
import static com.podcasses.authentication.AccountAuthenticator.REFRESH_TOKEN;

/**
 * Created by aleksandar.kovachev.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    @Inject
    AuthenticationCallInterface authenticationCall;

    private AuthenticatorActivityBinding binder;

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.authenticator_activity);

        ((BaseApplication) getApplication()).getAppComponent().inject(this);

        binder.loginInput.setOnClickListener(loginClickListener);

        accountManager = AccountManager.get(this);
    }

    private View.OnClickListener loginClickListener = v -> {
        ProgressDialog progressDialog = LoadingUtil.getProgressDialog(this);
        progressDialog.show();
        final Intent intent = new Intent();
        Call<KeycloakToken> authToken = authenticationCall.grantNewAccessToken(
                Objects.requireNonNull(binder.username.getText()).toString(),
                Objects.requireNonNull(binder.password.getText()).toString(),
                AuthenticationCallInterface.ACCESS_TOKEN_GRANT_TYPE);

        authToken.enqueue(new Callback<KeycloakToken>() {
            @Override
            public void onResponse(Call<KeycloakToken> call, Response<KeycloakToken> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    Account account = addOrFindAccount(binder.username.getText().toString(), binder.password.getText().toString(), response.body().getRefreshToken());
                    accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, response.body().getAccessToken());
                    finishAccountAdd(intent, binder.username.getText().toString(), response.body().getAccessToken(), response.body().getRefreshToken());
                }
            }

            @Override
            public void onFailure(Call<KeycloakToken> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(AuthenticatorActivity.class.getName(), "onFailure", t);
                Toasty.error(AuthenticatorActivity.this, getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
    };

    private Account addOrFindAccount(String username, String password, String refreshToken) {
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        Account account = accounts.length != 0 ? accounts[0] :
                new Account(username, ACCOUNT_TYPE);

        if (accounts.length == 0) {
            accountManager.addAccountExplicitly(account, password, null);
        } else {
            accountManager.setPassword(accounts[0], password);
        }
        accountManager.setUserData(account, REFRESH_TOKEN, refreshToken);
        return account;
    }

    private void finishAccountAdd(Intent intent, String accountName, String authToken, String password) {
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
        intent.putExtra(AccountManager.KEY_PASSWORD, password);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

}
