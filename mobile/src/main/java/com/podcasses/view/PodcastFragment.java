package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.PodcastViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private PodcastViewModel viewModel;

    private static String id;

    private LiveData<ApiResponse> response;

    static PodcastFragment newInstance(int instance, String podcastId) {
        id = podcastId;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        PodcastFragment fragment = new PodcastFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPodcastBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast, container, false);
        binder.setLifecycleOwner(this);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        binder.refreshLayout.setOnRefreshListener(refreshListener);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel.class);
        binder.setViewModel(viewModel);

        viewModel.setPodcastImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PODCAST_IMAGE + id);

        response = viewModel.podcast(this, id, false);
        response.observe(this, apiResponse -> consumeResponse(apiResponse, null));

        return binder.getRoot();
    }

    private OnRefreshListener refreshListener = refreshLayout -> {
        response = viewModel.podcast(this, id, true);
        response.observe(this, apiResponse -> consumeResponse(apiResponse, refreshLayout));
    };

    private void consumeResponse(@NonNull ApiResponse apiResponse, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                response.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                if (apiResponse.data instanceof Podcast) {
                    viewModel.setPodcast((Podcast) apiResponse.data);
                } else {
                    viewModel.setPodcast(((List<Podcast>) apiResponse.data).get(0));
                }
                break;
            case ERROR:
                response.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                logError(apiResponse);
                break;
            default:
                break;
        }
    }

}
