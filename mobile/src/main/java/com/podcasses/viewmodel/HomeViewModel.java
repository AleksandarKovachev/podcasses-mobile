package com.podcasses.viewmodel;

import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.request.TrendingFilter;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.model.response.Language;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.DialogUtil;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

public class HomeViewModel extends BasePodcastViewModel {

    private MutableLiveData<TrendingFilter> trendingFilterMutableLiveData = new MutableLiveData<>();
    private Integer categoryId = null, languageId = null;

    HomeViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public MutableLiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public MutableLiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    public LiveData<ApiResponse> trendingPodcasts(TrendingFilter filter) {
        return repository.getTrendingPodcasts(filter);
    }

    public MutableLiveData<TrendingFilter> getTrendingFilterMutableLiveData() {
        return trendingFilterMutableLiveData;
    }

    public void onFilterButtonClick(View view) {
        DialogUtil.createTrendingFilterDialog(view.getContext(), trendingFilterMutableLiveData, this);
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
}
