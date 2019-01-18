package com.podcasses.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.retrofit.util.ConnectivityUtil;
import com.podcasses.retrofit.util.LoadingUtil;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private AccountViewModel accountViewModel;

    private ProgressDialog progressDialog;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentAccountBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);

        progressDialog = LoadingUtil.getProgressDialog(getContext(), "Loading...");

        binder.button.setOnClickListener(v -> {
            if (ConnectivityUtil.checkInternetConnection(getContext())) {
                accountViewModel.accountResponse("drage503").observe(this, this::consumeResponse);
            }
        });

        return binder.getRoot();
    }


    private void consumeResponse(ApiResponse apiResponse) {
        switch (apiResponse.status) {
            case LOADING:
                progressDialog.show();
                break;
            case SUCCESS:
                progressDialog.dismiss();
                renderSuccessResponse(apiResponse.data);
                break;
            case ERROR:
                progressDialog.dismiss();
                Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void renderSuccessResponse(JsonElement response) {
        if (!response.isJsonNull()) {
            Toast.makeText(getContext(), "success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "error response", Toast.LENGTH_SHORT).show();
        }
    }

}
