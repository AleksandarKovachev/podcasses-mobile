package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class CommentReportRequest {

    private String commentId;

    private String report;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
