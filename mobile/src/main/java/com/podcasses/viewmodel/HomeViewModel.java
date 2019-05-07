package com.podcasses.viewmodel;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

public class HomeViewModel extends BasePodcastViewModel {

    private MutableLiveData<TrendingFilter> trendingFilterMutableLiveData = new MutableLiveData<>();

    HomeViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<ApiResponse> trendingPodcasts(TrendingFilter filter) {
        return repository.getTrendingPodcasts(filter);
    }

    public MutableLiveData<TrendingFilter> getTrendingFilterMutableLiveData() {
        return trendingFilterMutableLiveData;
    }

    public void onFilterButtonClick(View view) {
        DialogUtil.createTrendingFilterDialog(view.getContext(), trendingFilterMutableLiveData);
    }

}
