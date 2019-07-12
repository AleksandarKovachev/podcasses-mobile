package com.podcasses.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.AgreementActivityBinding;
import com.podcasses.view.base.BaseActivity;
import com.podcasses.viewmodel.AgreementViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class AgreementActivity extends BaseActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AgreementActivityBinding binder = DataBindingUtil.setContentView(this, R.layout.agreement_activity);
        ((BaseApplication) getApplication()).getAppComponent().inject(this);
        AgreementViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AgreementViewModel.class);
        binder.setLifecycleOwner(this);
        binder.setViewModel(viewModel);

        LiveData<String> agreement;
        int agreementBundleKey = getAgreementBundleKey(savedInstanceState);
        if (agreementBundleKey == 0) {
            setTitle(R.string.settings_terms_of_service);
            agreement = viewModel.getTermOfService();
        } else {
            setTitle(R.string.settings_privacy_policy);
            agreement = viewModel.getPrivacyPolicy();
        }
        agreement.observe(this, a -> {
            agreement.removeObservers(this);
            viewModel.setAgreement(a);
        });
    }

    private int getAgreementBundleKey(Bundle savedInstanceState) {
        int agreementBundleKey = 0;
        if (savedInstanceState != null) {
            agreementBundleKey = savedInstanceState.getInt("agreement");
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                agreementBundleKey = extras.getInt("agreement");
            }
        }
        return agreementBundleKey;
    }

}
