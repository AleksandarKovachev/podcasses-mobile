package com.podcasses.view;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.auth0.android.jwt.JWT;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.Task;
import com.podcasses.R;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.AuthenticatorActivityBinding;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.util.LogErrorResponseUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.inject.Inject;

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

    private static int RC_REGISTRATION = 1;
    private static int RC_GOOGLE_SIGN_IN = 2;

    @Inject
    AuthenticationCallInterface authenticationCall;

    @Inject
    MainDataRepository repository;

    private CallbackManager callbackManager;

    private AuthenticatorActivityBinding binder;

    private AccountManager accountManager;

    private Intent intent = new Intent();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.setContentView(this, R.layout.authenticator_activity);
        ((BaseApplication) getApplication()).getAppComponent().inject(this);
        accountManager = AccountManager.get(this);
        progressDialog = DialogUtil.getProgressDialog(this);
        callbackManager = CallbackManager.Factory.create();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        exchangeSocialToken(account != null ? account.getIdToken() : null, AuthenticationCallInterface.GOOGLE_ISSUER);

        binder.closeAuthenticatorActivity.setOnClickListener(c -> {
                    setResult(RESULT_CANCELED);
                    finish();
                }
        );
        binder.loginInput.setOnClickListener(loginClickListener);
        binder.register.setOnClickListener(r -> startActivityForResult(new Intent(this, RegistrationActivity.class), RC_REGISTRATION));

        binder.facebookLogin.setPermissions(Arrays.asList("email", "public_profile", "user_hometown", "user_location"));
        binder.facebookLogin.registerCallback(callbackManager, getFacebookLoginCallback());

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.google_web_client_id))
                        .requestScopes(
                                new Scope(Scopes.PROFILE),
                                new Scope(Scopes.PLUS_ME),
                                new Scope(Scopes.EMAIL),
                                new Scope(Scopes.OPEN_ID))
                        .requestProfile()
                        .requestEmail()
                        .build());
        binder.googleLogin.setOnClickListener(g -> startActivityForResult(googleSignInClient.getSignInIntent(), RC_GOOGLE_SIGN_IN));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REGISTRATION && resultCode == RESULT_OK) {
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            loginWithCredentials(username, password);
        } else if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                exchangeSocialToken(account != null ? account.getIdToken() : null, AuthenticationCallInterface.GOOGLE_ISSUER);
            } catch (ApiException e) {
                LogErrorResponseUtil.logFailure(e, this);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private FacebookCallback<LoginResult> getFacebookLoginCallback() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                exchangeSocialToken(loginResult.getAccessToken() != null ? loginResult.getAccessToken().getToken() : null,
                        AuthenticationCallInterface.FACEBOOK_ISSUER);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                LogErrorResponseUtil.logFailure(exception, AuthenticatorActivity.this);
            }
        };
    }

    private void exchangeSocialToken(String idToken, String issuer) {
        if (idToken != null) {
            Call<KeycloakToken> authToken = authenticationCall.tokenExchange(AuthenticationCallInterface.CLIENT_ID,
                    AuthenticationCallInterface.CLIENT_SECRET,
                    AuthenticationCallInterface.TOKEN_EXCHANGE_GRANT_TYPE,
                    issuer,
                    idToken);
            consumeAuthTokenResponse(authToken);
        }
    }

    private View.OnClickListener loginClickListener = v -> {
        if (Strings.isEmptyOrWhitespace(binder.username.getText().toString()) ||
                Strings.isEmptyOrWhitespace(binder.password.getText().toString())) {
            return;
        }
        loginWithCredentials(binder.username.getText().toString(), binder.password.getText().toString());
    };

    private void loginWithCredentials(String username, String password) {
        Call<KeycloakToken> authToken = authenticationCall.accessToken(
                username,
                password,
                AuthenticationCallInterface.ACCESS_TOKEN_GRANT_TYPE);
        consumeAuthTokenResponse(authToken);
    }

    private void consumeAuthTokenResponse(Call<KeycloakToken> authToken) {
        progressDialog.show();
        authToken.enqueue(new Callback<KeycloakToken>() {
            @Override
            public void onResponse(Call<KeycloakToken> call, Response<KeycloakToken> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    JWT jwt = new JWT(response.body().getAccessToken());
                    String username = jwt.getClaim("preferred_username").asString();
                    Account account = addOrFindAccount(username, binder.password.getText().toString());
                    accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, response.body().getAccessToken());
                    accountManager.setUserData(account, REFRESH_TOKEN, response.body().getRefreshToken());
                    repository.syncAccountPodcasts(response.body().getAccessToken());
                    finishAccountAdd(intent, binder.username.getText().toString(), response.body().getAccessToken(), response.body().getRefreshToken());
                } else if (response.errorBody() != null) {
                    try {
                        String errorBodyString = response.errorBody().string();
                        Log.e(AuthenticatorActivity.class.getName(), "Error response " + errorBodyString);
                        if (errorBodyString.contains("Invalid user credentials")) {
                            Toasty.error(AuthenticatorActivity.this,
                                    getResources().getString(R.string.invalid_user_credentials),
                                    Toast.LENGTH_SHORT, true).show();
                        } else {
                            Toasty.error(AuthenticatorActivity.this,
                                    getResources().getString(R.string.error_response),
                                    Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (IOException e) {
                        Log.e(AuthenticatorActivity.class.getName(), "Error reading ErrorResponseBody ", e);
                        Toasty.error(AuthenticatorActivity.this,
                                getResources().getString(R.string.error_response),
                                Toast.LENGTH_SHORT, true).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<KeycloakToken> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(AuthenticatorActivity.class.getName(), "onFailure", t);
                Toasty.error(AuthenticatorActivity.this, getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private Account addOrFindAccount(String username, String password) {
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        Account account = accounts.length != 0 ? accounts[0] : new Account(username, ACCOUNT_TYPE);
        if (accounts.length == 0) {
            accountManager.addAccountExplicitly(account, password, null);
        } else {
            accountManager.setPassword(accounts[0], password);
        }
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

    private Context updateBaseContextLocale(Context context) {
        String language = ((BaseApplication) context.getApplicationContext()).getSharedPreferencesManager().getLocale();
        if (language == null) {
            return context;
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesLocale(context, locale);
        }

        return updateResourcesLocaleLegacy(context, locale);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private Context updateResourcesLocaleLegacy(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }

}
