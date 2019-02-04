package com.podcasses.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.auth0.android.jwt.JWT;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.authentication.KeycloakToken;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.util.List;

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

    private AccountViewModel accountViewModel;

    static AccountFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        AccountFragment fragment = new AccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentAccountBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        binder.setLifecycleOwner(this);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);
        binder.setViewModel(accountViewModel);

        LiveData<String> token = isAuthenticated();
        token.observe(this, s -> {
            JWT jwt = new JWT(s);
            String username = jwt.getClaim(KeycloakToken.PREFERRED_USERNAME_CLAIMS).asString();
            String accountId = jwt.getSubject();

            accountViewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accountId);
            accountViewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + accountId);

            LiveData<ApiResponse> accountResponse = accountViewModel.account(username);
            LiveData<ApiResponse> accountSubscribesResponse = accountViewModel.accountSubscribes(accountId);
            LiveData<ApiResponse> podcasts = accountViewModel.podcasts(this, null, null, accountId);

            podcasts.observe(this, this::consumeResponse);
            accountResponse.observe(this, this::consumeResponse);
            accountSubscribesResponse.observe(this, this::consumeResponse);
        });

        setListClick();

        return binder.getRoot();
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                if (apiResponse.data instanceof Account) {
                    accountViewModel.setAccount((Account) apiResponse.data);
                } else if (apiResponse.data instanceof Integer) {
                    accountViewModel.setAccountSubscribes(String.format(getString(R.string.subscribe), apiResponse.data));
                } else if (apiResponse.data instanceof List) {
                    accountViewModel.setPodcastsInAdapter((List<Podcast>) apiResponse.data);
                }
                break;
            case ERROR:
                Log.e(getTag(), "consumeResponse: ", apiResponse.error);
                break;
            default:
                break;
        }
    }

    private void setListClick() {
        accountViewModel.getSelected().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId()));
                accountViewModel.getSelected().setValue(null);
            }
        });
    }

}
