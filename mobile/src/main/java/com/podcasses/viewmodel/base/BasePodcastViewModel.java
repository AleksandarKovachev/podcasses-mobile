package com.podcasses.viewmodel.base;

import android.view.View;

import com.google.android.gms.common.util.CollectionUtils;
import com.ohoussein.playpause.PlayPauseView;
import com.podcasses.BR;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.MutableLiveData;

import static com.podcasses.util.CustomViewBindings.PODCAST_IMAGE;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BasePodcastViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private ObservableInt playingIndex = new ObservableInt(-1);
    private MutableLiveData<List<Podcast>> podcasts = new MutableLiveData<>();
    private MutableLiveData<Podcast> selectedPodcast = new MutableLiveData<>();
    private ObservableField<String> selectedAccount = new ObservableField<>();
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, this);
    private ObservableField<View> selectedViewOptions = new ObservableField<>();
    private String selectedPodcastOptions;

    public BasePodcastViewModel(MainDataRepository repository) {
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

    public PodcastAdapter getPodcastAdapter() {
        return podcastAdapter;
    }

    public void setPodcastsInAdapter(List<Podcast> podcasts) {
        this.podcasts.setValue(podcasts);
        this.podcastAdapter.setPodcasts(podcasts);
        this.podcastAdapter.notifyDataSetChanged();
    }

    public MutableLiveData<Podcast> getSelectedPodcast() {
        return selectedPodcast;
    }

    public void onPodcastClick(Integer index) {
        Podcast podcast = podcasts.getValue().get(index);
        selectedPodcast.setValue(podcast);
    }

    public ObservableField<String> getSelectedAccount() {
        return selectedAccount;
    }

    public void onAccountClick(Integer index) {
        selectedAccount.set(podcasts.getValue().get(index).getUserId());
    }

    public void onPlayButtonClick(Integer index) {
        EventBus.getDefault().post(podcasts.getValue().get(index));
    }

    public List<Podcast> getPodcasts() {
        return podcasts.getValue();
    }

    public Podcast getPodcastAt(Integer index) {
        if (podcasts.getValue() != null && index != null && podcasts.getValue().size() > index) {
            return podcasts.getValue().get(index);
        }
        return null;
    }

    public String podcastImage(Integer position) {
        if (!CollectionUtils.isEmpty(podcasts.getValue())) {
            Podcast podcast = podcasts.getValue().get(position);
            return BuildConfig.API_GATEWAY_URL.concat(PODCAST_IMAGE).concat(podcast.getId());
        }
        return null;
    }

    @Bindable
    public Integer getPlayingIndex() {
        return playingIndex.get();
    }

    public void setPlayingIndex(Integer playingIndex) {
        this.playingIndex.set(playingIndex);
        notifyPropertyChanged(BR.playingIndex);
    }

    @BindingAdapter(value = {"playPauseStatus", "position"}, requireAll = false)
    public static void playPauseStatus(PlayPauseView view, Integer position, Integer playingIndex) {
        if (position == -1 || playingIndex == -1) {
            view.change(true);
        } else {
            view.change(!position.equals(playingIndex));
        }
    }

    public String getSelectedPodcastOptions() {
        return selectedPodcastOptions;
    }

    public ObservableField<View> getSelectedViewOptions() {
        return selectedViewOptions;
    }

    public void onOptionsButtonClick(View view, Integer position) {
        selectedPodcastOptions = podcasts.getValue().get(position).getId();
        selectedViewOptions.set(view);
    }

    protected void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}
