package com.podcasses.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;

import com.podcasses.BR;
import com.podcasses.constant.PodcastType;
import com.podcasses.model.response.Nomenclature;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.Language;
import com.podcasses.repository.MainDataRepository;
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

    public LiveData<List<Nomenclature>> getCategories() {
        return repository.getCategories();
    }

    public LiveData<List<Language>> getLanguages() {
        return repository.getLanguages();
    }

    public LiveData<List<Nomenclature>> getPrivacies() {
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
        repository.insertPodcast(PodcastType.MY_PODCASTS, response.body());
    }

}
