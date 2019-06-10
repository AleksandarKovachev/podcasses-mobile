package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.ApiResponse;
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
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountFragment extends BaseFragment implements OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    private AccountViewModel viewModel;
    private FragmentAccountBinding binding;

    private LiveData<String> token;

    private LiveData<ApiResponse> accountResponse;
    private LiveData<ApiResponse> accountSubscribesResponse;
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (accountId != null) {
            setAccountImages();
            setAuthenticationToken(false);
        } else {
            setAuthenticationToken(true);
        }

        setPodcastClick();
        setAccountClick();
        setAccountEditClick();
        setInfiniteScrollListener();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        page = 0;
        if (token == null || token.getValue() == null || token.getValue() != null && new JWT(token.getValue()).isExpired(new Date().getTime())) {
            setAuthenticationToken(false);
        }
        getAccountData(null, refreshLayout);
    }

    void updateTitle() {
        if (account != null && getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(account.getUsername());
    }

    private void setAuthenticationToken(boolean additionalData) {
        token = AuthenticationUtil.isAuthenticated(getContext(), this);
        token.observe(this, s -> {
            if (!Strings.isEmptyOrWhitespace(s)) {
                token.removeObservers(this);
                JWT jwt = new JWT(s);
                binding.setToken(s);
                if (accountId == null) {
                    accountId = jwt.getSubject();
                }
                binding.setAccountId(accountId);

                setAccountAdditionalData(jwt, additionalData);
                getAccountData(s, null);
            } else {
                getAccountData(null, null);
            }
        });
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
        accountSubscribesResponse = viewModel.accountSubscribes(accountId);
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
        accountSubscribesResponse.observe(this, apiResponse -> consumeResponse(apiResponse, accountSubscribesResponse, refreshLayout));
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
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
            updateTitle();
            viewModel.setAccount(account);
        } else if (apiResponse.data instanceof Integer) {
            viewModel.setAccountSubscribes((Integer) apiResponse.data);
        } else if (apiResponse.data instanceof Boolean) {
            boolean isSubscribed = (boolean) apiResponse.data;
            viewModel.setIsSubscribed(isSubscribed);
            if (isSubscribed) {
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
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
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

}
