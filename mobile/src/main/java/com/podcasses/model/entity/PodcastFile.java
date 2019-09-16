package com.podcasses.model.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.podcasses.database.DateConverter;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
public class PodcastFile {

    @NonNull
    @PrimaryKey
    private String id;

    private String userId;

    private String fileName;

    @TypeConverters({DateConverter.class})
    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPodcastFileDate() {
        DateFormat formatter = SimpleDateFormat.getDateInstance();
        return formatter.format(getDate());
    }

}
