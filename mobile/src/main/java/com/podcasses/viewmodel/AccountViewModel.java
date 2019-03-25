package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.PodcastFileAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private ObservableField<String> accountSubscribes = new ObservableField<>();

    private MutableLiveData<List<PodcastFile>> podcastFiles = new MutableLiveData<>();
    private PodcastFileAdapter podcastFileAdapter = new PodcastFileAdapter(R.layout.item_podcast_file, this);

    private String token;

    AccountViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
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

    public LiveData<ApiResponse> podcastFiles(LifecycleOwner lifecycleOwner, String token, String userId, boolean isSwipedToRefresh) {
        this.token = token;
        return repository.getPodcastFiles(lifecycleOwner, token, userId, isSwipedToRefresh);
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

    public void setPodcastFilesInAdapter(List<PodcastFile> podcastFiles) {
        this.podcastFiles.setValue(podcastFiles);
        this.podcastFileAdapter.setPodcasts(podcastFiles);
        this.podcastFileAdapter.notifyDataSetChanged();
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
                    Toasty.error(view.getContext(), view.getContext().getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toasty.error(view.getContext(), view.getContext().getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
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

    public void setAccountSubscribes(String accountSubscribes) {
        this.accountSubscribes.set(accountSubscribes);
        notifyPropertyChanged(BR.accountSubscribes);
    }

}
