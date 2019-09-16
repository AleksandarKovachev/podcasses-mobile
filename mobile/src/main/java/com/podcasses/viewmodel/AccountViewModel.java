package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.PodcastFileAdapter;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.Account;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.viewmodel.base.BasePodcastChannelViewModel;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountViewModel extends BasePodcastChannelViewModel {

    private ApiCallInterface apiCallInterface;

    private MutableLiveData<Account> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<Integer> podcastChannels = new ObservableField<>(0);

    private MutableLiveData<List<PodcastFile>> podcastFiles = new MutableLiveData<>();
    private PodcastFileAdapter podcastFileAdapter = new PodcastFileAdapter(this);

    private String token;

    AccountViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
        this.apiCallInterface = apiCallInterface;
    }

    public void removeAllLocalData() {
        repository.removeAllLocalData();
    }

    public LiveData<ApiResponse> account(String username, String id, boolean isSwipedToRefresh) {
        if (!isSwipedToRefresh && account.getValue() != null && username != null && account.getValue().getUsername().equals(username)) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getAccount(username, id, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> podcastChannels(@NonNull String accountId) {
        if (podcastChannels.get() != 0) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getAccountSubscribesCount(accountId);
    }

    public LiveData<ApiResponse> podcastFiles(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh) {
        this.token = token;
        if (!isSwipedToRefresh && podcastFiles.getValue() != null && !podcastFiles.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getPodcastFiles(lifecycleOwner, token, isSwipedToRefresh);
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
    public String getPodcastChannels() {
        return podcastChannels.get().toString();
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

    public void setAccount(Account account) {
        this.account.setValue(account);
        notifyPropertyChanged(BR.account);
    }

    public void setPodcastChannels(Integer accountPodcasts) {
        this.podcastChannels.set(accountPodcasts);
        notifyPropertyChanged(BR.podcastChannels);
    }

}
