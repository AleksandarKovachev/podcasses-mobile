package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountPodcastRequest {

    private String podcastId;

    private Integer likeStatus;

    private Long timeIndex;

    private Integer markAsPlayed;

    private Long markAsPlayedTimestamp;

    private Long likeTimestamp;

    private Long viewTimestamp;

    private Long createdTimestamp;

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public Integer getLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(Integer likeStatus) {
        this.likeStatus = likeStatus;
    }

    public Long getTimeIndex() {
        return timeIndex;
    }

    public void setTimeIndex(Long timeIndex) {
        this.timeIndex = timeIndex;
    }

    public Integer getMarkAsPlayed() {
        return markAsPlayed;
    }

    public void setMarkAsPlayed(Integer markAsPlayed) {
        this.markAsPlayed = markAsPlayed;
    }

    public Long getMarkAsPlayedTimestamp() {
        return markAsPlayedTimestamp;
    }

    public void setMarkAsPlayedTimestamp(Long markAsPlayedTimestamp) {
        this.markAsPlayedTimestamp = markAsPlayedTimestamp;
    }

    public Long getLikeTimestamp() {
        return likeTimestamp;
    }

    public void setLikeTimestamp(Long likeTimestamp) {
        this.likeTimestamp = likeTimestamp;
    }

    public Long getViewTimestamp() {
        return viewTimestamp;
    }

    public void setViewTimestamp(Long viewTimestamp) {
        this.viewTimestamp = viewTimestamp;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
