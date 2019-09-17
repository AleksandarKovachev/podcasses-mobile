package com.podcasses.model.entity;

import androidx.room.Entity;
import androidx.room.Index;

import com.podcasses.model.entity.base.Podcast;

/**
 * Created by aleksandar.kovachev.
 */
@Entity(inheritSuperIndices = true)
public class LikedPodcast extends Podcast {

    public LikedPodcast() {
    }

    public LikedPodcast(Podcast podcast) {
        super(podcast);
    }

}
