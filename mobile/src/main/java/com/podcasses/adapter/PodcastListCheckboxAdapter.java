package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.podcasses.R;
import com.podcasses.databinding.ItemCheckboxBinding;
import com.podcasses.model.dto.PodcastListCheckbox;
import com.podcasses.viewmodel.PodcastListDialogViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastListCheckboxAdapter extends RecyclerView.Adapter<PodcastListCheckboxAdapter.ViewHolder> {

    private List<PodcastListCheckbox> podcastListCheckboxes;

    private PodcastListDialogViewModel viewModel;

    public PodcastListCheckboxAdapter(PodcastListDialogViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public PodcastListCheckboxAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkbox,
                new FrameLayout(parent.getContext()), false);
        return new PodcastListCheckboxAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastListCheckboxAdapter.ViewHolder holder, int position) {
        holder.setData(viewModel, position);
    }

    @Override
    public int getItemCount() {
        return podcastListCheckboxes == null ? 0 : podcastListCheckboxes.size();
    }

    @Override
    public void onViewAttachedToWindow(PodcastListCheckboxAdapter.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.bind();
    }

    @Override
    public void onViewDetachedFromWindow(PodcastListCheckboxAdapter.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.unbind();
    }

    public void setPodcastListCheckBoxes(List<PodcastListCheckbox> podcastListCheckboxes) {
        this.podcastListCheckboxes = podcastListCheckboxes;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ItemCheckboxBinding binding;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bind();
        }

        void setData(PodcastListDialogViewModel viewModel, Integer position) {
            binding.setViewModel(viewModel);
            binding.setPosition(position);
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

    }

}
