package com.podcasses.database.dao;

import com.podcasses.model.entity.PodcastFile;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastFileDao {

    @Query("SELECT * FROM podcastFile WHERE userId = (:userId)")
    LiveData<List<PodcastFile>> getUserPodcastFiles(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(PodcastFile... podcastFiles);

    @Query("DELETE FROM podcastFile WHERE id = (:id)")
    void deletePodcastFile(String id);

    @Query("DELETE FROM podcastFile")
    void deletePodcastFiles();
}
