package com.podcasses.util;

/**
 * Created by aleksandar.kovachev.
 */
public enum LikeStatus {

    DISLIKED(2),
    LIKED(1),
    DEFAULT(0);

    private int value;

    LikeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
