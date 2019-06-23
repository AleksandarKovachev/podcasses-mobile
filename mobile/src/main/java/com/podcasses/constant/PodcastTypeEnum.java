package com.podcasses.constant;

import android.content.Context;

import com.podcasses.R;

/**
 * Created by aleksandar.kovachev.
 */
public enum PodcastTypeEnum {

    MY_PODCASTS(1, R.string.my_podcasts),
    HISTORY(2, R.string.history),
    LIKED_PODCASTS(3, R.string.liked_podcasts),
    TRENDING(4, R.string.trending_podcasts),
    DOWNLOADED(5, R.string.downloaded_podcasts),
    IN_PROGRESS(6, R.string.podcasts_in_progress),
    FROM_SUBSCRIPTIONS(7, R.string.podcasts_from_subscribe),
    MARK_AS_PLAYED(8, R.string.mark_as_played);

    private int type;
    private int title;

    PodcastTypeEnum(int type, int title) {
        this.type = type;
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public int getTitle() {
        return title;
    }

    public static String getTitle(Context context, int type) {
        for (PodcastTypeEnum podcastType : values()) {
            if (podcastType.getType() == type) {
                return context.getString(podcastType.getTitle());
            }
        }
        return null;
    }

    public static PodcastTypeEnum getPodcastType(int type) {
        for (PodcastTypeEnum podcastType : values()) {
            if (podcastType.getType() == type) {
                return podcastType;
            }
        }
        return null;
    }
}