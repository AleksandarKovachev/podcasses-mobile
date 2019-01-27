package com.podcasses.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.databinding.BindingAdapter;

/**
 * Created by aleksandar.kovachev.
 */
public class GlideUtil {

    public static final String PROFILE_IMAGE = "/account/image/";

    public static final String COVER_IMAGE = "/account/image/";

    @BindingAdapter("bind:imageUrl")
    public static void loadImage(ImageView view, String url) {
        Glide.with(view).load(url).into(view);
    }

}
