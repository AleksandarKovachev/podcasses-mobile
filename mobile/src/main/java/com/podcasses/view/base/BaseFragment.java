package com.podcasses.view.base;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.view.AuthenticatorActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import static android.app.Activity.RESULT_OK;
import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseFragment extends Fragment implements AuthenticationTokenTask {

    public static final String ARGS_INSTANCE = "com.podcasses.argsInstance";

    protected FragmentNavigation fragmentNavigation;
    protected int fragmentCount;

    private MutableLiveData<String> token;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 22) {
            token.setValue(data.getStringExtra(AccountManager.KEY_AUTHTOKEN));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void startAuthenticationActivity(MutableLiveData<String> token) {
        this.token = token;
        Intent intent = new Intent(getContext(), AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
        intent.putExtra(AUTH_TOKEN_TYPE, AccountAuthenticator.AUTH_TOKEN_TYPE);
        startActivityForResult(intent, 22);
    }

    public interface FragmentNavigation {
        void pushFragment(Fragment fragment);
    }

}
