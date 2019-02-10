package com.podcasses.viewmodel;

import com.podcasses.BR;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private MutableLiveData<List<String>> languages = new MutableLiveData<>();

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
    public List<String> getLanguages() {
        return languages.getValue();
    }

    public void setLanguages(List<String> languages) {
        this.languages.setValue(languages);
        notifyPropertyChanged(BR.languages);
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
