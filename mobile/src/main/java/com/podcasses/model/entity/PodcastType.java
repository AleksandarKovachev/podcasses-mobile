package com.podcasses.model.entity;

/**
 * Created by aleksandar.kovachev.
 */
public enum PodcastType {

    MY_PODCASTS(1),
    HISTORY(3),
    LIKED_PODCASTS(3),
    TRENDING(4);

    private int type;

    PodcastType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
