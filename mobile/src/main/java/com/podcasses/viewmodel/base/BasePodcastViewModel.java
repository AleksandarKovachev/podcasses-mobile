package com.podcasses.viewmodel.base;

import android.view.View;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
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
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, this);
    private PodcastAdapter trendingPodcastAdapter = new PodcastAdapter(R.layout.item_trending_podcast, this);

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
        if (podcasts.getValue() != null && !podcasts.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
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

    public void clearPodcastsInAdapter() {
        this.podcasts.setValue(Collections.emptyList());
        this.podcastAdapter.setPodcasts(this.podcasts.getValue());
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

    public void onAccountClick(Integer index) {
        selectedAccount.set(podcasts.getValue().get(index).getUserId());
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

    public void onOptionsButtonClick(View view, Integer position) {
        PopupMenuUtil.podcastPopupMenu(view, podcasts.getValue().get(position), apiCallInterface, token);
    }

}
