package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastReportRequest {

    private String podcastId;

    private String report;

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
