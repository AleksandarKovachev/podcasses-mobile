package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.podcasses.R;
import com.podcasses.databinding.ItemAccountBinding;
import com.podcasses.model.response.Account;
import com.podcasses.viewmodel.base.BasePodcastViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private List<Account> accounts;
    private BasePodcastViewModel viewModel;

    public AccountAdapter(BasePodcastViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account,
                new FrameLayout(parent.getContext()), false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(viewModel, position);
    }

    @Override
    public int getItemCount() {
        return accounts == null ? 0 : accounts.size();
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

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ItemAccountBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            bind();
        }

        void setData(BasePodcastViewModel viewModel, Integer position) {
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
