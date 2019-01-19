package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentHomeBinding;
import com.podcasses.databinding.FragmentSearchBinding;
import com.podcasses.viewmodel.AccountViewModel;
import com.podcasses.viewmodel.HomeViewModel;
import com.podcasses.viewmodel.SearchViewModel;
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
public class SearchFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private SearchViewModel viewModel;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentSearchBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel.class);

        return binder.getRoot();
    }

}
