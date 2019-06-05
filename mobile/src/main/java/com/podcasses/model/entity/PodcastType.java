package com.podcasses.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.podcasses.database.DateConverter;

import java.util.Date;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
public class PodcastType {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ForeignKey(entity = Podcast.class, parentColumns = "id", childColumns = "id")
    private String podcastId;

    private Integer podcastType;

    @TypeConverters({DateConverter.class})
    private Date createdTimestamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public Integer getPodcastType() {
        return podcastType;
    }

    public void setPodcastType(Integer podcastType) {
        this.podcastType = podcastType;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
