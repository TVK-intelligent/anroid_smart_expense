package com.example.smartexpense.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.models.DebtLoan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DebtLoanAdapter extends RecyclerView.Adapter<DebtLoanAdapter.ViewHolder> {

    public interface OnDebtActionListener {
        void onComplete(DebtLoan dl);
        void onDelete(DebtLoan dl);
    }

    private final List<DebtLoan> list;
    private final OnDebtActionListener listener;

    public DebtLoanAdapter(List<DebtLoan> list, OnDebtActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debt_loan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DebtLoan dl = list.get(position);
        holder.bind(dl, listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPartnerName, tvTypeLabel, tvAmount, tvInterest, tvDueDate, tvStatusBadge, tvNote;
        ImageView btnActionComplete, btnActionDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPartnerName = itemView.findViewById(R.id.tv_partner_name);
            tvTypeLabel = itemView.findViewById(R.id.tv_type_label);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvInterest = itemView.findViewById(R.id.tv_interest);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvNote = itemView.findViewById(R.id.tv_note);
            btnActionComplete = itemView.findViewById(R.id.btn_action_complete);
            btnActionDelete = itemView.findViewById(R.id.btn_action_delete);
        }

        public void bind(DebtLoan dl, OnDebtActionListener listener) {
            tvPartnerName.setText(dl.getPersonName());

            boolean isDebt = "DEBT".equalsIgnoreCase(dl.getType());
            tvTypeLabel.setText(isDebt ? "Tôi đi vay (DEBT)" : "Tôi cho vay (LOAN)");
            tvTypeLabel.setTextColor(itemView.getResources().getColor(isDebt ? R.color.crimson_expense : R.color.emerald_income));

            tvAmount.setText(formatCurrency(dl.getAmount()) + "đ");

            if (dl.getInterestRate() != null && dl.getInterestRate().compareTo(BigDecimal.ZERO) > 0) {
                tvInterest.setText("Lãi suất: " + dl.getInterestRate() + "%/năm");
                tvInterest.setVisibility(View.VISIBLE);
            } else {
                tvInterest.setVisibility(View.GONE);
            }

            if (dl.getDueDate() != null && !dl.getDueDate().trim().isEmpty()) {
                tvDueDate.setText("Hạn: " + dl.getDueDate());
                tvDueDate.setVisibility(View.VISIBLE);
            } else {
                tvDueDate.setVisibility(View.GONE);
            }

            boolean isPaid = "PAID".equalsIgnoreCase(dl.getStatus());
            tvStatusBadge.setText(isPaid ? "ĐÃ THANH TOÁN" : "CHƯA THANH TOÁN");
            
            // Set badge background color dynamically
            int badgeColor = itemView.getResources().getColor(isPaid ? R.color.emerald_income : R.color.amber_warning);
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

            if (dl.getNote() != null && !dl.getNote().trim().isEmpty()) {
                tvNote.setText("Ghi chú: " + dl.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Hide or show action mark completed button
            if (isPaid) {
                btnActionComplete.setVisibility(View.GONE);
            } else {
                btnActionComplete.setVisibility(View.VISIBLE);
                btnActionComplete.setOnClickListener(v -> listener.onComplete(dl));
            }

            btnActionDelete.setOnClickListener(v -> listener.onDelete(dl));
        }

        private String formatCurrency(BigDecimal amount) {
            if (amount == null) return "0";
            try {
                NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                return nf.format(amount.setScale(0, RoundingMode.HALF_UP));
            } catch (Exception e) {
                return amount.setScale(0, RoundingMode.HALF_UP).toString();
            }
        }
    }
}
