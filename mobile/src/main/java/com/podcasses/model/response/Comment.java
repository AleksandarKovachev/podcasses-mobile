package com.podcasses.model.response;

import com.podcasses.BR;
import com.podcasses.model.base.BaseLikeModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.databinding.Bindable;

/**
 * Created by aleksandar.kovachev.
 */
public class Comment extends BaseLikeModel {

    private String id;

    private String comment;

    private String podcastId;

    private List<Comment> comments;

    private String username;

    private String userId;

    private Date createdTimestamp;

    private Date updatedTimestamp;

    private boolean isLiked;

    private boolean isDisliked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Bindable
    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
        notifyPropertyChanged(BR.liked);
    }

    @Bindable
    public boolean isDisliked() {
        return isDisliked;
    }

    public void setDisliked(boolean disliked) {
        isDisliked = disliked;
        notifyPropertyChanged(BR.disliked);
    }

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getDate() {
        DateFormat formatter = SimpleDateFormat.getDateInstance();
        return formatter.format(getCreatedTimestamp());
    }

}
