package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.R;
import com.podcasses.constant.PodcastTypeEnum;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastsPageBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.PodcastsPageViewModel;
import com.podcasses.viewmodel.ViewModelFactory;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastsPageFragment extends BaseFragment implements OnRefreshListener {

    @Inject
    ViewModelFactory viewModelFactory;

    private PodcastsPageViewModel viewModel;

    private LiveData<ApiResponse> podcasts;
    private LiveData<String> token;
    private PodcastTypeEnum type;

    public static PodcastsPageFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, 0);
        args.putInt("type", type);
        PodcastsPageFragment fragment = new PodcastsPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = PodcastTypeEnum.getPodcastType(getArguments().getInt("type"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPodcastsPageBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_podcasts_page, container, false);
        binder.setLifecycleOwner(getViewLifecycleOwner());
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastsPageViewModel.class);
        binder.setViewModel(viewModel);
        binder.refreshLayout.setOnRefreshListener(this);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (type == PodcastTypeEnum.DOWNLOADED) {
            getDownloadedPodcasts();
        } else if (type == PodcastTypeEnum.HISTORY || type == PodcastTypeEnum.LIKED_PODCASTS
                || type == PodcastTypeEnum.FROM_SUBSCRIPTIONS) {
            token = AuthenticationUtil.isAuthenticated(this.getContext(), this);
            token.observe(getViewLifecycleOwner(), s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    token.removeObservers(getViewLifecycleOwner());
                    getPodcasts(s, null);
                } else {
                    getPodcasts(null, null);
                }
            });
        }
        setPodcastClick();
        setAccountClick();
    }

    private void getDownloadedPodcasts() {
        viewModel.getDownloadedPodcasts().observe(this, podcasts -> viewModel.setPodcastsInSimpleAdapter(podcasts));
    }

    private void getPodcasts(String token, RefreshLayout refreshLayout) {
        if (type == PodcastTypeEnum.FROM_SUBSCRIPTIONS) {
            podcasts = viewModel.getPodcastsFromSubscriptions(this, token, refreshLayout != null);
        } else {
            podcasts = viewModel.getHistoryPodcasts(this, token, type == PodcastTypeEnum.LIKED_PODCASTS ? 1 : null, type, refreshLayout != null);
        }
        podcasts.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcasts, refreshLayout));
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getPodcasts(token.getValue(), refreshLayout);
    }

    private void consumeResponse(@NonNull ApiResponse apiResponse, LiveData liveData, RefreshLayout refreshLayout) {
        switch (apiResponse.status) {
            case LOADING:
                break;
            case DATABASE:
                setDataFromResponse(apiResponse);
                break;
            case SUCCESS:
                liveData.removeObservers(getViewLifecycleOwner());
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                setDataFromResponse(apiResponse);
                break;
            case ERROR:
                liveData.removeObservers(getViewLifecycleOwner());
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            case FETCHED:
                liveData.removeObservers(getViewLifecycleOwner());
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                break;
        }
    }

    private void setDataFromResponse(@NonNull ApiResponse apiResponse) {
        if (apiResponse.data == null || CollectionUtils.isEmpty(((List) apiResponse.data))) {
            return;
        }
        if (((List) apiResponse.data).get(0) instanceof Podcast) {
            List<Podcast> data = (List<Podcast>) apiResponse.data;
            if (data.size() > 3) {
                data = data.subList(0, 3);
            }
            viewModel.setPodcastsInSimpleAdapter(data);
        }
    }

    private void setPodcastClick() {
        viewModel.getSelectedPodcast().observe(this, podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setAccountClick() {
        viewModel.getSelectedAccount().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedAccount().get() != null) {
                    fragmentNavigation.pushFragment(AccountFragment.newInstance(fragmentCount + 1, viewModel.getSelectedAccount().get()));
                    viewModel.getSelectedAccount().set(null);
                }
            }
        });
    }

}
