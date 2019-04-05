package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class CommentRequest {

    private String comment;

    private String podcastId;

    private String commentId;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
}
