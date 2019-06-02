package com.podcasses.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.podcasses.model.entity.PodcastType;
import com.podcasses.view.PodcastsPageFragment;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastsPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private List<Integer> types;

    public PodcastsPagerAdapter(Context context, FragmentManager fragmentManager, List<Integer> types) {
        super(fragmentManager);
        this.context = context;
        this.types = types;
    }

    @Override
    public Fragment getItem(int position) {
        return PodcastsPageFragment.newInstance(types.get(position));
    }

    @Override
    public int getCount() {
        return types.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PodcastType.getTitle(context, types.get(position));
    }

}
