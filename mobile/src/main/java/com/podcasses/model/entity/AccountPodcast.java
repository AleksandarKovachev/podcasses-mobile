package com.podcasses.model.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
public class AccountPodcast {

    private Long id;

    private String accountId;

    @NonNull
    @PrimaryKey
    private String podcastId;

    private int likeStatus;

    private long timeIndex;

    private int markAsPlayed;

    private Date markAsPlayedTimestamp;

    private Date likeTimestamp;

    private Date viewTimestamp;

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

    public Date getMarkAsPlayedTimestamp() {
        return markAsPlayedTimestamp;
    }

    public void setMarkAsPlayedTimestamp(Date markAsPlayedTimestamp) {
        this.markAsPlayedTimestamp = markAsPlayedTimestamp;
    }

    public Date getLikeTimestamp() {
        return likeTimestamp;
    }

    public void setLikeTimestamp(Date likeTimestamp) {
        this.likeTimestamp = likeTimestamp;
    }

    public Date getViewTimestamp() {
        return viewTimestamp;
    }

    public void setViewTimestamp(Date viewTimestamp) {
        this.viewTimestamp = viewTimestamp;
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
