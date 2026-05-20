package com.example.smartexpense.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.AnomalyResponse;
import com.example.smartexpense.models.Category;
import com.example.smartexpense.models.Transaction;
import com.example.smartexpense.models.Wallet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionFragment extends Fragment {

    private TextView btnToggleExpense, btnToggleIncome;
    private EditText etAmount, etNote;
    private Spinner spWallet, spCategory;
    private TextView tvDate;
    private MaterialButton btnScanAnomaly, btnSaveTransaction;

    private MaterialCardView cardAnomalyBanner;
    private LinearLayout layoutAnomalyBg;
    private TextView tvAnomalyTitle, tvAnomalyDesc;

    private boolean isExpense = true;
    private final List<Wallet> wallets = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private String selectedDate = dateFormat.format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        btnToggleExpense = view.findViewById(R.id.btn_toggle_expense);
        btnToggleIncome = view.findViewById(R.id.btn_toggle_income);
        etAmount = view.findViewById(R.id.et_amount);
        etNote = view.findViewById(R.id.et_note);
        spWallet = view.findViewById(R.id.sp_wallet);
        spCategory = view.findViewById(R.id.sp_category);
        tvDate = view.findViewById(R.id.tv_transaction_date);

        btnScanAnomaly = view.findViewById(R.id.btn_scan_anomaly);
        btnSaveTransaction = view.findViewById(R.id.btn_save_transaction);

        cardAnomalyBanner = view.findViewById(R.id.card_anomaly_banner);
        layoutAnomalyBg = view.findViewById(R.id.layout_anomaly_bg);
        tvAnomalyTitle = view.findViewById(R.id.tv_anomaly_title);
        tvAnomalyDesc = view.findViewById(R.id.tv_anomaly_desc);

        setupToggles();
        setupDatePicker();
        setupActions();
        loadReferenceData();

        return view;
    }

    private int getUserId() {
        return requireActivity()
                .getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
                .getInt("user_id", 1);
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

    private void setupDatePicker() {
        if (tvDate == null) return;
        tvDate.setText(selectedDate);
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            try {
                Date parsed = dateFormat.parse(selectedDate);
                if (parsed != null) cal.setTime(parsed);
            } catch (Exception ignored) {}

            new android.app.DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);
                        selectedDate = dateFormat.format(picked.getTime());
                        tvDate.setText(selectedDate);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void loadReferenceData() {
        int userId = getUserId();

        ApiClient.getApiService().getWallets(userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    wallets.clear();
                    wallets.addAll(response.body());
                    android.widget.ArrayAdapter<Wallet> adapter = new android.widget.ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            wallets
                    );
                    spWallet.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });

        ApiClient.getApiService().getCategories(userId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    android.widget.ArrayAdapter<Category> adapter = new android.widget.ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            categories
                    );
                    spCategory.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void setupActions() {
        btnScanAnomaly.setOnClickListener(v -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (Exception ignored) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getContext(), "Số tiền phải > 0", Toast.LENGTH_SHORT).show();
                return;
            }

            Category category = (Category) (spCategory != null ? spCategory.getSelectedItem() : null);
            if (category == null || category.getCategoryId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiClient.getApiService().checkAnomaly(getUserId(), category.getCategoryId(), amount).enqueue(new Callback<AnomalyResponse>() {
                @Override
                public void onResponse(Call<AnomalyResponse> call, Response<AnomalyResponse> response) {
                    if (!isAdded()) return;
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
                    } else {
                        Toast.makeText(getContext(), "Không quét được bất thường", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AnomalyResponse> call, Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSaveTransaction.setOnClickListener(v -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (Exception ignored) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getContext(), "Số tiền phải > 0", Toast.LENGTH_SHORT).show();
                return;
            }

            Wallet wallet = (Wallet) (spWallet != null ? spWallet.getSelectedItem() : null);
            if (wallet == null || wallet.getWalletId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
                return;
            }

            Category category = (Category) (spCategory != null ? spCategory.getSelectedItem() : null);
            if (category == null || category.getCategoryId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            String desiredType = isExpense ? "EXPENSE" : "INCOME";
            if (category.getType() != null && !desiredType.equalsIgnoreCase(category.getType())) {
                Toast.makeText(getContext(), "Danh mục không khớp loại thu/chi", Toast.LENGTH_SHORT).show();
                return;
            }

            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
            if (note.isEmpty()) note = "Giao dịch";

            Transaction t = new Transaction();
            t.setUserId(getUserId());
            t.setWalletId(wallet.getWalletId());
            t.setCategoryId(category.getCategoryId());
            t.setType(desiredType);
            t.setAmount(amount);
            t.setTransactionDate(selectedDate);
            t.setNote(note);

            ApiClient.getApiService().createTransaction(t).enqueue(new Callback<Transaction>() {
                @Override
                public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    } else {
                        String msg = "Không lưu được giao dịch";
                        try {
                            if (response.errorBody() != null) msg = response.errorBody().string();
                        } catch (Exception ignored) {}
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Transaction> call, Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

