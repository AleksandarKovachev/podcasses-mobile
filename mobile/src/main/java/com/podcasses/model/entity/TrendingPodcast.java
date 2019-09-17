package com.podcasses.model.entity;

import androidx.room.Entity;
import androidx.room.Index;

import com.podcasses.model.entity.base.Podcast;

/**
 * Created by aleksandar.kovachev.
 */
@Entity(inheritSuperIndices = true)
public class TrendingPodcast extends Podcast {

    public TrendingPodcast() {
    }

    public TrendingPodcast(Podcast podcast) {
        super(podcast);
    }

}
