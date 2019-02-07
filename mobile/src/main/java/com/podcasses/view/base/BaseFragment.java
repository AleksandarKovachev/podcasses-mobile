package com.podcasses.view.base;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.authentication.InvalidateToken;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.retrofit.util.ConnectivityUtil;
import com.podcasses.view.AuthenticatorActivity;

import java.net.ConnectException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static android.app.Activity.RESULT_OK;
import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseFragment extends Fragment {

    public static final String ARGS_INSTANCE = "com.podcasses.argsInstance";

    protected FragmentNavigation fragmentNavigation;
    protected int fragmentCount;

    private MutableLiveData<String> token = new MutableLiveData<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            fragmentCount = args.getInt(ARGS_INSTANCE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentNavigation) {
            fragmentNavigation = (FragmentNavigation) context;
        }
    }

    protected LiveData<String> isAuthenticated() {
        AccountManager accountManager = AccountManager.get(getContext());
        Account[] accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);

        if (accounts.length == 0) {
            startAuthenticatorActivity();
        } else {
            String authToken = accountManager.peekAuthToken(accounts[0], AUTH_TOKEN_TYPE);
            if (KeycloakToken.isValidToken(authToken)) {
                token.setValue(authToken);
            } else if (ConnectivityUtil.checkInternetConnection(getContext())) {
                InvalidateToken invalidateToken = new InvalidateToken(accountManager, accounts[0]);
                try {
                    token.setValue(invalidateToken.execute(authToken).get());
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), "isAuthenticated: ", e);
                }
            }
        }
        return token;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 22) {
            token.setValue(data.getStringExtra(AccountManager.KEY_AUTHTOKEN));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startAuthenticatorActivity() {
        Intent intent = new Intent(getContext(), AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
        intent.putExtra(AUTH_TOKEN_TYPE, AccountAuthenticator.AUTH_TOKEN_TYPE);
        startActivityForResult(intent, 22);
    }

    public interface FragmentNavigation {
        void pushFragment(Fragment fragment);
    }

    protected void logError(@NonNull ApiResponse apiResponse) {
        Log.e(getTag(), "consumeResponse: ", apiResponse.error);
        if (apiResponse.error instanceof ConnectException) {
            Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.could_not_fetch_data), Toast.LENGTH_SHORT).show();
        }
    }

}
