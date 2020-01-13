package com.podcasses.viewmodel;

import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.adapter.PodcastChannelAdapter;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Language;
import com.podcasses.model.response.Nomenclature;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.util.PopupMenuUtil;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

public class HomeViewModel extends BasePodcastViewModel {

    private MutableLiveData<List<Object>> newPodcasts = new MutableLiveData<>();
    private MutableLiveData<TrendingFilter> trendingFilterMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<List<PodcastChannel>> newPodcastChannels = new MutableLiveData<>();

    private PodcastChannelAdapter newPodcastChannelsAdapter = new PodcastChannelAdapter(R.layout.item_new_podcast_channel_mini, this);
    private PodcastAdapter newPodcastAdapter = new PodcastAdapter(R.layout.item_new_podcast, R.layout.ad_native_trending, this);

    private Integer categoryId = -1, languageId = -1;
    private LifecycleOwner lifecycleOwner;

    private String token;

    private ApiCallInterface apiCallInterface;

    HomeViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
        this.apiCallInterface = apiCallInterface;
    }

    public PodcastAdapter getNewPodcastAdapter() {
        return newPodcastAdapter;
    }

    public void setNewPodcastsInAdapter(List<Object> podcasts) {
        this.newPodcasts.setValue(podcasts);
        this.newPodcastAdapter.setPodcasts(newPodcasts.getValue());
    }

    public LiveData<ApiResponse> getSubscribedPodcastChannels(String token) {
        this.token = token;
        return repository.getSubscribedPodcastChannels(lifecycleOwner, token);
    }

    public PodcastChannelAdapter getNewPodcastChannelAdapter() {
        return newPodcastChannelsAdapter;
    }

    public void setNewPodcastChannelsInAdapter(List<PodcastChannel> podcastChannels) {
        this.newPodcastChannels.setValue(podcastChannels);
        this.newPodcastChannelsAdapter.setPodcastChannels((List<Object>) (Object) this.newPodcastChannels.getValue());
    }

    public void onNewPodcastChannelClick(Integer index) {
        super.getSelectedPodcastChannel().set(newPodcastChannels.getValue().get(index));
    }

    public PodcastChannel getNewPodcastChannelAt(Integer index) {
        if (newPodcastChannels.getValue() != null && index != null && newPodcastChannels.getValue().size() > index) {
            return newPodcastChannels.getValue().get(index);
        }
        return null;
    }

    public LiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public LiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    public LiveData<ApiResponse> trendingPodcasts(LifecycleOwner lifecycleOwner, TrendingFilter filter, boolean isSwipedToRefresh) {
        return repository.getTrendingPodcasts(lifecycleOwner, filter, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> podcastChannels() {
        return repository.getPodcastChannel(null, null, null, null, false, false);
    }

    public LiveData<ApiResponse> newPodcasts(String token) {
        return repository.getNewPodcasts(token);
    }

    public MutableLiveData<TrendingFilter> getTrendingFilterMutableLiveData() {
        return trendingFilterMutableLiveData;
    }

    public void onFilterButtonClick(View view) {
        DialogUtil.createTrendingFilterDialog(view.getContext(), trendingFilterMutableLiveData, this, lifecycleOwner);
    }

    public Podcast getNewPodcastAt(Integer index) {
        if (newPodcasts.getValue() != null && index != null && newPodcasts.getValue().size() > index) {
            return (Podcast) newPodcasts.getValue().get(index);
        }
        return null;
    }

    public void onNewPodcastOptionsButtonClick(View view, Integer position, FragmentManager fragmentManager) {
        PopupMenuUtil.podcastPopupMenu(this, view, (Podcast) newPodcasts.getValue().get(position), apiCallInterface, token, fragmentManager);
    }

    public void onNewPodcastClick(Integer index) {
        Podcast podcast = (Podcast) newPodcasts.getValue().get(index);
        super.getSelectedPodcast().setValue(podcast);
    }

    public void openDatePicker(View view) {
        DialogUtil.openDatePicker((AppCompatEditText) view);
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

}
