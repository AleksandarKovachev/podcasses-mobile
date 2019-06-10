package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.PodcastFileAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountViewModel extends BasePodcastViewModel {

    private ApiCallInterface apiCallInterface;

    private MutableLiveData<Account> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<String> coverImage = new ObservableField<>();
    private ObservableField<Integer> accountSubscribes = new ObservableField<>();
    private ObservableField<Boolean> isSubscribed = new ObservableField();
    private ObservableField<String> editAccountId = new ObservableField<>();

    private MutableLiveData<List<PodcastFile>> podcastFiles = new MutableLiveData<>();
    private PodcastFileAdapter podcastFileAdapter = new PodcastFileAdapter(this);

    private String token;

    AccountViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
        this.apiCallInterface = apiCallInterface;
    }

    public LiveData<ApiResponse> account(LifecycleOwner lifecycleOwner, String username, String id, boolean isSwipedToRefresh, boolean isMyAccount) {
        if (!isSwipedToRefresh && account.getValue() != null && username != null && account.getValue().getUsername().equals(username)) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getAccount(lifecycleOwner, username, id, isMyAccount, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> accountSubscribes(@NonNull String accountId) {
        if (accountSubscribes.get() != null) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.checkAccountSubscribe(accountId);
    }

    public LiveData<ApiResponse> checkAccountSubscribe(String token, String accountId) {
        if (isSubscribed.get() != null) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.checkAccountSubscribe(token, accountId);
    }

    public LiveData<ApiResponse> podcastFiles(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh) {
        this.token = token;
        if (!isSwipedToRefresh && podcastFiles.getValue() != null && !podcastFiles.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getPodcastFiles(lifecycleOwner, token, isSwipedToRefresh);
    }

    public void saveAccount(Account account) {
        repository.saveAccount(account);
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
    public Integer getAccountSubscribes() {
        return accountSubscribes.get();
    }

    @Bindable
    public Boolean getIsSubscribed() {
        return isSubscribed.get();
    }

    public PodcastFileAdapter getPodcastFileAdapter() {
        return podcastFileAdapter;
    }

    public void setPodcastFilesInAdapter(List<PodcastFile> podcastFiles) {
        this.podcastFiles.setValue(podcastFiles);
        this.podcastFileAdapter.setPodcasts(podcastFiles);
    }

    public PodcastFile getPodcastFileAt(Integer index) {
        if (podcastFiles.getValue() != null && index != null && podcastFiles.getValue().size() > index) {
            return podcastFiles.getValue().get(index);
        }
        return null;
    }

    public void onDeletePodcastFile(View view, Integer position) {
        PodcastFile podcastFile = podcastFiles.getValue().get(position);
        Call<Void> call = apiCallInterface.deletePodcastFile("Bearer " + token, podcastFile.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    List<PodcastFile> newPodcastFiles = podcastFiles.getValue();
                    newPodcastFiles.remove(podcastFile);
                    setPodcastFilesInAdapter(newPodcastFiles);
                    repository.deletePodcastFile(podcastFile.getId());
                    Toasty.success(view.getContext(), view.getContext().getString(R.string.successfully_deleted_podcast_file), Toast.LENGTH_SHORT, true).show();
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, view.getContext());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, view.getContext());
            }
        });
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

    public void setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed.set(isSubscribed);
        notifyPropertyChanged(BR.isSubscribed);
    }

    public void setAccountSubscribes(Integer accountSubscribes) {
        this.accountSubscribes.set(accountSubscribes);
        notifyPropertyChanged(BR.accountSubscribes);
    }

    public void subscribeAccount(View view, String token, String accountId) {
        Call<Integer> subscribeAccountCall = apiCallInterface.accountSubscribe("Bearer " + token, accountId);
        subscribeAccountCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body() == 1) {
                        setIsSubscribed(true);
                        Toasty.success(view.getContext(), view.getContext().getString(R.string.successful_subscribing), Toast.LENGTH_SHORT, true).show();
                    } else {
                        setIsSubscribed(false);
                        Toasty.success(view.getContext(), view.getContext().getString(R.string.successful_unsubscribing), Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    LogErrorResponseUtil.logErrorResponse(response, view.getContext());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, view.getContext());
            }
        });
    }

    public ObservableField<String> getEditAccountId() {
        return editAccountId;
    }

    public void onEditClick(String accountId) {
        editAccountId.set(accountId);
    }

    public void onSyncClick(View view, String token, ContentLoadingProgressBar progressBar) {
        NetworkRequestsUtil.rssFeedSync(apiCallInterface, view.getContext(), token, progressBar);
    }

}
