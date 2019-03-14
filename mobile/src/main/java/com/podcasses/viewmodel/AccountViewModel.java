package com.podcasses.viewmodel;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.PodcastFileAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountViewModel extends BasePodcastViewModel {

    private MutableLiveData<Account> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<String> coverImage = new ObservableField<>();
    private ObservableField<String> accountSubscribes = new ObservableField<>();

    private MutableLiveData<List<PodcastFile>> podcastFiles = new MutableLiveData<>();
    private PodcastFileAdapter podcastFileAdapter = new PodcastFileAdapter(R.layout.item_podcast_file, this);

    AccountViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<ApiResponse> account(LifecycleOwner lifecycleOwner, @NonNull String username, boolean isSwipedToRefresh) {
        return repository.getAccount(lifecycleOwner, username, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> accountSubscribes(LifecycleOwner lifecycleOwner, @NonNull String accountId, boolean isSwipedToRefresh) {
        return repository.getAccountSubscribes(lifecycleOwner, accountId, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> podcasts(LifecycleOwner lifecycleOwner, String userId, boolean isSwipedToRefresh, boolean saveData) {
        return repository.getPodcasts(lifecycleOwner, null, null, userId, isSwipedToRefresh, saveData);
    }

    public LiveData<ApiResponse> podcastFiles(String token) {
        return repository.getPodcastFiles(token);
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

    public PodcastFileAdapter getPodcastFileAdapter() {
        return podcastFileAdapter;
    }

    public void setPodcastFilesInAdapter(List<PodcastFile> podcastFile) {
        this.podcastFiles.setValue(podcastFile);
        this.podcastFileAdapter.setPodcasts(podcastFile);
        this.podcastFileAdapter.notifyDataSetChanged();
    }

    public PodcastFile getPodcastFileAt(Integer index) {
        if (podcastFiles.getValue() != null && index != null && podcastFiles.getValue().size() > index) {
            return podcastFiles.getValue().get(index);
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

}
