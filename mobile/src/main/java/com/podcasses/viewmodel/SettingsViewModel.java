package com.podcasses.viewmodel;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;

import com.podcasses.R;
import com.podcasses.adapter.LanguageAdapter;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.response.Language;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.util.DialogUtil;
import com.podcasses.view.AgreementActivity;
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

    public List<Language> getLanguages() {
        return languages;
    }

    public void termOfServiceClick(View view) {
        startAgreementActivity(view, 0);
    }

    public void privacyPolicyClick(View view) {
        startAgreementActivity(view, 1);
    }

    private void startAgreementActivity(View view, int agreement) {
        Intent intent = new Intent(view.getContext(), AgreementActivity.class);
        intent.putExtra("agreement", agreement);
        view.getContext().startActivity(intent);
    }

}
