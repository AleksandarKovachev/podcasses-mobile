package com.podcasses.viewmodel;

import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;

import com.podcasses.model.response.Language;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.util.DialogUtil;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class SettingsViewModel extends BaseViewModel {

    private List<Language> languages;
    private ObservableField<String> locale = new ObservableField<>();

    SettingsViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<List<Language>> getLocales() {
        return repository.getLocales();
    }

    public ObservableField<String> getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale.set(locale);
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public void onLocaleClick(View view) {
        DialogUtil.createLocaleDialog(view.getContext(), languages);
    }

}
