package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.PodcastFile;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastFileDao {

    @Query("SELECT * FROM PodcastFile")
    LiveData<List<PodcastFile>> getPodcastFiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(PodcastFile... podcastFiles);

    @Query("DELETE FROM PodcastFile WHERE id = (:id)")
    void deletePodcastFile(String id);

    @Query("DELETE FROM PodcastFile")
    void deletePodcastFiles();
}
