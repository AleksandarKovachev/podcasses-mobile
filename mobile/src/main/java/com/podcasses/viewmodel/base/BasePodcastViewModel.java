package com.podcasses.viewmodel.base;

import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.adapter.PodcastAdapter;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;

import java.util.List;

import androidx.lifecycle.MutableLiveData;

import static com.podcasses.util.CustomViewBindings.PODCAST_IMAGE;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BasePodcastViewModel extends BaseViewModel {

    private MutableLiveData<List<Podcast>> podcasts = new MutableLiveData<>();
    private MutableLiveData<Podcast> selected = new MutableLiveData<>();
    private PodcastAdapter podcastAdapter = new PodcastAdapter(R.layout.item_podcast, this);

    public BasePodcastViewModel(MainDataRepository repository) {
        super(repository);
    }

    public PodcastAdapter getPodcastAdapter() {
        return podcastAdapter;
    }

    public void setPodcastsInAdapter(List<Podcast> podcasts) {
        this.podcasts.setValue(podcasts);
        this.podcastAdapter.setPodcasts(podcasts);
        this.podcastAdapter.notifyDataSetChanged();
    }

    public MutableLiveData<Podcast> getSelected() {
        return selected;
    }

    public void onItemClick(Integer index) {
        Podcast podcast = podcasts.getValue().get(index);
        selected.setValue(podcast);
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

}
