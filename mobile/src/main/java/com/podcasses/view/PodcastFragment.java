package com.podcasses.view;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.retrofit.util.ApiResponse;
import com.podcasses.service.AudioPlayerService;
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
public class PodcastFragment extends BaseFragment implements Player.EventListener {

    public interface Callback {
        IBinder getBinder();
    }

    @Inject
    ViewModelFactory viewModelFactory;

    private FragmentPodcastBinding binding;

    private PodcastViewModel viewModel;

    private static String id;

    private LiveData<ApiResponse> response;

    private Podcast playingPodcast;
    private IBinder binder;
    private AudioPlayerService service;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast, container, false);
        binding.setLifecycleOwner(this);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        binding.refreshLayout.setOnRefreshListener(refreshListener);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel.class);
        binding.setViewModel(viewModel);

        viewModel.setPodcastImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PODCAST_IMAGE + id);

        response = viewModel.podcast(this, id, false);
        response.observe(this, apiResponse -> consumeResponse(apiResponse, null));

        service = ((AudioPlayerService.LocalBinder) binder).getService();
        SimpleExoPlayer player = service.getPlayerInstance();

        if (player != null) {
            playingPodcast = service.getPodcast();
            player.addListener(this);
            setPlayingStatus(player.getPlayWhenReady());
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Callback callback = (Callback) context;
            binder = callback.getBinder();
        } catch (ClassCastException e) {
            Log.e(getTag(), "Activity (Context) must implement Callback");
            throw new RuntimeException();
        }
    }

    private OnRefreshListener refreshListener = refreshLayout -> {
        response = viewModel.podcast(this, id, true);
        response.observe(this, apiResponse -> consumeResponse(apiResponse, refreshLayout));
    };

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
                    viewModel.setPodcast((Podcast) apiResponse.data);
                } else {
                    viewModel.setPodcast(((List<Podcast>) apiResponse.data).get(0));
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

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playingPodcast = service.getPodcast();
        setPlayingStatus(playWhenReady);
    }

    private void setPlayingStatus(boolean playingStatus) {
        if (playingPodcast != null && playingPodcast.getId().equals(id)) {
            binding.playButton.change(!playingStatus);
        }
    }

}
