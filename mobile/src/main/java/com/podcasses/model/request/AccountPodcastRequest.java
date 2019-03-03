package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountPodcastRequest {

    private String podcastId;

    private Integer likeStatus;

    private Long timeIndex;

    private Integer markAsPlayed;

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
}
