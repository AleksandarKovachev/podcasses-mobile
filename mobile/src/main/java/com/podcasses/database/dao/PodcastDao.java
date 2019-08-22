package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.DownloadedPodcast;
import com.podcasses.model.entity.HistoryPodcast;
import com.podcasses.model.entity.LikedPodcast;
import com.podcasses.model.entity.NewPodcast;
import com.podcasses.model.entity.ProgressPodcast;
import com.podcasses.model.entity.TrendingPodcast;
import com.podcasses.model.entity.UserPodcast;
import com.podcasses.model.entity.base.Podcast;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface PodcastDao {

    @Query("SELECT * " +
            "FROM UserPodcast " +
            "WHERE id = (:id)")
    LiveData<Podcast> getUserPodcastById(String id);

    @Query("SELECT * " +
            "FROM UserPodcast " +
            "WHERE userId = (:userId) " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getUserPodcastsByUserId(String userId, int page);

    @Query("SELECT * " +
            "FROM UserPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getUserPodcasts(int page);

    @Query("SELECT * " +
            "FROM HistoryPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getHistoryPodcasts(int page);

    @Query("SELECT * " +
            "FROM LikedPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getLikedPodcasts(int page);

    @Query("SELECT * " +
            "FROM NewPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getNewPodcasts(int page);

    @Query("SELECT * " +
            "FROM ProgressPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getProgressPodcasts(int page);

    @Query("SELECT * " +
            "FROM TrendingPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getTrendingPodcasts(int page);

    @Query("SELECT * " +
            "FROM DownloadedPodcast " +
            "ORDER BY internalId ASC " +
            "LIMIT (:page), 10")
    LiveData<List<Podcast>> getDownloadedPodcasts(int page);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistoryPodcasts(List<HistoryPodcast> podcasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserPodcasts(List<UserPodcast> podcasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLikedPodcasts(List<LikedPodcast> podcasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDownloadedPodcasts(List<DownloadedPodcast> podcasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNewPodcasts(List<NewPodcast> podcasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrendingPodcasts(List<TrendingPodcast> podcasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgressPodcasts(List<ProgressPodcast> podcasts);

    @Query("DELETE FROM UserPodcast")
    void deleteUserPodcasts();

    @Query("DELETE FROM HistoryPodcast")
    void deleteHistoryPodcasts();

    @Query("DELETE FROM LikedPodcast")
    void deleteLikedPodcasts();

    @Query("DELETE FROM NewPodcast")
    void deleteNewPodcasts();

    @Query("DELETE FROM ProgressPodcast")
    void deleteProgressPodcasts();

    @Query("DELETE FROM TrendingPodcast")
    void deleteTrendingPodcasts();

    @Query("DELETE FROM DownloadedPodcast")
    void deleteDownloadedPodcasts();

    @Query("DELETE FROM DownloadedPodcast WHERE id = (:id)")
    void deleteDownloadedPodcast(String id);

}
