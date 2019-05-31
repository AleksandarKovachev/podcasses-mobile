package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentHistoryBinding;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.HistoryViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class HistoryFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    public static HistoryFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentHistoryBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
        binder.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        HistoryViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(HistoryViewModel.class);
        binder.setViewModel(viewModel);
        binder.setFragmentManager(getChildFragmentManager());
        return binder.getRoot();
    }

}
