package com.podcasses.view.base;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.authentication.InvalidateToken;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.model.base.BaseUserModel;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.util.ConnectivityUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.AuthenticatorActivity;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

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
                    Log.e(getTag(), "isAuthenticated: ", e);
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

    protected void setAccounts(BaseViewModel viewModel, List<? extends BaseUserModel> baseUserModels) {
        List<String> accountIds = new ArrayList<>();
        for (BaseUserModel userModel : baseUserModels) {
            if (!accountIds.contains(userModel.getUserId())) {
                accountIds.add(userModel.getUserId());
            }
        }
        LiveData<ApiResponse> accounts = viewModel.accounts(accountIds);
        accounts.observe(this, response -> consumeAccountsData(response, accounts, baseUserModels));
    }

    private void consumeAccountsData(ApiResponse apiResponse, LiveData liveData, List<? extends BaseUserModel> baseUserModels) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                for (BaseUserModel userModel : baseUserModels) {
                    for (com.podcasses.model.entity.Account account : (List<com.podcasses.model.entity.Account>) apiResponse.data) {
                        if (userModel.getUserId().equals(account.getKeycloakId())) {
                            userModel.setUsername(account.getUsername());
                            break;
                        }
                    }
                }
                break;
            case ERROR:
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
        }
    }

}
