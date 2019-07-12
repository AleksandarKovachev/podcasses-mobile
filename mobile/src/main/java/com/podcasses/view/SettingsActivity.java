package com.podcasses.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.SettingsActivityBinding;
import com.podcasses.model.response.Language;
import com.podcasses.view.base.BaseActivity;
import com.podcasses.viewmodel.SettingsViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class SettingsActivity extends BaseActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsActivityBinding binder = DataBindingUtil.setContentView(this, R.layout.settings_activity);
        ((BaseApplication) getApplication()).getAppComponent().inject(this);
        SettingsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel.class);
        binder.setLifecycleOwner(this);
        binder.setViewModel(viewModel);
        viewModel.setLocale(getLocale().getDisplayLanguage() + " " + getLocale().getCountry());
        LiveData<List<Language>> languages = viewModel.getLocales();
        languages.observe(this, l -> {
            if (l != null) {
                languages.removeObservers(this);
                viewModel.setLanguages(l);
            }
        });
    }

}
