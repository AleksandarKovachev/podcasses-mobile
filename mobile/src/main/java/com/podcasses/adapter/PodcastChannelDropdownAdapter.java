package com.podcasses.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.podcasses.model.entity.PodcastChannel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelDropdownAdapter extends ArrayAdapter {

    private List<PodcastChannel> podcastChannels;

    public PodcastChannelDropdownAdapter(Context context, int textViewResourceId, List<PodcastChannel> podcastChannels, String prompt) {
        super(context, textViewResourceId, podcastChannels);
        this.podcastChannels = podcastChannels;
    }

    @Override
    public int getCount() {
        return podcastChannels.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return podcastChannels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setText(podcastChannels.get(position).getTitle());
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(podcastChannels.get(position).getTitle());
        return label;
    }

}
