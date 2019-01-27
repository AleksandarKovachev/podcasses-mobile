package com.podcasses.viewmodel;

import com.podcasses.BR;
import com.podcasses.model.entity.Account;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.viewmodel.base.BaseViewModel;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private MutableLiveData<Account> account = new MutableLiveData<>();

    private ObservableField<String> profileImage = new ObservableField<>();

    private ObservableField<String> coverImage = new ObservableField<>();

    public AccountViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<ApiResponse> account(@NonNull String username) {
        return repository.getAccount(username);
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

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
