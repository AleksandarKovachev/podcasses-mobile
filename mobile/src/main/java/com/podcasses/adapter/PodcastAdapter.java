package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.podcasses.R;
import com.podcasses.databinding.ItemPodcastBinding;
import com.podcasses.databinding.ItemTrendingPodcastBinding;
import com.podcasses.model.entity.Podcast;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {

    private List<Podcast> podcasts;
    private int layoutId;
    private BasePodcastViewModel viewModel;

    public PodcastAdapter(@LayoutRes int layoutId, BasePodcastViewModel viewModel) {
        this.layoutId = layoutId;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId,
                new FrameLayout(parent.getContext()), false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(viewModel, position);
    }

    @Override
    public int getItemCount() {
        return podcasts == null ? 0 : podcasts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return layoutId;
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.bind();
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.unbind();
    }

    public void setPodcasts(List<Podcast> podcasts) {
        this.podcasts = podcasts;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        ViewHolder(View itemView) {
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


}
