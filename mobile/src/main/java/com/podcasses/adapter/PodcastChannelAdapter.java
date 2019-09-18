package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.podcasses.R;
import com.podcasses.databinding.ItemPodcastChannelBinding;
import com.podcasses.databinding.ItemPodcastChannelMiniBinding;
import com.podcasses.viewmodel.base.BasePodcastChannelViewModel;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> podcastChannels;
    private int layoutId;
    private int adLayout;
    private BasePodcastChannelViewModel podcastChannelViewModel;
    private BasePodcastViewModel podcastViewModel;

    private static final int NATIVE_AD_VIEW_TYPE = 1;

    public PodcastChannelAdapter(@LayoutRes int layoutId, @LayoutRes int adLayout, BasePodcastChannelViewModel podcastChannelViewModel) {
        this.layoutId = layoutId;
        this.podcastChannelViewModel = podcastChannelViewModel;
        this.adLayout = adLayout;
    }

    public PodcastChannelAdapter(BasePodcastViewModel podcastViewModel) {
        this.layoutId = R.layout.item_podcast_channel_mini;
        this.podcastViewModel = podcastViewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NATIVE_AD_VIEW_TYPE) {
            View unifiedNativeLayoutView = LayoutInflater.from(
                    parent.getContext()).inflate(adLayout,
                    parent, false);
            return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId,
                    new FrameLayout(parent.getContext()), false);
            return new PodcastChannelViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == NATIVE_AD_VIEW_TYPE) {
            UnifiedNativeAd nativeAd = (UnifiedNativeAd) podcastChannels.get(position);
            populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
        } else {
            if (podcastViewModel != null) {
                ((PodcastChannelViewHolder) holder).setData(podcastViewModel, position);
            } else {
                ((PodcastChannelViewHolder) holder).setData(podcastChannelViewModel, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return podcastChannels == null ? 0 : podcastChannels.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object recyclerViewItem = podcastChannels.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return NATIVE_AD_VIEW_TYPE;
        }
        return layoutId;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof PodcastChannelViewHolder) {
            ((PodcastChannelViewHolder) holder).bind();
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof PodcastChannelViewHolder) {
            ((PodcastChannelViewHolder) holder).unbind();
        }
    }

    public void setPodcastChannels(List<Object> podcastChannels) {
        this.podcastChannels = podcastChannels;
        notifyDataSetChanged();
    }

    public void addElement(Object element) {
        if (podcastChannels != null && !podcastChannels.isEmpty()) {
            this.podcastChannels.add(element);
            notifyDataSetChanged();
        }
    }

    class UnifiedNativeAdViewHolder extends RecyclerView.ViewHolder {

        private UnifiedNativeAdView adView;

        UnifiedNativeAdView getAdView() {
            return adView;
        }

        UnifiedNativeAdViewHolder(View view) {
            super(view);
            adView = view.findViewById(R.id.ad_view);
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setIconView(adView.findViewById(R.id.ad_icon));
            adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
            adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        }
    }

    class PodcastChannelViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        PodcastChannelViewHolder(View itemView) {
            super(itemView);
            bind();
        }

        void bind() {
            if (binding == null) {
                binding = DataBindingUtil.bind(itemView);
            }
        }

        void unbind() {
            if (binding != null) {
                binding.unbind();
            }
        }

        void setData(BasePodcastViewModel viewModel, int position) {
            if (binding != null) {
                ((ItemPodcastChannelMiniBinding) binding).setViewModel(viewModel);
                ((ItemPodcastChannelMiniBinding) binding).setPosition(position);
            }
        }

        void setData(BasePodcastChannelViewModel viewModel, int position) {
            if (binding != null) {
                if (layoutId == R.layout.item_podcast_channel) {
                    ((ItemPodcastChannelBinding) binding).setViewModel(viewModel);
                    ((ItemPodcastChannelBinding) binding).setPosition(position);
                }
            }
        }
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (adView.getStarRatingView() != null) {
            if (nativeAd.getStarRating() == null) {
                adView.getStarRatingView().setVisibility(View.GONE);
            } else {
                ((RatingBar) adView.getStarRatingView())
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

}
