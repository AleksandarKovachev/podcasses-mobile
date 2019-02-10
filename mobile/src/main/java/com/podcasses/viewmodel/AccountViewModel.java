package com.podcasses.viewmodel;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.BR;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.podcasses.util.CustomViewBindings.PODCAST_IMAGE;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountViewModel extends BasePodcastViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private MutableLiveData<Account> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<String> coverImage = new ObservableField<>();
    private ObservableField<String> accountSubscribes = new ObservableField<>();
    private MutableLiveData<List<Podcast>> podcasts = new MutableLiveData<>();
    private MutableLiveData<Podcast> selected = new MutableLiveData<>();
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, this);

    AccountViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<ApiResponse> account(LifecycleOwner lifecycleOwner, @NonNull String username, boolean isSwipedToRefresh) {
        return repository.getAccount(lifecycleOwner, username, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> accountSubscribes(LifecycleOwner lifecycleOwner, @NonNull String accountId, boolean isSwipedToRefresh) {
        return repository.getAccountSubscribes(lifecycleOwner, accountId, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> podcasts(LifecycleOwner lifecycleOwner, String podcast, String podcastId, String userId, boolean isSwipedToRefresh) {
        return repository.getPodcasts(lifecycleOwner, podcast, podcastId, userId, isSwipedToRefresh);
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    @Bindable
    public Account getAccount() {
        return account.getValue();
    }

    @Bindable
    public String getProfileImage() {
        return profileImage.get();
    }

    @Bindable
    public String getCoverImage() {
        return coverImage.get();
    }

    @Bindable
    public String getAccountSubscribes() {
        return accountSubscribes.get();
    }

    public PodcastAdapter getPodcastAdapter() {
        return podcastAdapter;
    }

    public void setPodcastsInAdapter(List<Podcast> podcasts) {
        this.podcasts.setValue(podcasts);
        this.podcastAdapter.setPodcasts(podcasts);
        this.podcastAdapter.notifyDataSetChanged();
    }

    public MutableLiveData<Podcast> getSelected() {
        return selected;
    }

    @Override
    public void onItemClick(Integer index) {
        Podcast podcast = podcasts.getValue().get(index);
        selected.setValue(podcast);
    }

    @Override
    public Podcast getPodcastAt(Integer index) {
        if (podcasts.getValue() != null && index != null && podcasts.getValue().size() > index) {
            return podcasts.getValue().get(index);
        }
        return null;
    }

    @Override
    public String podcastImage(Integer position) {
        if (!CollectionUtils.isEmpty(podcasts.getValue())) {
            Podcast podcast = podcasts.getValue().get(position);
            return BuildConfig.API_GATEWAY_URL.concat(PODCAST_IMAGE).concat(podcast.getId());
        }
        return null;
    }

    public void setProfileImage(String url) {
        profileImage.set(url);
        notifyPropertyChanged(BR.profileImage);
    }

    public void setCoverImage(String url) {
        coverImage.set(url);
        notifyPropertyChanged(BR.coverImage);
    }

    public void setAccount(Account account) {
        this.account.setValue(account);
        notifyPropertyChanged(BR.account);
    }

    public void setAccountSubscribes(String accountSubscribes) {
        this.accountSubscribes.set(accountSubscribes);
        notifyPropertyChanged(BR.accountSubscribes);
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
