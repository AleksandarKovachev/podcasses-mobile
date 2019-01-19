package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentHomeBinding;
import com.podcasses.viewmodel.HomeViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class HomeFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private HomeViewModel viewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel.class);

        return binder.getRoot();
    }

}
