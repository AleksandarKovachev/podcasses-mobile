package com.podcasses.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.podcasses.R;
import com.podcasses.adapter.LanguageAdapter;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentSettingsBinding;
import com.podcasses.manager.SharedPreferencesManager;
import com.podcasses.model.response.Language;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.SettingsViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class SettingsFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private static Locale locale;

    static SettingsFragment newInstance(int instance, Locale locale) {
        SettingsFragment.locale = locale;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentSettingsBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        SettingsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel.class);
        binder.setLifecycleOwner(this);
        binder.setViewModel(viewModel);
        viewModel.setLocale(locale.getDisplayLanguage() + " " + locale.getCountry());
        LiveData<List<Language>> languages = viewModel.getLocales();
        languages.observe(this, l -> {
            if (l != null) {
                languages.removeObservers(this);
                viewModel.setLanguages(l);
            }
        });
        binder.languageSelect.setOnClickListener(l -> {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setAdapter(new LanguageAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, viewModel.getLanguages(),
                    getString(R.string.language)), (d, pos) -> {
                SharedPreferencesManager sharedPreferencesManager =
                        ((BaseApplication) getActivity().getApplication()).getSharedPreferencesManager();
                sharedPreferencesManager.setLocale(viewModel.getLanguages().get(pos).getIso639_1());

                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            });
            dialog = builder.create();
            dialog.show();
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_settings);
        setHasOptionsMenu(true);

        return binder.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

}
