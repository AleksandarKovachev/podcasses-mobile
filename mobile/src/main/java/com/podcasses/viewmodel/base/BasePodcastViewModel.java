package com.podcasses.viewmodel.base;

import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.AccountAdapter;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.PopupMenuUtil;

import java.util.Collections;
import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BasePodcastViewModel extends BaseViewModel {

    private MutableLiveData<List<Podcast>> podcasts = new MutableLiveData<>();
    private MutableLiveData<Podcast> selectedPodcast = new MutableLiveData<>();
    private ObservableField<String> selectedAccount = new ObservableField<>();
    private MutableLiveData<List<Account>> accounts = new MutableLiveData<>();
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, this);
    private PodcastAdapter trendingPodcastAdapter = new PodcastAdapter(R.layout.item_trending_podcast, this);
    private AccountAdapter accountAdapter = new AccountAdapter(this);

    private ApiCallInterface apiCallInterface;
    private String token;

    public BasePodcastViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public LiveData<ApiResponse> podcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId,
                                          String userId, boolean isSwipedToRefresh, boolean isMyAccount, int page) {
        if (!isSwipedToRefresh && podcasts.getValue() != null && !podcasts.getValue().isEmpty() && page == 0) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getPodcasts(lifecycleOwner, podcast, podcastId, userId, isMyAccount, isSwipedToRefresh, page);
    }

    public LiveData<ApiResponse> accountPodcasts(LifecycleOwner lifecycleOwner, String token, List<String> podcastIds, boolean isSwipedToRefresh) {
        this.token = token;
        return repository.getAccountPodcasts(lifecycleOwner, token, podcastIds, isSwipedToRefresh);
    }

    public PodcastAdapter getTrendingPodcastAdapter() {
        return trendingPodcastAdapter;
    }

    public void setTrendingPodcastsInAdapter(List<Podcast> podcasts) {
        this.podcasts.setValue(podcasts);
        this.trendingPodcastAdapter.setPodcasts(podcasts);
    }

    public PodcastAdapter getPodcastAdapter() {
        return podcastAdapter;
    }

    public AccountAdapter getAccountAdapter() {
        return accountAdapter;
    }

    public void clearPodcastsInAdapter() {
        this.podcasts.setValue(Collections.emptyList());
        this.podcastAdapter.setPodcasts(this.podcasts.getValue());
    }

    public void setAccountsInAdapter(List<Account> accounts) {
        this.accounts.setValue(accounts);
        this.accountAdapter.setAccounts(accounts);
    }

    public void setPodcastsInAdapter(List<Podcast> podcasts) {
        if (CollectionUtils.isEmpty(this.podcasts.getValue())) {
            this.podcasts.setValue(podcasts);
        } else {
            podcasts.removeAll(this.podcasts.getValue());
            this.podcasts.getValue().addAll(podcasts);
        }
        this.podcastAdapter.setPodcasts(this.podcasts.getValue());
    }

    public MutableLiveData<Podcast> getSelectedPodcast() {
        return selectedPodcast;
    }

    public void onPodcastClick(Integer index) {
        Podcast podcast = podcasts.getValue().get(index);
        selectedPodcast.setValue(podcast);
    }

    public ObservableField<String> getSelectedAccount() {
        return selectedAccount;
    }

    public void onAccountClickFromPodcast(Integer index) {
        selectedAccount.set(podcasts.getValue().get(index).getUserId());
    }

    public void onAccountClick(Integer index) {
        selectedAccount.set(accounts.getValue().get(index).getId());
    }

    public List<Podcast> getPodcasts() {
        return podcasts.getValue();
    }

    public Podcast getPodcastAt(Integer index) {
        if (podcasts.getValue() != null && index != null && podcasts.getValue().size() > index) {
            return podcasts.getValue().get(index);
        }
        return null;
    }

    public Account getAccountAt(Integer index) {
        if (accounts.getValue() != null && index != null && accounts.getValue().size() > index) {
            return accounts.getValue().get(index);
        }
        return null;
    }

    public String getAccountProfileImage(Integer index) {
        if (accounts.getValue() != null && index != null && accounts.getValue().size() > index) {
            return BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + accounts.getValue().get(index).getId();
        }
        return null;
    }

    public void onOptionsButtonClick(View view, Integer position) {
        PopupMenuUtil.podcastPopupMenu(view, podcasts.getValue().get(position), apiCallInterface, token);
    }

}
