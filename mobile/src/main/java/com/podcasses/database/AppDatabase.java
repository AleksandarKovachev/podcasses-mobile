package com.podcasses.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.podcasses.database.dao.AccountPodcastDao;
import com.podcasses.database.dao.PodcastChannelDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.database.dao.PodcastFileDao;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.DownloadedPodcast;
import com.podcasses.model.entity.HistoryPodcast;
import com.podcasses.model.entity.LikedPodcast;
import com.podcasses.model.entity.NewPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.entity.ProgressPodcast;
import com.podcasses.model.entity.TrendingPodcast;
import com.podcasses.model.entity.base.Podcast;

/**
 * Created by aleksandar.kovachev.
 */
@TypeConverters(DateConverter.class)
@Database(entities = {
        Podcast.class,
        DownloadedPodcast.class,
        HistoryPodcast.class,
        LikedPodcast.class,
        NewPodcast.class,
        ProgressPodcast.class,
        TrendingPodcast.class,
        AccountPodcast.class,
        PodcastFile.class,
        PodcastChannel.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "podcasses";

    public abstract PodcastDao podcastDao();

    public abstract AccountPodcastDao accountPodcastDao();

    public abstract PodcastFileDao podcastFileDao();

    public abstract PodcastChannelDao podcastChannelDao();

}
