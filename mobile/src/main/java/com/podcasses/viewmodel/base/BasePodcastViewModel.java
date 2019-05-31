package com.podcasses.viewmodel.base;

import android.view.View;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.PopupMenuUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BasePodcastViewModel extends BaseViewModel {

    private ObservableInt playingIndex = new ObservableInt(-1);
    private MutableLiveData<List<Podcast>> podcasts = new MutableLiveData<>();
    private MutableLiveData<Podcast> selectedPodcast = new MutableLiveData<>();
    private ObservableField<String> selectedAccount = new ObservableField<>();
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, this);
    private PodcastAdapter trendingPodcastAdapter = new PodcastAdapter(R.layout.item_trending_podcast, this);
    private PodcastAdapter simplePodcastAdapter = new PodcastAdapter(R.layout.item_simple_podcast, this);

    private ApiCallInterface apiCallInterface;
    private String token;

    public BasePodcastViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public LiveData<ApiResponse> podcasts(LifecycleOwner lifecycleOwner, String podcast,
                                          String podcastId, String userId, boolean isSwipedToRefresh, boolean saveData) {
        if (!isSwipedToRefresh && podcasts.getValue() != null && !podcasts.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getPodcasts(lifecycleOwner, podcast, podcastId, userId, isSwipedToRefresh, saveData);
    }

    public LiveData<ApiResponse> accountPodcasts(String token, List<String> podcastIds) {
        this.token = token;
        if (podcasts.getValue() != null && !podcasts.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getAccountPodcasts(token, podcastIds);
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

    public void setPodcastsInAdapter(List<Podcast> podcasts) {
        this.podcasts.setValue(podcasts);
        this.podcastAdapter.setPodcasts(podcasts);
    }

    public PodcastAdapter getSimplePodcastAdapter() {
        return simplePodcastAdapter;
    }

    public void setPodcastsInSimpleAdapter(List<Podcast> podcasts) {
        this.podcasts.setValue(podcasts);
        this.simplePodcastAdapter.setPodcasts(podcasts);
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

    public void onPlayButtonClick(Integer index) {
        EventBus.getDefault().post(podcasts.getValue().get(index));
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

    @Bindable
    public Integer getPlayingIndex() {
        return playingIndex.get();
    }

    public void setPlayingIndex(Integer playingIndex) {
        this.playingIndex.set(playingIndex);
        notifyPropertyChanged(BR.playingIndex);
    }

    public void onOptionsButtonClick(View view, Integer position) {
        PopupMenuUtil.podcastPopupMenu(view, podcasts.getValue().get(position), apiCallInterface, token);
    }

}
