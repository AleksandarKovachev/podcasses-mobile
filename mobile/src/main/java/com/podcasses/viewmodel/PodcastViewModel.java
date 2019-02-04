package com.podcasses.viewmodel;

import com.podcasses.BR;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.viewmodel.base.BaseViewModel;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    private MutableLiveData<Podcast> podcast = new MutableLiveData<>();
    private ObservableField<String> podcastImage = new ObservableField<>();

    PodcastViewModel(MainDataRepository repository) {
        super(repository);
    }

    public LiveData<ApiResponse> podcast(@NonNull LifecycleOwner lifecycleOwner, @NonNull String podcastId) {
        return repository.getPodcasts(lifecycleOwner, null, podcastId, null);
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    @Bindable
    public Podcast getPodcast() {
        return podcast.getValue();
    }

    @Bindable
    public String getPodcastImage() {
        return podcastImage.get();
    }

    public void setPodcast(Podcast podcast) {
        this.podcast.setValue(podcast);
        notifyPropertyChanged(BR.podcast);
    }

    public void setPodcastImage(String podcastImage) {
        this.podcastImage.set(podcastImage);
        notifyPropertyChanged(BR.podcastImage);
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
