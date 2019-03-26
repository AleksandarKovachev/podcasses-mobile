package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.model.response.Comment;
import com.podcasses.viewmodel.PodcastViewModel;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastCommentAdapter extends RecyclerView.Adapter<PodcastCommentAdapter.ViewHolder> {

    private List<Comment> comments;
    private int layoutId;
    private PodcastViewModel viewModel;

    public PodcastCommentAdapter(@LayoutRes int layoutId, PodcastViewModel viewModel) {
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
        return comments == null ? 0 : comments.size();
    }

    @Override
    public int getItemViewType(int position) {
        return layoutId;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PodcastViewModel viewModel, Integer position) {
            binding.setVariable(BR.position, position);
            binding.setVariable(BR.viewModel, viewModel);
            binding.executePendingBindings();
        }
    }

}
