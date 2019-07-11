package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.AccountPodcast;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface AccountPodcastDao {

    @Query("SELECT * FROM AccountPodcast WHERE podcastId = (:podcastId)")
    LiveData<AccountPodcast> getAccountPodcast(String podcastId);

    @Query("SELECT * FROM AccountPodcast WHERE podcastId IN (:podcastIds)")
    LiveData<List<AccountPodcast>> getAccountPodcasts(List<String> podcastIds);

    @Query("SELECT * FROM AccountPodcast WHERE accountId IS NULL")
    List<AccountPodcast> getNotSyncedAccountPodcasts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(AccountPodcast... accountPodcast);

    @Query("DELETE FROM AccountPodcast")
    void deleteAll();

}
