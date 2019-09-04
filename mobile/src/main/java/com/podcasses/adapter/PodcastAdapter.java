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
import com.podcasses.databinding.ItemPodcastBinding;
import com.podcasses.databinding.ItemTrendingPodcastBinding;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> podcasts;
    private int layoutId;
    private int adLayout;
    private BasePodcastViewModel viewModel;

    private static final int NATIVE_AD_VIEW_TYPE = 1;

    public PodcastAdapter(@LayoutRes int layoutId, @LayoutRes int adLayout, BasePodcastViewModel viewModel) {
        this.layoutId = layoutId;
        this.viewModel = viewModel;
        this.adLayout = adLayout;
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
            return new PodcastViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == NATIVE_AD_VIEW_TYPE) {
            UnifiedNativeAd nativeAd = (UnifiedNativeAd) podcasts.get(position);
            populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
        } else {
            ((PodcastViewHolder) holder).setData(viewModel, position);
        }
    }

    @Override
    public int getItemCount() {
        return podcasts == null ? 0 : podcasts.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object recyclerViewItem = podcasts.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return NATIVE_AD_VIEW_TYPE;
        }
        return layoutId;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof PodcastViewHolder) {
            ((PodcastViewHolder) holder).bind();
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof PodcastViewHolder) {
            ((PodcastViewHolder) holder).unbind();
        }
    }

    public void setPodcasts(List<Object> podcasts) {
        this.podcasts = podcasts;
        notifyDataSetChanged();
    }

    public void addElement(Object element) {
        if(podcasts != null && !podcasts.isEmpty()) {
            this.podcasts.add(element);
            notifyDataSetChanged();
        }
    }

    public void addElement(Object element, int index) {
        if(podcasts != null && !podcasts.isEmpty()) {
            this.podcasts.add(index, element);
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

    class PodcastViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        PodcastViewHolder(View itemView) {
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
                if (layoutId == R.layout.item_podcast) {
                    ((ItemPodcastBinding) binding).setViewModel(viewModel);
                    ((ItemPodcastBinding) binding).setPosition(position);
                } else if (layoutId == R.layout.item_trending_podcast) {
                    ((ItemTrendingPodcastBinding) binding).setViewModel(viewModel);
                    ((ItemTrendingPodcastBinding) binding).setPosition(position);
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
