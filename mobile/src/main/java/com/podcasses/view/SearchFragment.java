package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentSearchBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.SearchViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class SearchFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private SearchViewModel viewModel;
    private LiveData<ApiResponse> podcastsResponse;

    private static String podcast;

    static SearchFragment newInstance(int instance, String text) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        podcast = text;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentSearchBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        updateTitle();

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel.class);
        binder.setViewModel(viewModel);

        binder.refreshLayout.setOnRefreshListener(this::getPodcasts);
        getPodcasts(binder.refreshLayout);

        setListClick();

        return binder.getRoot();
    }

    private void getPodcasts(RefreshLayout refreshLayout) {
        podcastsResponse = viewModel.podcasts(this, podcast, true, false);
        podcastsResponse.observe(this, response -> consumeResponse(response, podcastsResponse, refreshLayout));
    }

    void updateTitle() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(podcast);
        }
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                refreshLayout.autoRefreshAnimationOnly();
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                viewModel.setPodcastsInAdapter((List<Podcast>) apiResponse.data);
                break;
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                logError(apiResponse);
                break;
            default:
                break;
        }
    }

    private void setListClick() {
        viewModel.getSelected().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId()));
                viewModel.getSelected().setValue(null);
            }
        });
    }

}
