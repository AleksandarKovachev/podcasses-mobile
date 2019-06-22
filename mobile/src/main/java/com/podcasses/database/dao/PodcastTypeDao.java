package com.podcasses.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.PodcastType;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastTypeDao {

    @Query("SELECT * FROM PodcastType WHERE podcastId = (:podcastId) AND podcastType = (:podcastType)")
    PodcastType getPodcastType(String podcastId, Integer podcastType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(PodcastType... podcastTypes);

    @Query("DELETE FROM PodcastType WHERE podcastType = (:type)")
    void deletePodcastType(Integer type);

    @Query("DELETE FROM PodcastType WHERE podcastType = (:type) AND podcastId = (:podcastId)")
    void deletePodcastType(Integer type, String podcastId);

    @Query("DELETE FROM PodcastType")
    void deleteAll();

}
