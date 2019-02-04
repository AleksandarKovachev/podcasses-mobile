package com.podcasses.model.repository;

import com.podcasses.database.dao.PodcastDao;
import com.podcasses.model.entity.Podcast;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class LocalDataSource {

    private PodcastDao podcastDao;

    @Inject
    public LocalDataSource(PodcastDao podcastDao) {
        this.podcastDao = podcastDao;
    }

    LiveData<List<Podcast>> getUserPodcasts(String userId) {
        return podcastDao.getUserPodcasts(userId);
    }

    LiveData<Podcast> getPodcastById(String podcastId) {
        return podcastDao.getPodcastById(podcastId);
    }

    void insertPodcasts(Podcast... podcasts) {
        podcastDao.insertAll(podcasts);
    }

    void delete(Podcast podcast) {
        podcastDao.remove(podcast);
    }

}
