package com.podcasses.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentSearchBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.SearchViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class SearchFragment extends BaseFragment implements OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    private AdLoader adLoader;

    private SearchViewModel viewModel;
    private LiveData<ApiResponse> podcastsResponse;
    private LiveData<ApiResponse> accountsResponse;

    private static String searchQuery;

    static SearchFragment newInstance(int instance, String text) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        searchQuery = text;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentSearchBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        binding.setLifecycleOwner(this);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        updateActionBar();
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel.class);
        binding.setViewModel(viewModel);
        binding.refreshLayout.setOnRefreshListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getData(null);
        setListClick();
        setAccountClick();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getData(refreshLayout);
    }

    private void getData(RefreshLayout refreshLayout) {
        accountsResponse = viewModel.getAccounts(searchQuery);
        podcastsResponse = viewModel.podcasts(this, searchQuery, null, null, true, false, 0);
        podcastsResponse.observe(this, response -> consumeResponse(response, podcastsResponse, refreshLayout));
        accountsResponse.observe(this, response -> consumeResponse(response, accountsResponse, refreshLayout));
    }

    void updateActionBar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(searchQuery);
        }
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                if (refreshLayout != null) {
                    refreshLayout.autoRefreshAnimationOnly();
                }
                break;
            case DATABASE:
                viewModel.setPodcastsInAdapter((List<Object>) apiResponse.data);
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                if (CollectionUtils.isEmpty((Collection<?>) apiResponse.data)) {
                    return;
                }

                if (((List) apiResponse.data).get(0) instanceof Podcast) {
                    viewModel.clearPodcastsInAdapter();
                    viewModel.setPodcastsInAdapter((List<Object>) apiResponse.data);

                    if (((List) apiResponse.data).size() > 5) {
                        addAds();
                    }
                } else {
                    viewModel.setAccountsInAdapter((List<Account>) apiResponse.data);
                }
                break;
            case FETCHED:
                liveData.removeObservers(this);
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            default:
                break;
        }
    }

    private void setListClick() {
        viewModel.getSelectedPodcast().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setAccountClick() {
        viewModel.getSelectedAccount().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedAccount().get() != null) {
                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAccount().get()));
                    viewModel.getSelectedAccount().set(null);
                }
            }
        });
    }

    private void addAds() {
        adLoader = new AdLoader.Builder(getContext(), BuildConfig.SEARCH_PODCAST_NATIVE_ADS)
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    Log.i(getTag(), "Native Ad In Search Podcasts Loaded");
                    if (!adLoader.isLoading()) {
                        viewModel.addElementInPodcastsAdapter(unifiedNativeAd, viewModel.getPodcastAdapter().getItemCount() / 2);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.e(getTag(), "Native Ad In Search Podcasts Failed to loaded: " + errorCode);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .build())
                .build();
        adLoader.loadAds(new AdRequest.Builder().build(), 1);
    }

}
