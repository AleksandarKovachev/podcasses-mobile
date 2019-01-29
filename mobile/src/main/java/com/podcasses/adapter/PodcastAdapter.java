package com.podcasses.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.podcasses.BR;
import com.podcasses.model.entity.Podcast;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

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
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, viewType, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(viewModel, position);
    }

    @Override
    public int getItemCount() {
        return podcasts == null ? 0 : podcasts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getLayoutIdForPosition(position);
    }

    private int getLayoutIdForPosition(int position) {
        return layoutId;
    }

    public void setPodcasts(List<Podcast> podcasts) {
        this.podcasts = podcasts;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BasePodcastViewModel viewModel, Integer position) {
            binding.setVariable(BR.position, position);
            binding.setVariable(BR.viewModel, viewModel);
            binding.executePendingBindings();
        }

    }


}
