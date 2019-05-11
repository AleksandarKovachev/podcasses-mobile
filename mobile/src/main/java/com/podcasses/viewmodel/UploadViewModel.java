package com.podcasses.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.BR;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.Language;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

import retrofit2.Response;


/**
 * Created by aleksandar.kovachev.
 */
public class UploadViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private MutableLiveData<List<Nomenclature>> categories = new MutableLiveData<>();
    private MutableLiveData<List<Nomenclature>> privacies = new MutableLiveData<>();
    private MutableLiveData<List<Language>> languages = new MutableLiveData<>();
    private ObservableField<String> podcastImage = new ObservableField<>();

    private Podcast podcast = new Podcast();

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

    public LiveData<List<Language>> getLanguageNomenclatures() {
        return repository.getLanguages();
    }

    public Podcast getPodcast() {
        return podcast;
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

    public void savePodcast(Response<Podcast> response) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                repository.savePodcast(response.body());
            }
        };
        thread.start();
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
