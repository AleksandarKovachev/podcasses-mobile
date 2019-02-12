package com.podcasses.viewmodel;

import android.widget.Spinner;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.NomenclatureAdapter;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private MutableLiveData<List<Nomenclature>> categories = new MutableLiveData<>();
    private MutableLiveData<List<Nomenclature>> privacies = new MutableLiveData<>();
    private MutableLiveData<List<String>> languages = new MutableLiveData<>();
    private ObservableField<String> podcastImage = new ObservableField<>();
    private ObservableField<Integer> podcastUploadProgress = new ObservableField<>();

    UploadViewModel(MainDataRepository repository) {
        super(repository);
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    public LiveData<List<Nomenclature>> getCategoryNomenclatures() {
        return repository.getCategories();
    }

    public LiveData<List<Nomenclature>> getPrivacyNomenclatures() {
        return repository.getPrivacies();
    }

    public LiveData<List<Nomenclature>> getLanguageNomenclatures() {
        return repository.getLanguages();
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
    public List<String> getLanguages() {
        return languages.getValue();
    }

    public void setLanguages(List<String> languages) {
        this.languages.setValue(languages);
        notifyPropertyChanged(BR.languages);
    }

    @Bindable
    public List<Nomenclature> getPrivacies() {
        return privacies.getValue();
    }

    public void setPrivacies(List<Nomenclature> privacies) {
        this.privacies.setValue(privacies);
        notifyPropertyChanged(BR.privacies);
    }

    @Bindable
    public String getPodcastImage() {
        return podcastImage.get();
    }

    public void setPodcastImage(String podcastImage) {
        this.podcastImage.set(podcastImage);
        notifyPropertyChanged(BR.podcastImage);
    }

    @Bindable
    public Integer getPodcastUploadProgress() {
        return podcastUploadProgress.get();
    }

    public void setPodcastUploadProgress(Double progress) {
        this.podcastUploadProgress.set(progress.intValue());
        notifyPropertyChanged(BR.podcastUploadProgress);
    }

    @BindingAdapter(value = {"progress_current"}, requireAll = false)
    public static void setCurrentProgress(NumberProgressBar progressBar, Integer progress) {
        if (progress != null) {
            progressBar.setProgress(progress);
        }
    }

    @BindingAdapter(value = {"privacies", "selectedPrivacyAttrChanged"}, requireAll = false)
    public static void setPrivacies(Spinner spinner, List<Nomenclature> privacies, InverseBindingListener listener) {
        if (privacies == null) {
            return;
        }
        spinner.setAdapter(new NomenclatureAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item, privacies, spinner.getContext().getString(R.string.podcast_category)));
    }

    @BindingAdapter(value = {"categories", "selectedCategoryAttrChanged"}, requireAll = false)
    public static void setCategoriesAdapter(Spinner spinner, List<Nomenclature> categories, InverseBindingListener listener) {
        if (categories == null) {
            return;
        }
        spinner.setAdapter(new NomenclatureAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item, categories, spinner.getContext().getString(R.string.podcast_category)));
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
