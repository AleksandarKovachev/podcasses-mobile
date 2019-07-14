package com.podcasses.viewmodel;

import android.webkit.URLUtil;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.podcasses.BR;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.request.AccountRequest;
import com.podcasses.model.response.Language;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class EditAccountViewModel extends BaseViewModel {

    private MutableLiveData<AccountRequest> account = new MutableLiveData<>();
    private ObservableField<String> profileImage = new ObservableField<>();
    private ObservableField<String> coverImage = new ObservableField<>();

    private ApiCallInterface apiCallInterface;

    EditAccountViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
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

    public void verifyDisplayName(CharSequence displayName,
                                  AppCompatImageView displayNameStatus,
                                  ContentLoadingProgressBar progressBar,
                                  MaterialButton submitButton) {
        if (!Strings.isEmptyOrWhitespace(displayName.toString())) {
            NetworkRequestsUtil.displayNameVerify(apiCallInterface, displayName.toString(), displayNameStatus, progressBar, submitButton);
        }
    }

    public void verifyRssFeed(CharSequence rssFeed,
                              AppCompatTextView rssFeedEmail,
                              ContentLoadingProgressBar progressBar,
                              MaterialButton submitButton) {
        if (!Strings.isEmptyOrWhitespace(rssFeed.toString()) && URLUtil.isValidUrl(rssFeed.toString())) {
            NetworkRequestsUtil.rssFeedVerify(apiCallInterface, rssFeed.toString(), rssFeedEmail, progressBar, submitButton, account.getValue());
        }
    }

    @Bindable
    public AccountRequest getAccount() {
        return account.getValue();
    }

    public void setAccount(AccountRequest accountRequest) {
        this.account.setValue(accountRequest);
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
