package com.example.smartexpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
        
        String displayType = wallet.getType() != null ? wallet.getType().toUpperCase() : "WALLET";
        holder.tvWalletType.setText(displayType);
        holder.tvWalletName.setText(wallet.getName() != null ? wallet.getName() : "");
        holder.tvWalletBalance.setText(wallet.getBalance() != null ? formatter.format(wallet.getBalance()) + "đ" : "0đ");

        // Set dynamic gradient background based on wallet type
        String type = wallet.getType() != null ? wallet.getType().toLowerCase().trim() : "";
        int bgResId = R.drawable.grad_wallet_default;
        if (type.contains("bank")) {
            bgResId = R.drawable.grad_wallet_bank;
        } else if (type.contains("credit")) {
            bgResId = R.drawable.grad_wallet_credit;
        } else if (type.contains("cash") || type.contains("tiền mặt")) {
            bgResId = R.drawable.grad_wallet_cash;
        } else if (type.contains("e-wallet") || type.contains("ewallet") || type.contains("ví") || type.contains("momo") || type.contains("pay")) {
            bgResId = R.drawable.grad_wallet_ewallet;
        }
        holder.layoutWalletCardBg.setBackgroundResource(bgResId);

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
        RelativeLayout layoutWalletCardBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWalletType = itemView.findViewById(R.id.tv_wallet_type);
            tvWalletName = itemView.findViewById(R.id.tv_wallet_name);
            tvWalletBalance = itemView.findViewById(R.id.tv_wallet_balance);
            layoutWalletCardBg = itemView.findViewById(R.id.layout_wallet_card_bg);
        }
    }

    public interface OnWalletActionListener {
        void onWalletClick(Wallet wallet);
        void onWalletLongClick(Wallet wallet);
    }
}
