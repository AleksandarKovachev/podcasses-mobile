package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.Podcast;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastDao {

    @Query("SELECT podcast.* FROM Podcast AS podcast JOIN PodcastType AS podcastType ON podcast.id = podcastType.podcastId WHERE podcastType.podcastType = (:type) ORDER BY podcastType.createdTimestamp DESC LIMIT (:page), 10")
    LiveData<List<Podcast>> getPodcasts(Integer type, int page);

    @Query("SELECT podcast.* FROM Podcast AS podcast JOIN AccountPodcast AS accountPodcast ON podcast.id = accountPodcast.podcastId WHERE accountPodcast.timeIndex > 0 AND accountPodcast.markAsPlayed = 0 ORDER BY accountPodcast.viewTimestamp DESC LIMIT (:page), 10")
    LiveData<List<Podcast>> getPodcastsInProgress(int page);

    @Query("SELECT * FROM Podcast WHERE id = (:podcastId)")
    LiveData<Podcast> getPodcastById(String podcastId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Podcast... podcasts);

    @Query("DELETE FROM Podcast WHERE id = (:id)")
    void deletePodcast(String id);

    @Query("DELETE FROM Podcast WHERE id NOT IN (SELECT podcastId FROM PODCASTTYPE)")
    void deletePodcasts();

}
