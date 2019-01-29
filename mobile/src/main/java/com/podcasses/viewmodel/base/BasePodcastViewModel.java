package com.podcasses.viewmodel.base;

import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BasePodcastViewModel extends BaseViewModel {

    public BasePodcastViewModel(MainDataRepository repository) {
        super(repository);
    }

    public abstract void onItemClick(Integer index);

    public abstract String podcastImage(Integer index);

    public abstract Podcast getPodcastAt(Integer index);

}
