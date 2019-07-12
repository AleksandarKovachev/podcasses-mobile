package com.podcasses.viewmodel;

import android.text.Spanned;

import androidx.core.text.HtmlCompat;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;

import com.google.android.gms.common.util.Strings;
import com.podcasses.BR;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.viewmodel.base.BaseViewModel;

/**
 * Created by aleksandar.kovachev.
 */
public class AgreementViewModel extends BaseViewModel {

    private ObservableField<String> agreement = new ObservableField<>();

    AgreementViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<String> getTermOfService() {
        return repository.getTermOfService();
    }

    public LiveData<String> getPrivacyPolicy() {
        return repository.getPrivacyPolicy();
    }

    @Bindable
    public Spanned getAgreement() {
        if (Strings.isEmptyOrWhitespace(agreement.get())) {
            return null;
        }
        return HtmlCompat.fromHtml(agreement.get(), 0);
    }

    public void setAgreement(String agreement) {
        this.agreement.set(agreement);
        notifyPropertyChanged(BR.agreement);
    }

}
