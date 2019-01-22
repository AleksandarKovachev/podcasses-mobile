package com.podcasses.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentNotificationsBinding;
import com.podcasses.viewmodel.NotificationsViewModel;
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
public class NotificationsFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private NotificationsViewModel viewModel;

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentNotificationsBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NotificationsViewModel.class);

        Intent intent = new Intent(getContext(), AuthenticatorActivity.class);
        getContext().startActivity(intent);

        return binder.getRoot();
    }

}
