package com.podcasses.model.entity;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
public class AccountPodcast {

    public static final int DUSLIKED = 2;
    public static final int LIKED = 1;
    public static final int DEFAULT = 0;

    @NonNull
    @PrimaryKey
    private Long id;

    private String accountId;

    private String podcastId;

    private int likeStatus;

    private long timeIndex;

    private int markAsPlayed;

    private Date createdTimestamp;

    private Date updatedTimestamp;

    private int version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public int getLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(int likeStatus) {
        this.likeStatus = likeStatus;
    }

    public long getTimeIndex() {
        return timeIndex;
    }

    public void setTimeIndex(long timeIndex) {
        this.timeIndex = timeIndex;
    }

    public int getMarkAsPlayed() {
        return markAsPlayed;
    }

    public void setMarkAsPlayed(int markAsPlayed) {
        this.markAsPlayed = markAsPlayed;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Date getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(Date updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
