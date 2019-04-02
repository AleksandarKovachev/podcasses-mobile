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
import com.google.android.gms.common.util.CollectionUtils;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentSearchBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.service.AudioPlayerService;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.view.base.FragmentCallback;
import com.podcasses.viewmodel.SearchViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by aleksandar.kovachev.
 */
public class SearchFragment extends BaseFragment implements Player.EventListener {

    @Inject
    ViewModelFactory viewModelFactory;

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private LiveData<ApiResponse> podcastsResponse;
    private List<Podcast> podcasts;

    private Podcast playingPodcast;
    private IBinder binder;
    private AudioPlayerService service;
    private SimpleExoPlayer player;

    private static String podcast;

    static SearchFragment newInstance(int instance, String text) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        podcast = text;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        updateTitle();

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel.class);
        binding.setViewModel(viewModel);

        binding.refreshLayout.setOnRefreshListener(this::getPodcasts);
        getPodcasts(binding.refreshLayout);

        setListClick();

        service = ((AudioPlayerService.LocalBinder) binder).getService();
        player = service.getPlayerInstance();

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
            FragmentCallback fragmentCallback = (FragmentCallback) context;
            binder = fragmentCallback.getBinder();
        } catch (ClassCastException e) {
            Log.e(getTag(), "Activity (Context) must implement FragmentCallback");
            throw new RuntimeException();
        }
    }

    private void getPodcasts(RefreshLayout refreshLayout) {
        podcastsResponse = viewModel.podcasts(this, podcast, true, false);
        podcastsResponse.observe(this, response -> consumeResponse(response, podcastsResponse, refreshLayout));
    }

    void updateTitle() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(podcast);
        }
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                refreshLayout.autoRefreshAnimationOnly();
                break;
            case SUCCESS:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                podcasts = (List<Podcast>) apiResponse.data;
                viewModel.setPodcastsInAdapter(podcasts);
                if (player != null) {
                    setPlayingStatus(player.getPlayWhenReady());
                }
                break;
            case ERROR:
                liveData.removeObservers(this);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                logError(apiResponse);
                break;
            default:
                break;
        }
    }

    private void setListClick() {
        viewModel.getSelected().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId()));
                viewModel.getSelected().setValue(null);
            }
        });
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playingPodcast = service.getPodcast();
        setPlayingStatus(playWhenReady);

        if (playbackState == Player.STATE_IDLE) {
            viewModel.setPlayingIndex(-1);
        }
    }

    private void setPlayingStatus(boolean playingStatus) {
        if (!playingStatus) {
            viewModel.setPlayingIndex(-1);
            return;
        }
        if (playingPodcast != null && !CollectionUtils.isEmpty(viewModel.getPodcasts())) {
            int i;
            for (i = 0; i < viewModel.getPodcasts().size(); i++) {
                if (playingPodcast.getId().equals(viewModel.getPodcasts().get(i).getId())) {
                    break;
                }
            }
            viewModel.setPlayingIndex(i);
        }
    }

}
