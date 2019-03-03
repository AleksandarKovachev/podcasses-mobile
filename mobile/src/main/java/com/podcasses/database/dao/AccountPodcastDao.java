package com.podcasses.database.dao;

import com.podcasses.model.entity.AccountPodcast;

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
public interface AccountPodcastDao {

    @Query("SELECT * FROM AccountPodcast WHERE accountId = :accountId AND podcastId = :podcastId")
    LiveData<AccountPodcast> getAccountPodcast(String accountId, String podcastId);

    @Query("SELECT * FROM AccountPodcast WHERE accountId = :accountId")
    LiveData<List<AccountPodcast>> getAccountPodcasts(String accountId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(AccountPodcast... accountPodcast);

}
