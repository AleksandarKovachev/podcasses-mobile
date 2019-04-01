package com.podcasses.util;

import com.podcasses.model.entity.BaseLikeModel;

/**
 * Created by aleksandar.kovachev.
 */
public class LikeStatusUtil {

    private LikeStatusUtil() {
    }

    public static void updateLikeStatus(BaseLikeModel likeModel, Integer likeStatus, Integer previousLikeStatus) {
        if (likeStatus == LikeStatus.LIKE.getValue()) {
            if (previousLikeStatus == LikeStatus.LIKE.getValue()) {
                likeModel.setLikes(likeModel.getLikes() - 1);
            } else if (previousLikeStatus == LikeStatus.DISLIKE.getValue()) {
                likeModel.setLikes(likeModel.getLikes() + 1);
                likeModel.setDislikes(likeModel.getDislikes() - 1);
            } else {
                likeModel.setLikes(likeModel.getLikes() + 1);
            }
        } else if (likeStatus == LikeStatus.DISLIKE.getValue()) {
            if (previousLikeStatus == LikeStatus.DISLIKE.getValue()) {
                likeModel.setDislikes(likeModel.getDislikes() - 1);
            } else if (previousLikeStatus == LikeStatus.LIKE.getValue()) {
                likeModel.setLikes(likeModel.getLikes() - 1);
                likeModel.setDislikes(likeModel.getDislikes() + 1);
            } else {
                likeModel.setDislikes(likeModel.getDislikes() + 1);
            }
        } else {
            if (previousLikeStatus == LikeStatus.LIKE.getValue()) {
                likeModel.setLikes(likeModel.getLikes() - 1);
            } else if (previousLikeStatus == LikeStatus.DISLIKE.getValue()) {
                likeModel.setDislikes(likeModel.getDislikes() - 1);
            }
        }
    }

}
