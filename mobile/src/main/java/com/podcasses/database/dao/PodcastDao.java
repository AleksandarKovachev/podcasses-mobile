package com.podcasses.database.dao;

import com.podcasses.model.entity.Podcast;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastDao {

    @Query("SELECT * FROM podcast WHERE userId = (:userId)")
    LiveData<List<Podcast>> getUserPodcasts(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Podcast... podcasts);

    @Delete
    void remove(Podcast Podcast);

}
