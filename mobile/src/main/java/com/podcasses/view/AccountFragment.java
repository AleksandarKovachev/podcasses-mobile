package com.podcasses.view;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.auth0.android.jwt.JWT;
import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.ads.mediation.facebook.FacebookExtras;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.AccountList;
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

    private AdLoader adLoader;

    private MutableLiveData<String> token;

    private LiveData<ApiResponse> accountResponse;
    private LiveData<ApiResponse> podcastChannelsResponse;
    private LiveData<ApiResponse> podcastFiles;
    private LiveData<ApiResponse> accountLists;

    private static String accountId;

    private static Boolean isMyAccount = false;

    static AccountFragment newInstance(int instance, String openedAccountId, Boolean isMyAccount) {
        accountId = openedAccountId;
        AccountFragment.isMyAccount = isMyAccount;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        AccountFragment fragment = new AccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountViewModel.class);
        binding.setViewModel(viewModel);
        binding.refreshLayout.setOnRefreshListener(this);

        binding.add.addActionItem(
                new SpeedDialActionItem.Builder(
                        R.id.add_podcast_channel,
                        R.drawable.ic_add_podcast_channel)
                        .setLabel(R.string.podcast_channel_add).create());
        binding.add.setOnActionSelectedListener(actionItem -> {
            if (actionItem.getId() == R.id.add_podcast_channel) {
                fragmentNavigation.pushFragment(PodcastChannelAddFragment.newInstance(fragmentCount + 1));
            } else {
                fragmentNavigation.pushFragment(UploadFragment.newInstance(fragmentCount + 1));
            }
            return false;
        });
        setHasOptionsMenu(true);
        return binding.getRoot();
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
        token = AuthenticationUtil.getAuthenticationToken(getContext(), authenticationCallInterface);
        if (token == null && accountId == null) {
            handleNotAuthenticatedView();
        } else {
            if (accountId != null) {
                setAccountImages();
                setAuthenticationToken(false);
            } else {
                setAuthenticationToken(true);
            }
        }
        setPodcastChannelClick();
        setAccountListClick();
        setAuthorClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 22 &&
                data.getStringExtra(AccountManager.KEY_AUTHTOKEN) != null) {
            if (token == null) {
                token = new MutableLiveData<>();
            }
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
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
        if (token == null || Strings.isEmptyOrWhitespace(token.getValue())) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } else {
            getActivity().getMenuInflater().inflate(R.menu.account_navigation, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        if (token == null || token.getValue() == null || new JWT(token.getValue()).isExpired(0)) {
            setAuthenticationToken(false);
        }
        getAccountData(null, refreshLayout);
    }

    private void setAuthenticationToken(boolean additionalData) {
        if (token == null || token.getValue() != null && new JWT(token.getValue()).isExpired(0)) {
            token = AuthenticationUtil.getAuthenticationToken(getContext(), authenticationCallInterface);
        }
        if (token != null) {
            token.observe(getViewLifecycleOwner(), s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    binding.notAuthenticatedView.setVisibility(View.GONE);
                    binding.refreshLayout.setVisibility(View.VISIBLE);
                    token.removeObservers(this);
                    JWT jwt = new JWT(s);
                    if (accountId == null) {
                        accountId = jwt.getSubject();
                    }

                    setAccountAdditionalData(jwt, additionalData);
                    getAccountData(s, null);
                } else if (AccountManager.get(getContext()).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE).length != 0) {
                    token.removeObservers(this);
                    binding.notAuthenticatedView.setVisibility(View.GONE);
                    binding.refreshLayout.setVisibility(View.VISIBLE);
                    getAccountData(null, null);
                }
            });
        } else {
            getAccountData(null, null);
        }
    }

    private void setAccountAdditionalData(JWT jwt, boolean setAccountImages) {
        if (setAccountImages) {
            isMyAccount = true;
            setAccountImages();
        } else {
            if (accountId.equals(jwt.getSubject())) {
                isMyAccount = true;
            }
        }
    }

    private void setAccountImages() {
        viewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accountId);
    }

    private void getAccountData(String token, RefreshLayout refreshLayout) {
        if (accountId == null) {
            isMyAccount = true;
        }
        if (isMyAccount) {
            accountResponse = viewModel.account(this, null, accountId, refreshLayout != null, true);

            podcastFiles = viewModel.podcastFiles(this, token != null ? token : this.token.getValue(), refreshLayout != null);
            podcastFiles.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcastFiles, refreshLayout));

            accountLists = viewModel.accountLists(token != null ? token : this.token.getValue(), refreshLayout != null);
            accountLists.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, accountLists, refreshLayout));
        } else if (accountId != null) {
            accountResponse = viewModel.account(this, null, accountId, refreshLayout != null, false);
        }

        podcastChannelsResponse = viewModel.podcastChannels(this,
                token != null ? token : this.token.getValue(), accountId, null, isMyAccount, refreshLayout != null);

        podcastChannelsResponse.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcastChannelsResponse, refreshLayout));
        accountResponse.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, accountResponse, refreshLayout));
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData
            liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse);
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                setDataFromResponse(apiResponse);
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

    private void setDataFromResponse(@NonNull ApiResponse apiResponse) {
        if (apiResponse.data instanceof Account) {
            Account account = (Account) apiResponse.data;
            viewModel.setAccount(account);
        } else if (apiResponse.data instanceof List) {
            viewModel.setIsLoading(false);
            if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                binding.podcastFilesCardView.setVisibility(View.GONE);
                return;
            }
            if (((List) apiResponse.data).get(0) instanceof PodcastChannel) {
                if (binding.add.getActionItems().size() < 2) {
                    binding.add.addActionItem(
                            new SpeedDialActionItem.Builder(
                                    R.id.add_podcast,
                                    R.drawable.ic_headset_white)
                                    .setLabel(R.string.podcast_add).create());
                }
                viewModel.clearPodcastChannelsInAdapter();
                viewModel.setPodcastChannelsInAdapter((List<Object>) apiResponse.data);
                viewModel.setPodcastChannels(((List<Object>) apiResponse.data).size());

                if (((List<Object>) apiResponse.data).size() >= 3) {
                    addAds();
                }
            } else if (((List) apiResponse.data).get(0) instanceof AccountList) {
                viewModel.setAccountListsInAdapter((List<AccountList>) apiResponse.data);
                binding.accountListsView.setVisibility(View.VISIBLE);
            } else {
                viewModel.setPodcastFilesInAdapter((List<PodcastFile>) apiResponse.data);
                binding.podcastFilesCardView.setVisibility(View.VISIBLE);
            }
        }
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
            handleNotAuthenticatedView();
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

    private void addAds() {
        adLoader = new AdLoader.Builder(getContext(), BuildConfig.ACCOUNT_PODCAST_CHANNELS_NATIVE_ADS)
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    Log.i(getTag(), "Native Ad In Account Podcast Channels Loaded");
                    if (!adLoader.isLoading()) {
                        viewModel.addElementInPodcastChannelsAdapter(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.e(getTag(), "Native Ad In Account Podcast Channels Failed to loaded: " + errorCode);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .build())
                .build();
        adLoader.loadAds(new AdRequest.Builder().addNetworkExtrasBundle(FacebookAdapter.class, new FacebookExtras()
                .setNativeBanner(true)
                .build()).build(), 1);
    }

    private void setPodcastChannelClick() {
        viewModel.getSelectedPodcastChannel().observe(getViewLifecycleOwner(), podcastChannel -> {
            if (podcastChannel != null) {
                fragmentNavigation.pushFragment(PodcastChannelFragment.newInstance(fragmentCount + 1, podcastChannel.getId(), podcastChannel, false));
                viewModel.getSelectedPodcastChannel().setValue(null);
            }
        });
    }

    private void setAccountListClick() {
        viewModel.getSelectedAccountList().observe(getViewLifecycleOwner(), accountList -> {
            if (accountList != null) {
                fragmentNavigation.pushFragment(PodcastsPageFragment.newInstance(fragmentCount + 1, 0, accountList.getId()));
                viewModel.getSelectedAccountList().setValue(null);
            }
        });
    }

    private void setAuthorClick() {
        viewModel.getSelectedAuthor().observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, author, accountId.equals(author)));
                viewModel.getSelectedAuthor().setValue(null);
            }
        });
    }

    private void handleNotAuthenticatedView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        binding.bannerAdView.loadAd(new AdRequest.Builder().build());
        binding.notAuthenticatedView.setVisibility(View.VISIBLE);
        binding.refreshLayout.setVisibility(View.GONE);
        binding.notAuthenticatedView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
            intent.putExtra(AUTH_TOKEN_TYPE, AccountAuthenticator.AUTH_TOKEN_TYPE);
            startActivityForResult(intent, 22);
        });
    }

}
