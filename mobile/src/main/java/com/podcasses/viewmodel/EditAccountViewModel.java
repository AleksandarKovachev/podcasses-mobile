package com.podcasses.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.Language;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class EditAccountViewModel extends BaseViewModel {

    private MutableLiveData<Account> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<String> coverImage = new ObservableField<>();

    EditAccountViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public LiveData<List<Nomenclature>> getCountries() {
        return repository.getCountries();
    }

    public LiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    @Bindable
    public Account getAccount() {
        return account.getValue();
    }

    public void setAccount(Account account) {
        this.account.setValue(account);
        notifyPropertyChanged(BR.account);
    }

    public String getProfileImage() {
        return profileImage.get();
    }

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

}
