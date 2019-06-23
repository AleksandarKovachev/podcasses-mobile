package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentEditAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.request.AccountRequest;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.EditAccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class EditAccountFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    private LiveData<String> token;

    private EditAccountViewModel viewModel;
    private static Account account;

    static EditAccountFragment newInstance(int instance, Account accountData) {
        Bundle args = new Bundle();
        account = accountData;
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        EditAccountFragment fragment = new EditAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentEditAccountBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_account, container, false);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditAccountViewModel.class);
        binder.setLifecycleOwner(this);
        binder.setViewModel(viewModel);
        binder.updateAccountBtn.setOnClickListener(updateAccountClickListener);
        token = AuthenticationUtil.getAuthenticationToken(getContext());
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setAccount(AccountRequest.toAccountRequest(account));
        viewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + account.getId());
        viewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + account.getId());
    }

    private View.OnClickListener updateAccountClickListener =
            v -> {
                LiveData<Account> response =
                        NetworkRequestsUtil.sendUpdateAccountRequest(apiCallInterface, token.getValue(), viewModel.getAccount(), getContext());
                response.observe(this, accountResponse -> {
                    fragmentNavigation.popFragment();
                    if (accountResponse != null) {
                        account = accountResponse;
                    }
                    response.removeObservers(getViewLifecycleOwner());
                });
            };

}
