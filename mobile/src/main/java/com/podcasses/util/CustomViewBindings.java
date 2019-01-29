package com.podcasses.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.podcasses.R;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by aleksandar.kovachev.
 */
public class CustomViewBindings {

    public static final String PROFILE_IMAGE = "/account/image/";

    public static final String COVER_IMAGE = "/account/cover/";

    public static final String PODCAST_IMAGE = "/podcast/image/";

    @BindingAdapter("bind:setAdapter")
    public static void bindRecyclerViewAdapter(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    @BindingAdapter("bind:imageUrl")
    public static void loadImage(ImageView view, String url) {
        Glide.with(view).load(url).apply(RequestOptions.placeholderOf(R.drawable.cover_placeholder)).into(view);
    }

}
