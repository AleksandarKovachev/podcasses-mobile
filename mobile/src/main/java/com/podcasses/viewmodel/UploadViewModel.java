package com.podcasses.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
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
public class UploadViewModel extends BaseViewModel {

    private ObservableField<String> podcastImage = new ObservableField<>();

    private Podcast podcast = new Podcast();

    UploadViewModel(MainDataRepository repository) {
        super(repository);
    }

    public MutableLiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public MutableLiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    public MutableLiveData<List<Nomenclature>> getPrivacies() {
        return repository.getPrivacies();
    }

    public Podcast getPodcast() {
        return podcast;
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

}
