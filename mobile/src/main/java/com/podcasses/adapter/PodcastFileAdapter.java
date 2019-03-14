package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.viewmodel.AccountViewModel;
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
public class PodcastFileAdapter extends RecyclerView.Adapter<PodcastFileAdapter.ViewHolder> {

    private List<PodcastFile> podcastFiles;
    private int layoutId;
    private AccountViewModel viewModel;

    public PodcastFileAdapter(@LayoutRes int layoutId, AccountViewModel viewModel) {
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
        return podcastFiles == null ? 0 : podcastFiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return layoutId;
    }

    public void setPodcasts(List<PodcastFile> podcasts) {
        this.podcastFiles = podcasts;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BasePodcastViewModel viewModel, Integer position) {
            binding.getRoot().findViewById(R.id.podcast_file_name).setSelected(true);
            binding.setVariable(BR.position, position);
            binding.setVariable(BR.viewModel, viewModel);
            binding.executePendingBindings();
        }

    }


}
