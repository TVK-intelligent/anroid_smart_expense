package com.example.smartexpense.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.adapters.TransactionAdapter;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.Category;
import com.example.smartexpense.models.Transaction;
import com.example.smartexpense.models.Wallet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private ProgressBar progress;
    private TextView tvState;
    private TextView btnFilter;

    private List<Wallet> wallets = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    // active filter
    private String filterStartDate = null; // yyyy-MM-dd
    private String filterEndDate = null;   // yyyy-MM-dd
    private Integer filterWalletId = null;
    private Integer filterCategoryId = null;
    private String filterType = null; // INCOME/EXPENSE

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        rvTransactions = view.findViewById(R.id.rv_transactions);
        progress = view.findViewById(R.id.progress_transactions);
        tvState = view.findViewById(R.id.tv_transactions_state);
        btnFilter = view.findViewById(R.id.btn_filter_transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(transactionList, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                showEditTransactionDialog(transaction);
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                showTransactionActionsDialog(transaction);
            }
        });
        rvTransactions.setAdapter(adapter);

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }

        loadReferenceData();
        loadTransactions();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh when returning from AddTransaction
        loadTransactions();
    }

    private int getUserId() {
        return requireActivity()
                .getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
                .getInt("user_id", 1);
    }

    private void loadReferenceData() {
        int userId = getUserId();
        ApiClient.getApiService().getWallets(userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wallets.clear();
                    wallets.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });

        ApiClient.getApiService().getCategories(userId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void setLoading(boolean loading) {
        if (progress == null || tvState == null || rvTransactions == null) return;
        if (loading) {
            progress.setVisibility(View.VISIBLE);
            tvState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
        }
    }

    private void setState(String message) {
        if (tvState == null || rvTransactions == null) return;
        tvState.setText(message);
        tvState.setVisibility(View.VISIBLE);
        rvTransactions.setVisibility(View.GONE);
    }

    private void updateListVisibility() {
        if (tvState == null || rvTransactions == null) return;
        if (transactionList.isEmpty()) {
            setState("Chưa có giao dịch nào");
        } else {
            tvState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void loadTransactions() {
        int userId = getUserId();
        setLoading(true);

        Call<List<Transaction>> call;
        boolean hasFilter = filterStartDate != null || filterEndDate != null || filterWalletId != null || filterCategoryId != null || filterType != null;
        if (hasFilter) {
            call = ApiClient.getApiService().filterTransactions(userId, filterStartDate, filterEndDate, filterWalletId, filterCategoryId, filterType);
        } else {
            call = ApiClient.getApiService().getRecentTransactions(userId, 50);
        }

        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    transactionList.clear();
                    transactionList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateListVisibility();
                } else {
                    setState("Không tải được giao dịch");
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                setLoading(false);
                setState("Lỗi mạng khi tải giao dịch");
            }
        });
    }

    private void showFilterDialog() {
        if (getContext() == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Lọc giao dịch");

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        // Date range (simple text input yyyy-MM-dd)
        com.google.android.material.textfield.TextInputLayout tilStart = new com.google.android.material.textfield.TextInputLayout(getContext());
        tilStart.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilStart.setHint("Từ ngày (yyyy-MM-dd)");
        com.google.android.material.textfield.TextInputEditText etStart = new com.google.android.material.textfield.TextInputEditText(getContext());
        etStart.setText(filterStartDate != null ? filterStartDate : "");
        tilStart.addView(etStart);
        container.addView(tilStart);

        View space1 = new View(getContext());
        space1.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space1);

        com.google.android.material.textfield.TextInputLayout tilEnd = new com.google.android.material.textfield.TextInputLayout(getContext());
        tilEnd.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilEnd.setHint("Đến ngày (yyyy-MM-dd)");
        com.google.android.material.textfield.TextInputEditText etEnd = new com.google.android.material.textfield.TextInputEditText(getContext());
        etEnd.setText(filterEndDate != null ? filterEndDate : "");
        tilEnd.addView(etEnd);
        container.addView(tilEnd);

        View space2 = new View(getContext());
        space2.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space2);

        // wallet spinner
        Spinner spWallet = new Spinner(getContext());
        List<String> walletOptions = new ArrayList<>();
        walletOptions.add("ALL");
        for (Wallet w : wallets) {
            walletOptions.add(w.getName() != null ? w.getName() : ("Wallet " + w.getWalletId()));
        }
        android.widget.ArrayAdapter<String> walletAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, walletOptions);
        spWallet.setAdapter(walletAdapter);
        if (filterWalletId != null) {
            int idx = 0;
            for (int i = 0; i < wallets.size(); i++) {
                if (wallets.get(i).getWalletId() != null && wallets.get(i).getWalletId().equals(filterWalletId)) {
                    idx = i + 1;
                    break;
                }
            }
            spWallet.setSelection(idx);
        }
        container.addView(spWallet);

        View space3 = new View(getContext());
        space3.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space3);

        // category spinner
        Spinner spCategory = new Spinner(getContext());
        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add("ALL");
        for (Category c : categories) {
            categoryOptions.add(c.getName() != null ? c.getName() : ("Category " + c.getCategoryId()));
        }
        android.widget.ArrayAdapter<String> categoryAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions);
        spCategory.setAdapter(categoryAdapter);
        if (filterCategoryId != null) {
            int idx = 0;
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getCategoryId() != null && categories.get(i).getCategoryId().equals(filterCategoryId)) {
                    idx = i + 1;
                    break;
                }
            }
            spCategory.setSelection(idx);
        }
        container.addView(spCategory);

        View space4 = new View(getContext());
        space4.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space4);

        // type spinner
        Spinner spType = new Spinner(getContext());
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("ALL");
        typeOptions.add("EXPENSE");
        typeOptions.add("INCOME");
        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, typeOptions);
        spType.setAdapter(typeAdapter);
        if (filterType != null) {
            spType.setSelection(typeOptions.indexOf(filterType));
        }
        container.addView(spType);

        builder.setView(container);

        builder.setPositiveButton("Áp dụng", (dialog, which) -> {
            String start = etStart.getText() != null ? etStart.getText().toString().trim() : "";
            String end = etEnd.getText() != null ? etEnd.getText().toString().trim() : "";
            filterStartDate = start.isEmpty() ? null : start;
            filterEndDate = end.isEmpty() ? null : end;

            String selectedWallet = (String) spWallet.getSelectedItem();
            if ("ALL".equals(selectedWallet)) {
                filterWalletId = null;
            } else {
                int sel = spWallet.getSelectedItemPosition();
                filterWalletId = sel > 0 ? wallets.get(sel - 1).getWalletId() : null;
            }

            String selectedCategory = (String) spCategory.getSelectedItem();
            if ("ALL".equals(selectedCategory)) {
                filterCategoryId = null;
            } else {
                int sel = spCategory.getSelectedItemPosition();
                filterCategoryId = sel > 0 ? categories.get(sel - 1).getCategoryId() : null;
            }

            String selectedType = (String) spType.getSelectedItem();
            filterType = "ALL".equals(selectedType) ? null : selectedType;

            loadTransactions();
        });

        builder.setNegativeButton("Clear", (dialog, which) -> {
            filterStartDate = null;
            filterEndDate = null;
            filterWalletId = null;
            filterCategoryId = null;
            filterType = null;
            loadTransactions();
        });

        builder.show();
    }

    private void showTransactionActionsDialog(Transaction transaction) {
        if (getContext() == null || transaction == null) return;
        String[] items = new String[]{"Sửa giao dịch", "Xóa giao dịch"};
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Giao dịch")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showEditTransactionDialog(transaction);
                    } else {
                        confirmDeleteTransaction(transaction);
                    }
                })
                .show();
    }

    private void showEditTransactionDialog(Transaction transaction) {
        if (getContext() == null || transaction == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Sửa giao dịch");

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        // type
        Spinner spType = new Spinner(getContext());
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("EXPENSE");
        typeOptions.add("INCOME");
        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, typeOptions);
        spType.setAdapter(typeAdapter);
        if (transaction.getType() != null) {
            int idx = typeOptions.indexOf(transaction.getType().toUpperCase(Locale.US));
            if (idx >= 0) spType.setSelection(idx);
        }
        container.addView(spType);

        View space0 = new View(getContext());
        space0.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space0);

        // wallet
        Spinner spWallet = new Spinner(getContext());
        List<Wallet> walletOptions = new ArrayList<>(wallets);
        android.widget.ArrayAdapter<Wallet> walletAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, walletOptions);
        spWallet.setAdapter(walletAdapter);
        if (transaction.getWalletId() != null) {
            int idx = 0;
            for (int i = 0; i < walletOptions.size(); i++) {
                if (walletOptions.get(i).getWalletId() != null && walletOptions.get(i).getWalletId().equals(transaction.getWalletId())) {
                    idx = i;
                    break;
                }
            }
            spWallet.setSelection(idx);
        }
        container.addView(spWallet);

        View spaceW = new View(getContext());
        spaceW.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(spaceW);

        // category
        Spinner spCategory = new Spinner(getContext());
        List<Category> categoryOptions = new ArrayList<>(categories);
        android.widget.ArrayAdapter<Category> categoryAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions);
        spCategory.setAdapter(categoryAdapter);
        if (transaction.getCategoryId() != null) {
            int idx = 0;
            for (int i = 0; i < categoryOptions.size(); i++) {
                if (categoryOptions.get(i).getCategoryId() != null && categoryOptions.get(i).getCategoryId().equals(transaction.getCategoryId())) {
                    idx = i;
                    break;
                }
            }
            spCategory.setSelection(idx);
        }
        container.addView(spCategory);

        View spaceC = new View(getContext());
        spaceC.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(spaceC);

        // date (simple input)
        com.google.android.material.textfield.TextInputLayout tilDate = new com.google.android.material.textfield.TextInputLayout(getContext());
        tilDate.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilDate.setHint("Ngày (yyyy-MM-dd)");
        com.google.android.material.textfield.TextInputEditText etDate = new com.google.android.material.textfield.TextInputEditText(getContext());
        etDate.setText(transaction.getTransactionDate() != null ? transaction.getTransactionDate() : "");
        tilDate.addView(etDate);
        container.addView(tilDate);

        View spaceD = new View(getContext());
        spaceD.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(spaceD);

        com.google.android.material.textfield.TextInputLayout tilAmount = new com.google.android.material.textfield.TextInputLayout(getContext());
        tilAmount.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilAmount.setHint("Số tiền");
        com.google.android.material.textfield.TextInputEditText etAmount = new com.google.android.material.textfield.TextInputEditText(getContext());
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etAmount.setText(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : "");
        tilAmount.addView(etAmount);
        container.addView(tilAmount);

        View space1 = new View(getContext());
        space1.setLayoutParams(new LinearLayout.LayoutParams(1, (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space1);

        com.google.android.material.textfield.TextInputLayout tilNote = new com.google.android.material.textfield.TextInputLayout(getContext());
        tilNote.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilNote.setHint("Ghi chú");
        com.google.android.material.textfield.TextInputEditText etNote = new com.google.android.material.textfield.TextInputEditText(getContext());
        etNote.setText(transaction.getNote() != null ? transaction.getNote() : "");
        tilNote.addView(etNote);
        container.addView(tilNote);

        builder.setView(container);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            java.math.BigDecimal amount;
            try {
                amount = new java.math.BigDecimal(amountStr);
            } catch (Exception ignored) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                Toast.makeText(getContext(), "Số tiền phải > 0", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction payload = new Transaction();
            Wallet selectedWallet = (Wallet) spWallet.getSelectedItem();
            Category selectedCategory = (Category) spCategory.getSelectedItem();
            if (selectedWallet == null || selectedWallet.getWalletId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCategory == null || selectedCategory.getCategoryId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedType = (String) spType.getSelectedItem();
            payload.setType(selectedType);
            payload.setWalletId(selectedWallet.getWalletId());
            payload.setCategoryId(selectedCategory.getCategoryId());
            payload.setAmount(amount);

            String dateStr = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            if (dateStr.isEmpty()) {
                dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            }
            payload.setTransactionDate(dateStr);
            payload.setNote(etNote.getText() != null ? etNote.getText().toString().trim() : "");

            ApiClient.getApiService().updateTransaction(transaction.getTransactionId(), getUserId(), payload).enqueue(new Callback<Transaction>() {
                @Override
                public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
                        loadTransactions();
                    } else {
                        String msg = "Không cập nhật được giao dịch";
                        try {
                            if (response.errorBody() != null) msg = response.errorBody().string();
                        } catch (Exception ignored) {}
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Transaction> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng khi cập nhật", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        if (getContext() == null) return;
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn chắc chắn muốn xóa giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTransaction(transaction))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        if (getContext() == null || transaction == null) return;
        ApiClient.getApiService().deleteTransaction(transaction.getTransactionId(), getUserId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    loadTransactions();
                } else {
                    String msg = "Không xóa được giao dịch";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
