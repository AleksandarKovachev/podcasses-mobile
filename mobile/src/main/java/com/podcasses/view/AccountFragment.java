package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.podcasses.R;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    Gson gson;

    private Account account = new Account();

    private AccountViewModel accountViewModel;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentAccountBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        binder.setLifecycleOwner(this);
        binder.setAccount(account);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);

        LiveData<String> token = isAuthenticated();
        token.observe(this, s -> {
            JWT jwt = new JWT(s);
            LiveData<ApiResponse> apiResponse = accountViewModel.account(jwt.getClaim(KeycloakToken.PREFERRED_USERNAME_CLAIMS).asString());
            apiResponse.observe(this, this::consumeResponse);
        });

        return binder.getRoot();
    }


    private void consumeResponse(@NonNull ApiResponse apiResponse) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                renderSuccessResponse(apiResponse.data);
                break;
            case ERROR:
                Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void renderSuccessResponse(JsonElement response) {
        if (!response.isJsonNull()) {
            account.consume(gson.fromJson(response, Account.class));
        }
    }

}
