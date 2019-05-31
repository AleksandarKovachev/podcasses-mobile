package com.podcasses.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.podcasses.R;
import com.podcasses.view.AccountPodcastsFragment;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastsPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public PodcastsPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return AccountPodcastsFragment.newInstance(position, position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.history);
            case 1:
                return context.getString(R.string.liked_podcasts);
            default:
                return null;
        }
    }


}
