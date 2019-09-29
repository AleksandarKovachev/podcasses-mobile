package com.podcasses.viewmodel;

import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Language;
import com.podcasses.model.response.Nomenclature;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

public class HomeViewModel extends BasePodcastViewModel {

    private MutableLiveData<TrendingFilter> trendingFilterMutableLiveData = new MutableLiveData<>();
    private Integer categoryId = -1, languageId = -1;
    private LifecycleOwner lifecycleOwner;

    HomeViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<ApiResponse> getSubscribedPodcastChannels(String token) {
        return repository.getSubscribedPodcastChannels(lifecycleOwner, token);
    }

    public LiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public LiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    public LiveData<ApiResponse> trendingPodcasts(LifecycleOwner lifecycleOwner, TrendingFilter filter, boolean isSwipedToRefresh) {
        return repository.getTrendingPodcasts(lifecycleOwner, filter, isSwipedToRefresh);
    }

    public MutableLiveData<TrendingFilter> getTrendingFilterMutableLiveData() {
        return trendingFilterMutableLiveData;
    }

    public void onFilterButtonClick(View view) {
        DialogUtil.createTrendingFilterDialog(view.getContext(), trendingFilterMutableLiveData, this, lifecycleOwner);
    }

    public void openDatePicker(View view) {
        DialogUtil.openDatePicker((AppCompatEditText) view);
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

}
