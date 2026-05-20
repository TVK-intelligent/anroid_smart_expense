package com.example.smartexpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.models.Transaction;
import com.google.android.material.card.MaterialCardView;
import java.text.DecimalFormat;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final List<Transaction> transactionList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final OnTransactionActionListener listener;

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionActionListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        
        // Map category ID to display names
        String categoryName = "Other";
        int iconRes = android.util.TypedValue.applyDimension(1, 1, holder.itemView.getResources().getDisplayMetrics()) > 0 
                ? android.R.drawable.ic_menu_today : android.R.drawable.ic_menu_today; // Fallback
        
        int categoryId = transaction.getCategoryId() != null ? transaction.getCategoryId() : 0;
        switch (categoryId) {
            case 1:
                categoryName = "Dining & Food";
                iconRes = android.R.drawable.ic_menu_compass; // Mock icons
                break;
            case 2:
                categoryName = "Transport";
                iconRes = android.R.drawable.ic_menu_directions;
                break;
            case 3:
                categoryName = "Shopping";
                iconRes = android.R.drawable.ic_menu_gallery;
                break;
            case 4:
                categoryName = "Housing";
                iconRes = android.R.drawable.ic_menu_myplaces;
                break;
            case 5:
                categoryName = "Leisure";
                iconRes = android.R.drawable.ic_menu_slideshow;
                break;
            default:
                categoryName = "Other";
                iconRes = android.R.drawable.ic_menu_today;
                break;
        }

        holder.tvCategory.setText(categoryName);
        holder.tvNote.setText(transaction.getNote() != null ? transaction.getNote() : "");
        holder.ivIcon.setImageResource(iconRes);

        boolean isExpense = true;
        if (transaction.getType() != null) {
            isExpense = "EXPENSE".equalsIgnoreCase(transaction.getType());
        } else {
            // fallback: old mock mapping
            isExpense = categoryId != 6;
        }
        java.math.BigDecimal amount = transaction.getAmount() != null ? transaction.getAmount() : java.math.BigDecimal.ZERO;
        if (isExpense) {
            holder.tvAmount.setText("-" + formatter.format(amount) + "đ");
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.crimson_expense));
        } else {
            holder.tvAmount.setText("+" + formatter.format(amount) + "đ");
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.emerald_income));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(transaction);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTransactionLongClick(transaction);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCategory, tvNote, tvAmount;
        MaterialCardView cardIconBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_trans_icon);
            tvCategory = itemView.findViewById(R.id.tv_trans_category);
            tvNote = itemView.findViewById(R.id.tv_trans_note);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            cardIconBg = itemView.findViewById(R.id.card_trans_icon_bg);
        }
    }

    public interface OnTransactionActionListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }
}
