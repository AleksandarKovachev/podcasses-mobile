package com.podcasses.database;

import com.podcasses.database.dao.PodcastDao;
import com.podcasses.model.entity.Podcast;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Created by aleksandar.kovachev.
 */
@TypeConverters(DateConverter.class)
@Database(entities = {Podcast.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "podcasses";

    public abstract PodcastDao podcastDao();

}
