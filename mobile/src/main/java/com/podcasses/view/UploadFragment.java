package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentUploadBinding;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.UploadViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private UploadViewModel viewModel;

    public static UploadFragment newInstance() {
        return new UploadFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentUploadBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UploadViewModel.class);

        return binder.getRoot();
    }

}
