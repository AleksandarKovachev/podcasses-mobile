package com.podcasses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.podcasses.R;
import com.podcasses.databinding.ItemAccountListBinding;
import com.podcasses.model.response.AccountList;
import com.podcasses.viewmodel.AccountViewModel;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.ViewHolder> {

    private List<AccountList> accountLists;

    private AccountViewModel viewModel;

    public AccountListAdapter(AccountViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_list,
                new FrameLayout(parent.getContext()), false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(viewModel, position);
    }

    @Override
    public int getItemCount() {
        return accountLists == null ? 0 : accountLists.size();
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

    public void setAccountLists(List<AccountList> accountLists) {
        this.accountLists = accountLists;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ItemAccountListBinding binding;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bind();
        }

        void setData(AccountViewModel viewModel, Integer position) {
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
