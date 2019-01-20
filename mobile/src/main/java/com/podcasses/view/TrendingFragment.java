package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentSearchBinding;
import com.podcasses.databinding.FragmentTrendingBinding;
import com.podcasses.viewmodel.SearchViewModel;
import com.podcasses.viewmodel.TrendingViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class TrendingFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private TrendingViewModel viewModel;

    public static TrendingFragment newInstance() {
        return new TrendingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentTrendingBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_trending, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TrendingViewModel.class);

        return binder.getRoot();
    }

}
