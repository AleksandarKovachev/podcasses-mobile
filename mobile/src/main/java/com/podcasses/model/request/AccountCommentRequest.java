package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountCommentRequest {

    private String commentId;

    private Integer likeStatus;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public Integer getLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(Integer likeStatus) {
        this.likeStatus = likeStatus;
    }

}
