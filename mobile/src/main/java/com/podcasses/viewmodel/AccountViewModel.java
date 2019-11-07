package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.snackbar.Snackbar;
import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.AccountListAdapter;
import com.podcasses.adapter.PodcastFileAdapter;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.response.AccountList;
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

    private MutableLiveData<AccountList> selectedAccountList = new MutableLiveData<>();

    private MutableLiveData<Account> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<Integer> podcastChannels = new ObservableField<>(0);

    private MutableLiveData<List<PodcastFile>> podcastFiles = new MutableLiveData<>();
    private MutableLiveData<List<AccountList>> accountLists = new MutableLiveData<>();

    private PodcastFileAdapter podcastFileAdapter = new PodcastFileAdapter(this);
    private AccountListAdapter accountListAdapter = new AccountListAdapter(this);

    private String token;

    AccountViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public void removeAllLocalData() {
        repository.removeAllLocalData();
    }

    public LiveData<ApiResponse> account(LifecycleOwner lifecycleOwner, String username, String id, boolean isSwipedToRefresh, boolean isMyAccount) {
        if (!isSwipedToRefresh && account.getValue() != null && username != null && account.getValue().getUsername().equals(username)) {
            return new MutableLiveData<>(ApiResponse.fetched());
        }
        return repository.getAccount(lifecycleOwner, username, id, isSwipedToRefresh, isMyAccount);
    }

    public LiveData<ApiResponse> podcastFiles(LifecycleOwner lifecycleOwner, String token, boolean isSwipedToRefresh) {
        this.token = token;
        if (!isSwipedToRefresh && podcastFiles.getValue() != null && !podcastFiles.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.success(podcastFiles.getValue(), null));
        }
        return repository.getPodcastFiles(lifecycleOwner, token, isSwipedToRefresh);
    }

    public LiveData<ApiResponse> accountLists(String token, boolean isSwipedToRefresh) {
        if (!isSwipedToRefresh && accountLists.getValue() != null && !accountLists.getValue().isEmpty()) {
            return new MutableLiveData<>(ApiResponse.success(accountLists.getValue(), null));
        }
        return repository.getAccountLists(token, null);
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

    public AccountListAdapter getAccountListAdapter() {
        return accountListAdapter;
    }

    public void setAccountListsInAdapter(List<AccountList> accountLists) {
        this.accountLists.setValue(accountLists);
        this.accountListAdapter.setAccountLists(accountLists);
    }

    public AccountList getAccountListAt(Integer index) {
        if (accountLists.getValue() != null && index != null && accountLists.getValue().size() > index) {
            return accountLists.getValue().get(index);
        }
        return null;
    }

    public void onAccountListClick(Integer index) {
        selectedAccountList.setValue(accountLists.getValue().get(index));
    }

    public void onDeleteAccountList(View view, Integer index) {
        List<AccountList> listOfAccountLists = accountLists.getValue();
        AccountList accountList = listOfAccountLists.get(index);
        apiCallInterface.deleteAccountList("Bearer " + token, accountList.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listOfAccountLists.remove(accountList);
                    accountListAdapter.setAccountLists(listOfAccountLists);
                    Snackbar.make(view, view.getContext().getText(R.string.account_list_successfully_deleted), Snackbar.LENGTH_LONG)
                            .setAction(view.getContext().getText(R.string.undo), v -> apiCallInterface.activateAccountList("Bearer " + token, accountList.getId()).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        listOfAccountLists.add(index, accountList);
                                        accountListAdapter.setAccountLists(listOfAccountLists);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    LogErrorResponseUtil.logFailure(t, view.getContext());
                                }
                            })).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                LogErrorResponseUtil.logFailure(t, view.getContext());
            }
        });
    }

    public MutableLiveData<AccountList> getSelectedAccountList() {
        return selectedAccountList;
    }

    public void setProfileImage(String url) {
        profileImage.set(url);
        notifyPropertyChanged(BR.profileImage);
    }

    public void setAccount(Account account) {
        this.account.setValue(account);
        notifyPropertyChanged(BR.account);
    }

    public void setPodcastChannels(Integer podcastChannels) {
        this.podcastChannels.set(podcastChannels);
        notifyPropertyChanged(BR.podcastChannels);
    }

}
