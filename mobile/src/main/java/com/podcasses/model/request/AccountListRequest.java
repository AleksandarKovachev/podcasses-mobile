package com.podcasses.model.request;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountListRequest {

    private Long accountListId;

    private String name;

    private String podcastId;

    public Long getAccountListId() {
        return accountListId;
    }

    public void setAccountListId(Long accountListId) {
        this.accountListId = accountListId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }
}
