package com.example.smartexpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.models.Wallet;
import java.text.DecimalFormat;
import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {
    private final List<Wallet> walletList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final OnWalletActionListener listener;

    public WalletAdapter(List<Wallet> walletList, OnWalletActionListener listener) {
        this.walletList = walletList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet wallet = walletList.get(position);
        holder.tvWalletType.setText(wallet.getType() != null ? wallet.getType().toUpperCase() : "");
        holder.tvWalletName.setText(wallet.getName() != null ? wallet.getName() : "");
        holder.tvWalletBalance.setText(wallet.getBalance() != null ? formatter.format(wallet.getBalance()) + "đ" : "0đ");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWalletClick(wallet);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onWalletLongClick(wallet);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWalletType, tvWalletName, tvWalletBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWalletType = itemView.findViewById(R.id.tv_wallet_type);
            tvWalletName = itemView.findViewById(R.id.tv_wallet_name);
            tvWalletBalance = itemView.findViewById(R.id.tv_wallet_balance);
        }
    }

    public interface OnWalletActionListener {
        void onWalletClick(Wallet wallet);
        void onWalletLongClick(Wallet wallet);
    }
}
