package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentHomeBinding;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.request.TrendingReport;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Language;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.HomeViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class HomeFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private HomeViewModel viewModel;

    private LiveData<ApiResponse> trendingPodcasts;

    static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel.class);
        binder.setViewModel(viewModel);

        TrendingFilter trendingFilter = new TrendingFilter(TrendingReport.WEEKLY, null, null, null, null);
        getData(null, trendingFilter);
        binder.refreshLayout.setOnRefreshListener(r -> getData(r, trendingFilter));

        setListClick();
        setAccountClick();
        setTrendingFilterChange();
        setLanguages();
        setCategories();

        return binder.getRoot();
    }

    private void getData(RefreshLayout refreshLayout, TrendingFilter trendingFilter) {
        getTrendingPodcasts(refreshLayout, trendingFilter);
    }

    private void getTrendingPodcasts(RefreshLayout refreshLayout, TrendingFilter trendingFilter) {
        trendingPodcasts = viewModel.trendingPodcasts(trendingFilter);
        trendingPodcasts.observe(this, apiResponse -> consumeResponse(apiResponse, trendingPodcasts, refreshLayout));
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                if (apiResponse.data instanceof List) {
                    viewModel.setTrendingPodcastsInAdapter((List<Podcast>) apiResponse.data);
                }
                break;
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
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

    private void setTrendingFilterChange() {
        viewModel.getTrendingFilterMutableLiveData().observe(this, trendingFilter ->
                getTrendingPodcasts(null, trendingFilter)
        );
    }

    private void setCategories() {
        LiveData<List<Nomenclature>> categories = viewModel.getCategoryNomenclatures();
        categories.observe(this, nomenclatures -> {
            categories.removeObservers(this);
            viewModel.setCategories(nomenclatures);
        });
    }

    private void setLanguages() {
        LiveData<List<Language>> languages = viewModel.getLanguageNomenclatures();
        languages.observe(this, language -> {
            languages.removeObservers(this);
            viewModel.setLanguages(language);
        });
    }

}
