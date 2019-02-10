package com.podcasses.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.podcasses.R;
import com.podcasses.adapter.NomenclatureAdapter;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentUploadBinding;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.UploadViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private UploadViewModel viewModel;

    private RTManager rtManager;

    private LifecycleOwner lifecycleOwner;

    private FragmentUploadBinding binder;

    static UploadFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        UploadFragment fragment = new UploadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        lifecycleOwner = this;

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UploadViewModel.class);

        binder.numberProgressBar.setProgress(20);

        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl((Activity) getContext()), new RTMediaFactoryImpl(getContext(), true));
        rtManager = new RTManager(rtApi, savedInstanceState);

        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_character));
        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_paragraph));
        rtManager.registerEditor(binder.podcastDescription, true);

        setPrivacies();
        setCategories();

        return binder.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        rtManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rtManager.onDestroy(true);
    }

    private void setCategories() {
        LiveData<List<Nomenclature>> categories = viewModel.getCategoryNomenclatures();
        categories.observe(lifecycleOwner, nomenclatures -> {
            categories.removeObservers(lifecycleOwner);
            binder.podcastCategory.setAdapter(
                    new NomenclatureAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, nomenclatures, getString(R.string.podcast_category)));
        });
    }

    private void setPrivacies() {
        LiveData<List<Nomenclature>> privacies = viewModel.getPrivacyNomenclatures();
        privacies.observe(lifecycleOwner, nomenclatures -> {
            privacies.removeObservers(lifecycleOwner);
            binder.podcastPrivacy.setAdapter(
                    new NomenclatureAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, nomenclatures, getString(R.string.podcast_privacy)));
        });
    }

}
