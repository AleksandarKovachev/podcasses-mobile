package com.podcasses.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.podcasses.database.dao.AccountDao;
import com.podcasses.database.dao.AccountPodcastDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.database.dao.PodcastFileDao;
import com.podcasses.database.dao.PodcastTypeDao;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.entity.PodcastType;

/**
 * Created by aleksandar.kovachev.
 */
@TypeConverters(DateConverter.class)
@Database(entities = {Podcast.class, Account.class, AccountPodcast.class, PodcastFile.class, PodcastType.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "podcasses";

    public abstract PodcastDao podcastDao();

    public abstract AccountDao accountDao();

    public abstract AccountPodcastDao accountPodcastDao();

    public abstract PodcastFileDao podcastFileDao();

    public abstract PodcastTypeDao podcastTypeDao();

}
