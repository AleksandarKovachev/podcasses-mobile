package com.podcasses.model.repository;

import com.podcasses.database.dao.PodcastDao;
import com.podcasses.model.entity.Podcast;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;

/**
 * Created by aleksandar.kovachev.
 */
@Singleton
public class LocalDataSource {

    private final PodcastDao podcastDao;

    @Inject
    public LocalDataSource(PodcastDao podcastDao) {
        this.podcastDao = podcastDao;
    }

    public LiveData<List<Podcast>> getUserPodcasts(String userId) {
        return podcastDao.getUserPodcasts(userId);
    }

    public void insertPodcasts(Podcast... podcasts) {
        podcastDao.insertAll(podcasts);
    }

    public void delete(Podcast podcast) {
        podcastDao.remove(podcast);
    }

}
