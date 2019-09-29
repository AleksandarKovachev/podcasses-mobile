package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.Strings;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.DialogPodcastListBinding;
import com.podcasses.model.dto.PodcastListCheckbox;
import com.podcasses.model.response.AccountList;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.viewmodel.PodcastListDialogViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastListDialogFragment extends DialogFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private PodcastListDialogViewModel viewModel;
    private DialogPodcastListBinding binding;

    private static String podcastId;

    public static PodcastListDialogFragment newInstance(String podcastId) {
        PodcastListDialogFragment.podcastId = podcastId;
        PodcastListDialogFragment fragment = new PodcastListDialogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_podcast_list, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastListDialogViewModel.class);
        binding.setViewModel(viewModel);
        binding.setPodcastId(podcastId);
        binding.setDialogFragment(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LiveData<String> token = AuthenticationUtil.getAuthenticationToken(getContext());
        if (token != null) {
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    binding.setToken(s);
                    LiveData<ApiResponse> accountListsResponse = viewModel.accountLists(s, null);
                    accountListsResponse.observe(this, accountLists -> {
                        if (accountLists.status == ApiResponse.Status.SUCCESS) {
                            accountListsResponse.removeObservers(this);
                            List<PodcastListCheckbox> podcastListCheckboxes = new ArrayList<>();
                            for (AccountList accountList : (List<AccountList>) accountLists.data) {
                                PodcastListCheckbox podcastListCheckbox = new PodcastListCheckbox();
                                podcastListCheckbox.setName(accountList.getName());
                                podcastListCheckbox.setId(accountList.getId());
                                podcastListCheckboxes.add(podcastListCheckbox);
                            }
                            viewModel.setPodcastListCheckboxes(podcastListCheckboxes);

                            LiveData<ApiResponse> podcastListsResponse = viewModel.accountLists(s, podcastId);
                            podcastListsResponse.observe(this, podcastLists -> {
                                if (podcastLists.status == ApiResponse.Status.SUCCESS) {
                                    podcastListsResponse.removeObservers(this);
                                    viewModel.setCheckedAccountLists((List<AccountList>) podcastLists.data);

                                    for (AccountList accountList : (List<AccountList>) podcastLists.data) {
                                        for (PodcastListCheckbox podcastListCheckbox : viewModel.getPodcastListAdapter().getPodcastListCheckboxes()) {
                                            if (accountList.getId().equals(podcastListCheckbox.getId())) {
                                                podcastListCheckbox.setChecked(true);
                                            }
                                        }
                                    }
                                } else if (podcastLists.status == ApiResponse.Status.ERROR) {
                                    podcastListsResponse.removeObservers(this);
                                }
                            });
                        } else if (accountLists.status == ApiResponse.Status.ERROR) {
                            accountListsResponse.removeObservers(this);
                        }
                    });
                }
            });
        }
    }

}
