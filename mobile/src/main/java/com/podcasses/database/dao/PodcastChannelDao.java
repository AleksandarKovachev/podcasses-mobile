package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.PodcastChannel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastChannelDao {

    @Query("SELECT * FROM PodcastChannel where userId = :userId")
    LiveData<List<PodcastChannel>> getPodcastChannelsByUserId(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PodcastChannel> podcastChannels);

    @Query("DELETE FROM PodcastChannel WHERE userId = :userId")
    void deletePodcastChannelsByUserId(String userId);

    @Query("DELETE FROM PodcastChannel WHERE id = :id")
    void deletePodcastChannelsById(String id);

    @Query("DELETE FROM PodcastChannel")
    void deletePodcastChannels();

}
