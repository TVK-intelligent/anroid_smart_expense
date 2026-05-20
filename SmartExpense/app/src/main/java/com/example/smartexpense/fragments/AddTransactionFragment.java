package com.example.smartexpense.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.AnomalyResponse;
import com.example.smartexpense.models.Transaction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.math.BigDecimal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionFragment extends Fragment {

    private TextView btnToggleExpense, btnToggleIncome;
    private EditText etAmount, etNote;
    private LinearLayout chipFood, chipTransport, chipShopping, chipRent, chipLeisure;
    private TextView tvFood, tvTransport, tvShopping, tvRent, tvLeisure;
    private MaterialButton btnScanAnomaly, btnSaveTransaction;
    
    // Anomaly banner UI
    private MaterialCardView cardAnomalyBanner;
    private LinearLayout layoutAnomalyBg;
    private TextView tvAnomalyTitle, tvAnomalyDesc;

    private int selectedCategoryId = 1; // 1: Food, 2: Transport, 3: Shopping, 4: Rent, 5: Leisure
    private boolean isExpense = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        btnToggleExpense = view.findViewById(R.id.btn_toggle_expense);
        btnToggleIncome = view.findViewById(R.id.btn_toggle_income);
        etAmount = view.findViewById(R.id.et_amount);
        etNote = view.findViewById(R.id.et_note);

        chipFood = view.findViewById(R.id.chip_cat_1);
        chipTransport = view.findViewById(R.id.chip_cat_2);
        chipShopping = view.findViewById(R.id.chip_cat_3);
        chipRent = view.findViewById(R.id.chip_cat_4);
        chipLeisure = view.findViewById(R.id.chip_cat_5);

        tvFood = view.findViewById(R.id.tv_cat_1);
        tvTransport = view.findViewById(R.id.tv_cat_2);
        tvShopping = view.findViewById(R.id.tv_cat_3);
        tvRent = view.findViewById(R.id.tv_cat_4);
        tvLeisure = view.findViewById(R.id.tv_cat_5);

        btnScanAnomaly = view.findViewById(R.id.btn_scan_anomaly);
        btnSaveTransaction = view.findViewById(R.id.btn_save_transaction);

        cardAnomalyBanner = view.findViewById(R.id.card_anomaly_banner);
        layoutAnomalyBg = view.findViewById(R.id.layout_anomaly_bg);
        tvAnomalyTitle = view.findViewById(R.id.tv_anomaly_title);
        tvAnomalyDesc = view.findViewById(R.id.tv_anomaly_desc);

        setupToggles();
        setupCategoryChips();
        setupActions();

        return view;
    }

    private void setupToggles() {
        btnToggleExpense.setOnClickListener(v -> {
            isExpense = true;
            btnToggleExpense.setBackgroundResource(R.drawable.bg_active_pill);
            btnToggleExpense.setTextColor(getResources().getColor(R.color.surface_white));
            btnToggleIncome.setBackground(null);
            btnToggleIncome.setTextColor(getResources().getColor(R.color.text_secondary));
        });

        btnToggleIncome.setOnClickListener(v -> {
            isExpense = false;
            btnToggleIncome.setBackgroundResource(R.drawable.bg_active_pill);
            btnToggleIncome.setTextColor(getResources().getColor(R.color.surface_white));
            btnToggleExpense.setBackground(null);
            btnToggleExpense.setTextColor(getResources().getColor(R.color.text_secondary));
        });
    }

    private void setupCategoryChips() {
        View.OnClickListener listener = v -> {
            // Reset all chips first
            chipFood.setBackgroundResource(R.drawable.bg_pill_chip);
            tvFood.setTextColor(getResources().getColor(R.color.text_secondary));
            tvFood.setTypeface(null, android.graphics.Typeface.NORMAL);

            chipTransport.setBackgroundResource(R.drawable.bg_pill_chip);
            tvTransport.setTextColor(getResources().getColor(R.color.text_secondary));
            tvTransport.setTypeface(null, android.graphics.Typeface.NORMAL);

            chipShopping.setBackgroundResource(R.drawable.bg_pill_chip);
            tvShopping.setTextColor(getResources().getColor(R.color.text_secondary));
            tvShopping.setTypeface(null, android.graphics.Typeface.NORMAL);

            chipRent.setBackgroundResource(R.drawable.bg_pill_chip);
            tvRent.setTextColor(getResources().getColor(R.color.text_secondary));
            tvRent.setTypeface(null, android.graphics.Typeface.NORMAL);

            chipLeisure.setBackgroundResource(R.drawable.bg_pill_chip);
            tvLeisure.setTextColor(getResources().getColor(R.color.text_secondary));
            tvLeisure.setTypeface(null, android.graphics.Typeface.NORMAL);

            int id = v.getId();
            if (id == R.id.chip_cat_1) {
                selectedCategoryId = 1;
                chipFood.setBackgroundResource(R.drawable.bg_pill_chip_active);
                tvFood.setTextColor(getResources().getColor(R.color.primary));
                tvFood.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (id == R.id.chip_cat_2) {
                selectedCategoryId = 2;
                chipTransport.setBackgroundResource(R.drawable.bg_pill_chip_active);
                tvTransport.setTextColor(getResources().getColor(R.color.primary));
                tvTransport.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (id == R.id.chip_cat_3) {
                selectedCategoryId = 3;
                chipShopping.setBackgroundResource(R.drawable.bg_pill_chip_active);
                tvShopping.setTextColor(getResources().getColor(R.color.primary));
                tvShopping.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (id == R.id.chip_cat_4) {
                selectedCategoryId = 4;
                chipRent.setBackgroundResource(R.drawable.bg_pill_chip_active);
                tvRent.setTextColor(getResources().getColor(R.color.primary));
                tvRent.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (id == R.id.chip_cat_5) {
                selectedCategoryId = 5;
                chipLeisure.setBackgroundResource(R.drawable.bg_pill_chip_active);
                tvLeisure.setTextColor(getResources().getColor(R.color.primary));
                tvLeisure.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        };

        chipFood.setOnClickListener(listener);
        chipTransport.setOnClickListener(listener);
        chipShopping.setOnClickListener(listener);
        chipRent.setOnClickListener(listener);
        chipLeisure.setOnClickListener(listener);
    }

    private void setupActions() {
        btnScanAnomaly.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);
            ApiClient.getApiService().checkAnomaly(1, selectedCategoryId, amount).enqueue(new Callback<AnomalyResponse>() {
                @Override
                public void onResponse(Call<AnomalyResponse> call, Response<AnomalyResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AnomalyResponse ar = response.body();
                        cardAnomalyBanner.setVisibility(View.VISIBLE);
                        tvAnomalyDesc.setText(ar.getMessage());

                        if (ar.isAnomalous()) {
                            tvAnomalyTitle.setText("PHÁT HIỆN CHI TIÊU BẤT THƯỜNG!");
                            tvAnomalyTitle.setTextColor(getResources().getColor(R.color.crimson_expense));
                            layoutAnomalyBg.setBackgroundColor(getResources().getColor(R.color.crimson_expense) & 0x15FFFFFF | 0x0A000000);
                        } else {
                            tvAnomalyTitle.setText("GIAO DỊCH AN TOÀN");
                            tvAnomalyTitle.setTextColor(getResources().getColor(R.color.emerald_income));
                            layoutAnomalyBg.setBackgroundColor(getResources().getColor(R.color.emerald_income) & 0x15FFFFFF | 0x0A000000);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AnomalyResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSaveTransaction.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);
            String note = etNote.getText().toString().trim();
            if (note.isEmpty()) note = "Giao dịch";

            Transaction t = new Transaction();
            t.setUserId(1);
            t.setWalletId(1);
            t.setCategoryId(selectedCategoryId);
            t.setAmount(amount);
            t.setNote(note);

            ApiClient.getApiService().createTransaction(t).enqueue(new Callback<Transaction>() {
                @Override
                public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show();
                        etAmount.setText("");
                        etNote.setText("");
                        cardAnomalyBanner.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<Transaction> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
