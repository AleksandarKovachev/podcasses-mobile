package com.podcasses.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ohoussein.playpause.PlayPauseView;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.service.MediaPlayerService;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.PodcastViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private PodcastViewModel viewModel;

    private static String id;

    private LiveData<ApiResponse> response;

    private MediaPlayerService player;

    private boolean serviceBound;

    private Podcast podcast;

    static PodcastFragment newInstance(int instance, String podcastId) {
        id = podcastId;
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        PodcastFragment fragment = new PodcastFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPodcastBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast, container, false);
        binder.setLifecycleOwner(this);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        binder.refreshLayout.setOnRefreshListener(refreshListener);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel.class);
        binder.setViewModel(viewModel);

        viewModel.setPodcastImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PODCAST_IMAGE + id);

        response = viewModel.podcast(this, id, false);
        response.observe(this, apiResponse -> consumeResponse(apiResponse, null));

        if(savedInstanceState != null) {
            serviceBound = savedInstanceState.getBoolean(MainActivity.SERVICE_STATUS, false);
        }

        binder.playButton.setOnClickListener(playClickListener);

        return binder.getRoot();
    }

    private OnRefreshListener refreshListener = refreshLayout -> {
        response = viewModel.podcast(this, id, true);
        response.observe(this, apiResponse -> consumeResponse(apiResponse, refreshLayout));
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            getContext().unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case SUCCESS:
                response.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                if (apiResponse.data instanceof Podcast) {
                    podcast = (Podcast) apiResponse.data;
                    viewModel.setPodcast(podcast);
                } else {
                    podcast = ((List<Podcast>) apiResponse.data).get(0);
                    viewModel.setPodcast(podcast);
                }
                break;
            case ERROR:
                response.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                logError(apiResponse);
                break;
            default:
                break;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private View.OnClickListener playClickListener = v -> {
        ((PlayPauseView) v).toggle();
        playAudio();
    };

    private void playAudio() {
        if (!serviceBound) {
            Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
            playerIntent.putExtra("podcastTitle", podcast.getTitle());
            playerIntent.putExtra("podcastUser", podcast.getUserId());
            playerIntent.putExtra("podcastId", podcast.getId());
            getContext().startService(playerIntent);
            getContext().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(MediaPlayerService.Broadcast_PLAY_NEW_AUDIO);
            getContext().sendBroadcast(broadcastIntent);
        }
    }

}
