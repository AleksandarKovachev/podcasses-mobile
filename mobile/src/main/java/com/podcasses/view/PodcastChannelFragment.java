package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastChannelBinding;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.PodcastChannelViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private FragmentPodcastChannelBinding binding;
    private PodcastChannelViewModel viewModel;

    private static String podcastChannelId;
    private static PodcastChannel podcastChannel;

    private LiveData<ApiResponse> podcastChannelResponse;

    static PodcastChannelFragment newInstance(int instance, String podcastChannelId, PodcastChannel podcastChannel) {
        PodcastChannelFragment.podcastChannelId = podcastChannelId;
        PodcastChannelFragment.podcastChannel = podcastChannel;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        PodcastChannelFragment fragment = new PodcastChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast_channel, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastChannelViewModel.class);
        binding.setViewModel(viewModel);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    void updateActionBar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (podcastChannel != null) {
            viewModel.setPodcastChannel(podcastChannel);
        } else {
            podcastChannelResponse = viewModel.podcastChannel(podcastChannelId);
            podcastChannelResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastChannelResponse));
        }
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData) {
        switch (apiResponse.status) {
            case LOADING:
            case DATABASE:
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                viewModel.setPodcastChannel((PodcastChannel) apiResponse.data);
                break;
            case ERROR:
                liveData.removeObservers(this);
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
        }
    }

//    private void setPodcastClick() {
//        viewModel.getSelectedPodcast().observe(this, podcast -> {
//            if (podcast != null) {
//                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
//                viewModel.getSelectedPodcast().setValue(null);
//            }
//        });
//    }
//
//    private void setAccountClick() {
//        viewModel.getSelectedAccount().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
//            @Override
//            public void onPropertyChanged(Observable sender, int propertyId) {
//                if (viewModel.getSelectedAccount().get() != null) {
//                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAccount().get()));
//                    viewModel.getSelectedAccount().set(null);
//                }
//            }
//        });
//    }
//
//    private void getAccountPodcasts(List<Podcast> podcasts, boolean isSwipedToRefresh) {
//        List<String> podcastIds = new ArrayList<>();
//        for (Podcast podcast : podcasts) {
//            podcastIds.add(podcast.getId());
//        }
//
//        LiveData<ApiResponse> accountPodcasts = viewModel.accountPodcasts(this, token.getValue(), podcastIds, isSwipedToRefresh);
//        accountPodcasts.observe(this, response -> consumeAccountPodcasts(response, accountPodcasts));
//    }
//
//    private void consumeAccountPodcasts(ApiResponse accountPodcastsResponse, LiveData<ApiResponse> liveData) {
//        switch (accountPodcastsResponse.status) {
//            case LOADING:
//                break;
//            case DATABASE:
//                setAccountPodcastsData(accountPodcastsResponse);
//                break;
//            case SUCCESS: {
//                liveData.removeObservers(this);
//                setAccountPodcastsData(accountPodcastsResponse);
//                break;
//            }
//            case ERROR: {
//                liveData.removeObservers(this);
//                LogErrorResponseUtil.logErrorApiResponse(accountPodcastsResponse, getContext());
//                break;
//            }
//        }
//    }
//
//    private void setAccountPodcastsData(ApiResponse accountPodcastsResponse) {
//        if (!CollectionUtils.isEmpty((List<AccountPodcast>) accountPodcastsResponse.data)) {
//            for (Object podcast : viewModel.getPodcasts()) {
//                for (AccountPodcast accountPodcast : (List<AccountPodcast>) accountPodcastsResponse.data) {
//                    if (accountPodcast.getPodcastId().equals(((Podcast) podcast).getId())) {
//                        ((Podcast) podcast).setMarkAsPlayed(accountPodcast.getMarkAsPlayed() == 1);
//                    }
//                }
//            }
//        }
//    }
//
//    private void setInfiniteScrollListener() {
//        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//            if (scrollY >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
//                if (!viewModel.getIsLoading()) {
//                    ++page;
//                    viewModel.setIsLoading(true);
//                    loadPodcasts(isMyAccount, null);
//                }
//            }
//        });
//    }
//
//    private void loadPodcasts(boolean isMyAccount, RefreshLayout refreshLayout) {
//        podcastsResponse = viewModel.podcasts(this, null, null, accountId, refreshLayout != null, isMyAccount, page);
//        podcastsResponse.observe(this, apiResponse -> consumeResponse(apiResponse, podcastsResponse, refreshLayout));
//    }

}
