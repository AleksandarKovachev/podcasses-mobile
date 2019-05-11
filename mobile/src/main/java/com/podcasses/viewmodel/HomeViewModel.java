package com.podcasses.viewmodel;

import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
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
    private MutableLiveData<List<Nomenclature>> categories = new MutableLiveData<>();
    private MutableLiveData<List<Language>> languages = new MutableLiveData<>();
    private Integer categoryId = null, languageId = null;

    HomeViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository, apiCallInterface);
    }

    public LiveData<List<Nomenclature>> getCategoryNomenclatures() {
        return repository.getCategories();
    }

    public LiveData<List<Language>> getLanguageNomenclatures() {
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

    @Bindable
    public List<Nomenclature> getCategories() {
        return categories.getValue();
    }

    public void setCategories(List<Nomenclature> categories) {
        this.categories.setValue(categories);
        notifyPropertyChanged(BR.categories);
    }

    @Bindable
    public List<Language> getLanguages() {
        return languages.getValue();
    }

    public void setLanguages(List<Language> languages) {
        this.languages.setValue(languages);
        notifyPropertyChanged(BR.languages);
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
