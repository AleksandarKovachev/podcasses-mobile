package com.podcasses.view;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.AuthenticationCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.podcasses.authentication.AccountAuthenticator.AUTH_TOKEN_TYPE;
import static com.podcasses.authentication.AccountAuthenticator.REFRESH_TOKEN;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountFragment extends BaseFragment implements OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    AuthenticationCallInterface authenticationCallInterface;

    private AccountViewModel viewModel;
    private FragmentAccountBinding binding;

    private MutableLiveData<String> token;

    private LiveData<ApiResponse> accountResponse;
    private LiveData<ApiResponse> accountSubscribesResponse;
    private LiveData<ApiResponse> accountPodcastsCountResponse;
    private LiveData<ApiResponse> checkAccountSubscribeResponse;
    private LiveData<ApiResponse> podcasts;
    private LiveData<ApiResponse> podcastFiles;

    private Account account;
    private static String accountId;

    private boolean isMyAccount = false;
    private int page = 0;

    static AccountFragment newInstance(int instance, String openedAccountId) {
        accountId = openedAccountId;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);
        binding.setViewModel(viewModel);
        binding.refreshLayout.setOnRefreshListener(this);
        token = new MutableLiveData<>();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    void updateActionBar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        token = AuthenticationUtil.getAuthenticationToken(getContext());
        if (token == null) {
            binding.notAuthenticatedView.setVisibility(View.VISIBLE);
            binding.refreshLayout.setVisibility(View.GONE);
            binding.notAuthenticatedView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AuthenticatorActivity.class);
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                intent.putExtra(AUTH_TOKEN_TYPE, AccountAuthenticator.AUTH_TOKEN_TYPE);
                startActivityForResult(intent, 22);
            });
        } else {
            if (accountId != null) {
                setAccountImages();
                setAuthenticationToken(false);
            } else {
                setAuthenticationToken(true);
            }
        }
        setPodcastClick();
        setAccountClick();
        setAccountEditClick();
        setInfiniteScrollListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 22 &&
                data.getStringExtra(AccountManager.KEY_AUTHTOKEN) != null) {
            if (token == null) {
                token = new MutableLiveData<>();
            }
            token.setValue(data.getStringExtra(AccountManager.KEY_AUTHTOKEN));
            binding.notAuthenticatedView.setVisibility(View.GONE);
            binding.refreshLayout.setVisibility(View.VISIBLE);
            setAuthenticationToken(true);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.account_navigation, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_upload:
                fragmentNavigation.pushFragment(UploadFragment.newInstance(fragmentCount + 1));
                break;
            case R.id.navigation_history:
                fragmentNavigation.pushFragment(HistoryFragment.newInstance(fragmentCount + 1));
                break;
            case R.id.navigation_logout:
                handleLogout();
                break;
        }
        return true;
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        page = 0;
        if (token == null || token.getValue() == null || new JWT(token.getValue()).isExpired(0)) {
            setAuthenticationToken(false);
        }
        getAccountData(null, refreshLayout);
    }

    private void setAuthenticationToken(boolean additionalData) {
        if (token == null || token.getValue() != null && new JWT(token.getValue()).isExpired(0)) {
            token = AuthenticationUtil.getAuthenticationToken(getContext());
        }
        if (token != null) {
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    binding.notAuthenticatedView.setVisibility(View.GONE);
                    binding.refreshLayout.setVisibility(View.VISIBLE);
                    token.removeObservers(this);
                    JWT jwt = new JWT(s);
                    binding.setToken(s);
                    if (accountId == null) {
                        accountId = jwt.getSubject();
                    }
                    binding.setAccountId(accountId);

                    setAccountAdditionalData(jwt, additionalData);
                    getAccountData(s, null);
                } else if (AccountManager.get(getContext()).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE).length != 0) {
                    binding.notAuthenticatedView.setVisibility(View.GONE);
                    binding.refreshLayout.setVisibility(View.VISIBLE);
                    getAccountData(null, null);
                }
            });
        }
    }

    private void setAccountAdditionalData(JWT jwt, boolean setAccountImages) {
        if (setAccountImages) {
            binding.setIsMyAccount(true);
            isMyAccount = true;
            setAccountImages();
        } else {
            if (accountId.equals(jwt.getSubject())) {
                binding.setIsMyAccount(true);
                isMyAccount = true;
            }
        }
    }

    private void setAccountImages() {
        viewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accountId);
        viewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + accountId);
    }

    private void getAccountData(String token, RefreshLayout refreshLayout) {
        if (accountId == null) {
            isMyAccount = true;
        }
        loadPodcasts(isMyAccount, refreshLayout);

        if (isMyAccount) {
            accountResponse = viewModel.account(this, null, accountId, refreshLayout != null, true);
            podcastFiles = viewModel.podcastFiles(this, token != null ? token : this.token.getValue(), refreshLayout != null);
            podcastFiles.observe(this, apiResponse -> consumeResponse(apiResponse, podcastFiles, refreshLayout));
        } else if (accountId != null) {
            accountResponse = viewModel.account(this, null, accountId, refreshLayout != null, false);
            checkAccountSubscribeResponse = viewModel.checkAccountSubscribe(token != null ? token : this.token.getValue(), accountId);
            checkAccountSubscribeResponse.observe(this, apiResponse -> consumeResponse(apiResponse, checkAccountSubscribeResponse, refreshLayout));
        }
        accountResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountResponse, refreshLayout));

        if (accountId != null) {
            accountSubscribesResponse = viewModel.accountSubscribes(accountId);
            accountPodcastsCountResponse = viewModel.accountPodcastsCount(token, accountId);
            accountSubscribesResponse.observe(this, apiResponse -> consumeIntegerResponse(apiResponse, accountSubscribesResponse, true));
            accountPodcastsCountResponse.observe(this, apiResponse -> consumeIntegerResponse(apiResponse, accountPodcastsCountResponse, false));
        }
    }

    private void consumeIntegerResponse(ApiResponse apiResponse, LiveData liveData, boolean isSubscribes) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                if (apiResponse.data == null) {
                    return;
                }
                if (isSubscribes) {
                    viewModel.setAccountSubscribes((Integer) apiResponse.data);
                } else {
                    viewModel.setAccountPodcasts((Integer) apiResponse.data);
                }
                break;
            case SUCCESS:
                if (apiResponse.data == null) {
                    return;
                }
                liveData.removeObservers(this);
                if (isSubscribes) {
                    viewModel.setAccountSubscribes((Integer) apiResponse.data);
                } else {
                    viewModel.setAccountPodcasts((Integer) apiResponse.data);
                }
                break;
            case ERROR:
                viewModel.setIsLoading(false);
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            case FETCHED:
                viewModel.setIsLoading(false);
                liveData.removeObservers(this);
                break;
        }
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData
            liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse, refreshLayout != null);
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    viewModel.clearPodcastsInAdapter();
                    refreshLayout.finishRefresh();
                }
                setDataFromResponse(apiResponse, refreshLayout != null);
                break;
            case ERROR:
                viewModel.setIsLoading(false);
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            case FETCHED:
                viewModel.setIsLoading(false);
                liveData.removeObservers(this);
                break;
        }
    }

    private void setDataFromResponse(@NonNull ApiResponse apiResponse, boolean isSwipedToRefresh) {
        if (apiResponse.data instanceof Account) {
            account = (Account) apiResponse.data;
            viewModel.setAccount(account);
            if (viewModel.getIsSubscribed() && account != null) {
                viewModel.saveAccount(account);
            }
        } else if (apiResponse.data instanceof Boolean) {
            boolean isSubscribed = (boolean) apiResponse.data;
            viewModel.setIsSubscribed(isSubscribed);
            if (isSubscribed && account != null) {
                viewModel.saveAccount(account);
            }
        } else if (apiResponse.data instanceof List) {
            viewModel.setIsLoading(false);
            if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                binding.podcastFilesCardView.setVisibility(View.GONE);
                return;
            }
            if (((List) apiResponse.data).get(0) instanceof Podcast) {
                viewModel.setPodcastsInAdapter((List<Podcast>) apiResponse.data);
                getAccountPodcasts((List<Podcast>) apiResponse.data, isSwipedToRefresh);
            } else {
                viewModel.setPodcastFilesInAdapter((List<PodcastFile>) apiResponse.data);
                binding.podcastFilesCardView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setPodcastClick() {
        viewModel.getSelectedPodcast().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setAccountClick() {
        viewModel.getSelectedAccount().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedAccount().get() != null) {
                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAccount().get()));
                    viewModel.getSelectedAccount().set(null);
                }
            }
        });
    }

    private void setAccountEditClick() {
        viewModel.getEditAccountId().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getEditAccountId().get() != null) {
                    fragmentNavigation.pushFragment(EditAccountFragment.newInstance(fragmentCount + 1, account));
                    viewModel.getEditAccountId().set(null);
                }
            }
        });
    }

    private void getAccountPodcasts(List<Podcast> podcasts, boolean isSwipedToRefresh) {
        List<String> podcastIds = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            podcastIds.add(podcast.getId());
        }

        LiveData<ApiResponse> accountPodcasts = viewModel.accountPodcasts(this, token.getValue(), podcastIds, isSwipedToRefresh);
        accountPodcasts.observe(this, response -> consumeAccountPodcasts(response, accountPodcasts));
    }

    private void consumeAccountPodcasts(ApiResponse accountPodcastsResponse, LiveData<ApiResponse> liveData) {
        switch (accountPodcastsResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setAccountPodcastsData(accountPodcastsResponse);
                break;
            case SUCCESS: {
                liveData.removeObservers(this);
                setAccountPodcastsData(accountPodcastsResponse);
                break;
            }
            case ERROR: {
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(accountPodcastsResponse, getContext());
                break;
            }
        }
    }

    private void setAccountPodcastsData(ApiResponse accountPodcastsResponse) {
        if (!CollectionUtils.isEmpty((List<AccountPodcast>) accountPodcastsResponse.data)) {
            for (Podcast podcast : viewModel.getPodcasts()) {
                for (AccountPodcast accountPodcast : (List<AccountPodcast>) accountPodcastsResponse.data) {
                    if (accountPodcast.getPodcastId().equals(podcast.getId())) {
                        podcast.setMarkAsPlayed(accountPodcast.getMarkAsPlayed() == 1);
                    }
                }
            }
        }
    }

    private void setInfiniteScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                if (!viewModel.getIsLoading()) {
                    ++page;
                    viewModel.setIsLoading(true);
                    loadPodcasts(isMyAccount, null);
                }
            }
        });
    }

    private void loadPodcasts(boolean isMyAccount, RefreshLayout refreshLayout) {
        podcasts = viewModel.podcasts(this, null, null, accountId, refreshLayout != null, isMyAccount, page);
        podcasts.observe(this, apiResponse -> consumeResponse(apiResponse, podcasts, refreshLayout));
    }

    private void handleLogout() {
        AccountManager accountManager = AccountManager.get(getContext());
        android.accounts.Account[] accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            String authToken = accountManager.peekAuthToken(accounts[0], AUTH_TOKEN_TYPE);
            String refreshToken = accountManager.getUserData(accounts[0], REFRESH_TOKEN);
            authenticationCallInterface.logout(AuthenticationCallInterface.TOKEN_TYPE + authToken,
                    AuthenticationCallInterface.CLIENT_ID,
                    AuthenticationCallInterface.CLIENT_SECRET,
                    refreshToken).enqueue(logoutRequest(accounts[0], accountManager));
        }
    }

    private Callback<Void> logoutRequest(android.accounts.Account account, AccountManager
            accountManager) {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                viewModel.removeAllLocalData();
                accountManager.removeAccount(account, null, null);
                Toast.makeText(getContext(), getString(R.string.successful_logout), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_response), Toast.LENGTH_SHORT).show();
            }
        };
    }

}
