package com.podcasses.model.base;

import com.podcasses.BR;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseLikeModel extends BaseUserModel {

    private int likes;

    private int dislikes;

    @Bindable
    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
        notifyPropertyChanged(BR.likes);
    }

    @Bindable
    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
        notifyPropertyChanged(BR.dislikes);
    }

}
