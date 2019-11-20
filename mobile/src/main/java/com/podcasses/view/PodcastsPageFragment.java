package com.podcasses.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.podcasses.R;
import com.podcasses.constant.PodcastType;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastsPageBinding;
import com.podcasses.model.entity.base.Podcast;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.retrofit.AuthenticationCallInterface;
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

    @Inject
    AuthenticationCallInterface authenticationCallInterface;

    private PodcastsPageViewModel viewModel;

    private FragmentPodcastsPageBinding binder;
    private LiveData<ApiResponse> podcasts;
    private LiveData<String> token;

    private static PodcastType type;
    private static Long accountListId;

    private int page = 0;

    public static PodcastsPageFragment newInstance(int type) {
        return newInstance(0, type, null);
    }

    public static PodcastsPageFragment newInstance(int instance, int type, Long accountListId) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        args.putInt("type", type);
        if (accountListId != null) {
            args.putLong("accountListId", accountListId);
        }
        PodcastsPageFragment fragment = new PodcastsPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = PodcastType.getPodcastType(getArguments().getInt("type"));
        accountListId = getArguments().getLong("accountListId");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_podcasts_page, container, false);
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
        type = PodcastType.getPodcastType(getArguments().getInt("type"));
        accountListId = getArguments().getLong("accountListId", -1);
        if (accountListId != -1) {
            token = AuthenticationUtil.getAuthenticationToken(this.getContext(), authenticationCallInterface);
            if (token != null) {
                token.observe(getViewLifecycleOwner(), s -> {
                    if (!Strings.isEmptyOrWhitespace(s)) {
                        token.removeObservers(getViewLifecycleOwner());
                        getPodcasts(s, null);
                    }
                });
            }
        } else if (type == PodcastType.DOWNLOADED) {
            getDownloadedPodcasts();
        } else if (type == PodcastType.HISTORY ||
                type == PodcastType.LIKED_PODCASTS ||
                type == PodcastType.FROM_SUBSCRIPTIONS ||
                type == PodcastType.IN_PROGRESS) {
            token = AuthenticationUtil.getAuthenticationToken(this.getContext(), authenticationCallInterface);
            if (token != null) {
                token.observe(getViewLifecycleOwner(), s -> {
                    if (!Strings.isEmptyOrWhitespace(s)) {
                        token.removeObservers(getViewLifecycleOwner());
                        getPodcasts(s, null);
                    } else {
                        getPodcasts(null, null);
                    }
                });
            } else {
                getPodcasts(null, null);
            }
        }
        setPodcastClick();
        setChannelClick();
        setPodcastScrollListener();
    }

    private void getDownloadedPodcasts() {
        viewModel.getDownloadedPodcasts(page).observe(getViewLifecycleOwner(), podcasts -> viewModel.setPodcastsInAdapter((List<Object>) (Object) podcasts));
    }

    private void getPodcasts(String token, RefreshLayout refreshLayout) {
        if (accountListId != null && accountListId != -1) {
            podcasts = viewModel.getPodcastsFromAccountList(token, accountListId, page);
        } else if (type == PodcastType.FROM_SUBSCRIPTIONS) {
            podcasts = viewModel.getPodcastsFromSubscriptions(this, token, refreshLayout != null, page);
        } else {
            podcasts = viewModel.getPodcastsByType(this, token, type == PodcastType.LIKED_PODCASTS ? 1 : null,
                    type, refreshLayout != null, page);
        }
        podcasts.observe(getViewLifecycleOwner(), apiResponse -> consumeResponse(apiResponse, podcasts, refreshLayout));
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        page = 0;
        getPodcasts(token == null ? null : token.getValue(), refreshLayout);
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
                    viewModel.clearPodcastsInAdapter();
                    refreshLayout.finishRefresh();
                }
                setDataFromResponse(apiResponse);
                break;
            case ERROR:
                liveData.removeObservers(getViewLifecycleOwner());
                viewModel.setIsLoading(false);
                if (refreshLayout != null) {
                    viewModel.clearPodcastsInAdapter();
                    refreshLayout.finishRefresh();
                }
                LogErrorResponseUtil.logErrorApiResponse(apiResponse, getContext());
                break;
            case FETCHED:
                liveData.removeObservers(getViewLifecycleOwner());
                viewModel.setIsLoading(false);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                break;
        }
    }

    private void setDataFromResponse(@NonNull ApiResponse apiResponse) {
        viewModel.setIsLoading(false);
        if (apiResponse.data == null || CollectionUtils.isEmpty(((List) apiResponse.data))) {
            return;
        }
        if (((List) apiResponse.data).get(0) instanceof Podcast) {
            viewModel.setPodcastsInAdapter((List<Object>) apiResponse.data);
        }
    }

    private void setPodcastClick() {
        viewModel.getSelectedPodcast().observe(getViewLifecycleOwner(), podcast -> {
            if (podcast != null) {
                fragmentNavigation.pushFragment(PodcastFragment.newInstance(fragmentCount + 1, podcast.getId(), podcast));
                viewModel.getSelectedPodcast().setValue(null);
            }
        });
    }

    private void setChannelClick() {
        viewModel.getSelectedChannelId().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getSelectedChannelId().get() != null) {
                    fragmentNavigation.pushFragment(PodcastChannelFragment.newInstance(fragmentCount + 1, viewModel.getSelectedChannelId().get(), null, false));
                    viewModel.getSelectedChannelId().set(null);
                }
            }
        });
    }

    private void setPodcastScrollListener() {
        binder.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                if (!viewModel.getIsLoading()) {
                    ++page;
                    viewModel.setIsLoading(true);
                    getPodcasts(token.getValue(), null);
                }
            }
        });
    }

}
