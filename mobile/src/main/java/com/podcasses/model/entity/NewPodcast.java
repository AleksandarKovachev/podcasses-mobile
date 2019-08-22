package com.podcasses.model.entity;

import androidx.room.Entity;
import androidx.room.Index;

import com.podcasses.model.entity.base.Podcast;

/**
 * Created by aleksandar.kovachev.
 */
@Entity(indices = {@Index(value = {"id"}, unique = true)})
public class NewPodcast extends Podcast {

    public NewPodcast() {
    }

    public NewPodcast(Podcast podcast) {
        super(podcast);
    }

}
