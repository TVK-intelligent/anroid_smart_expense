package com.example.smartexpense.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_filter, null);
        builder.setView(dialogView);

        TextInputEditText etStart = dialogView.findViewById(R.id.et_filter_start_date);
        TextInputEditText etEnd = dialogView.findViewById(R.id.et_filter_end_date);
        AutoCompleteTextView actWallet = dialogView.findViewById(R.id.act_filter_wallet);
        AutoCompleteTextView actCategory = dialogView.findViewById(R.id.act_filter_category);
        AutoCompleteTextView actType = dialogView.findViewById(R.id.act_filter_type);

        // Pre-fill existing date values
        etStart.setText(filterStartDate != null ? filterStartDate : "");
        etEnd.setText(filterEndDate != null ? filterEndDate : "");

        // Set up click listeners for MaterialDatePicker
        etStart.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Từ ngày")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                etStart.setText(sdf.format(new Date(selection)));
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER_FILTER_START");
        });

        etEnd.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Đến ngày")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                etEnd.setText(sdf.format(new Date(selection)));
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER_FILTER_END");
        });

        // Populate Wallet Dropdown
        List<String> walletNames = new ArrayList<>();
        walletNames.add("Tất cả ví");
        for (Wallet w : wallets) {
            walletNames.add(w.getName() != null ? w.getName() : ("Ví " + w.getWalletId()));
        }
        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, walletNames);
        actWallet.setAdapter(walletAdapter);
        if (filterWalletId == null) {
            actWallet.setText("Tất cả ví", false);
        } else {
            String selectedName = "Tất cả ví";
            for (Wallet w : wallets) {
                if (w.getWalletId() != null && w.getWalletId().equals(filterWalletId)) {
                    selectedName = w.getName() != null ? w.getName() : ("Ví " + w.getWalletId());
                    break;
                }
            }
            actWallet.setText(selectedName, false);
        }

        // Populate Category Dropdown
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Tất cả hạng mục");
        for (Category c : categories) {
            categoryNames.add(c.getName() != null ? c.getName() : ("Danh mục " + c.getCategoryId()));
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
        actCategory.setAdapter(categoryAdapter);
        if (filterCategoryId == null) {
            actCategory.setText("Tất cả hạng mục", false);
        } else {
            String selectedName = "Tất cả hạng mục";
            for (Category c : categories) {
                if (c.getCategoryId() != null && c.getCategoryId().equals(filterCategoryId)) {
                    selectedName = c.getName() != null ? c.getName() : ("Danh mục " + c.getCategoryId());
                    break;
                }
            }
            actCategory.setText(selectedName, false);
        }

        // Populate Transaction Type Dropdown
        String[] typeOptions = {"Tất cả giao dịch", "EXPENSE (Chi tiêu)", "INCOME (Thu nhập)"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, typeOptions);
        actType.setAdapter(typeAdapter);
        if (filterType == null) {
            actType.setText("Tất cả giao dịch", false);
        } else if ("EXPENSE".equalsIgnoreCase(filterType)) {
            actType.setText("EXPENSE (Chi tiêu)", false);
        } else if ("INCOME".equalsIgnoreCase(filterType)) {
            actType.setText("INCOME (Thu nhập)", false);
        }

        builder.setPositiveButton("Áp dụng", (dialog, which) -> {
            String start = etStart.getText() != null ? etStart.getText().toString().trim() : "";
            String end = etEnd.getText() != null ? etEnd.getText().toString().trim() : "";
            filterStartDate = start.isEmpty() ? null : start;
            filterEndDate = end.isEmpty() ? null : end;

            String selectedWallet = actWallet.getText() != null ? actWallet.getText().toString().trim() : "";
            if ("Tất cả ví".equals(selectedWallet) || selectedWallet.isEmpty()) {
                filterWalletId = null;
            } else {
                filterWalletId = null;
                for (Wallet w : wallets) {
                    String name = w.getName() != null ? w.getName() : ("Ví " + w.getWalletId());
                    if (name.equalsIgnoreCase(selectedWallet)) {
                        filterWalletId = w.getWalletId();
                        break;
                    }
                }
            }

            String selectedCategory = actCategory.getText() != null ? actCategory.getText().toString().trim() : "";
            if ("Tất cả hạng mục".equals(selectedCategory) || selectedCategory.isEmpty()) {
                filterCategoryId = null;
            } else {
                filterCategoryId = null;
                for (Category c : categories) {
                    String name = c.getName() != null ? c.getName() : ("Danh mục " + c.getCategoryId());
                    if (name.equalsIgnoreCase(selectedCategory)) {
                        filterCategoryId = c.getCategoryId();
                        break;
                    }
                }
            }

            String selectedType = actType.getText() != null ? actType.getText().toString().trim() : "";
            if ("EXPENSE (Chi tiêu)".equalsIgnoreCase(selectedType)) {
                filterType = "EXPENSE";
            } else if ("INCOME (Thu nhập)".equalsIgnoreCase(selectedType)) {
                filterType = "INCOME";
            } else {
                filterType = null;
            }

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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_edit, null);
        builder.setView(dialogView);

        AutoCompleteTextView actType = dialogView.findViewById(R.id.act_edit_type);
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_edit_amount);
        AutoCompleteTextView actWallet = dialogView.findViewById(R.id.act_edit_wallet);
        AutoCompleteTextView actCategory = dialogView.findViewById(R.id.act_edit_category);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_edit_date);
        TextInputEditText etNote = dialogView.findViewById(R.id.et_edit_note);

        // Pre-fill amount & note
        etAmount.setText(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : "");
        etNote.setText(transaction.getNote() != null ? transaction.getNote() : "");

        // Pre-fill and handle Date picker
        etDate.setText(transaction.getTransactionDate() != null ? transaction.getTransactionDate() : "");
        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày giao dịch")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                etDate.setText(sdf.format(new Date(selection)));
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER_EDIT");
        });

        // Set up Transaction Type options and preselect
        String[] editTypes = {"EXPENSE (Chi tiêu)", "INCOME (Thu nhập)"};
        ArrayAdapter<String> editTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, editTypes);
        actType.setAdapter(editTypeAdapter);
        if ("INCOME".equalsIgnoreCase(transaction.getType())) {
            actType.setText("INCOME (Thu nhập)", false);
        } else {
            actType.setText("EXPENSE (Chi tiêu)", false);
        }

        // Set up Wallet options and preselect
        ArrayAdapter<Wallet> editWalletAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, wallets);
        actWallet.setAdapter(editWalletAdapter);
        if (transaction.getWalletId() != null) {
            Wallet matchedWallet = null;
            for (Wallet w : wallets) {
                if (w.getWalletId() != null && w.getWalletId().equals(transaction.getWalletId())) {
                    matchedWallet = w;
                    break;
                }
            }
            if (matchedWallet != null) {
                actWallet.setText(matchedWallet.getName(), false);
            }
        }

        // Set up Category options and preselect
        ArrayAdapter<Category> editCategoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(editCategoryAdapter);
        if (transaction.getCategoryId() != null) {
            Category matchedCategory = null;
            for (Category c : categories) {
                if (c.getCategoryId() != null && c.getCategoryId().equals(transaction.getCategoryId())) {
                    matchedCategory = c;
                    break;
                }
            }
            if (matchedCategory != null) {
                actCategory.setText(matchedCategory.getName(), false);
            }
        }

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

            // Find selected wallet
            String walletName = actWallet.getText() != null ? actWallet.getText().toString().trim() : "";
            Wallet selectedWallet = null;
            for (Wallet w : wallets) {
                String wName = w.getName() != null ? w.getName() : ("Ví " + w.getWalletId());
                if (wName.equalsIgnoreCase(walletName)) {
                    selectedWallet = w;
                    break;
                }
            }

            // Find selected category
            String catName = actCategory.getText() != null ? actCategory.getText().toString().trim() : "";
            Category selectedCategory = null;
            for (Category c : categories) {
                String cName = c.getName() != null ? c.getName() : ("Danh mục " + c.getCategoryId());
                if (cName.equalsIgnoreCase(catName)) {
                    selectedCategory = c;
                    break;
                }
            }

            if (selectedWallet == null || selectedWallet.getWalletId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCategory == null || selectedCategory.getCategoryId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction payload = new Transaction();
            String selectedType = actType.getText() != null ? actType.getText().toString().trim() : "EXPENSE";
            String typeVal = "INCOME (Thu nhập)".equalsIgnoreCase(selectedType) ? "INCOME" : "EXPENSE";

            payload.setType(typeVal);
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

        android.app.AlertDialog alertDialog = builder.create();

        // Listen for type changes to morph Save button color dynamically
        actType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = parent.getItemAtPosition(position).toString();
            String typeVal = "INCOME (Thu nhập)".equalsIgnoreCase(selectedType) ? "INCOME" : "EXPENSE";
            updateDialogButtonColor(alertDialog, typeVal);
        });

        // Morph Save button color initially based on current transaction type
        alertDialog.setOnShowListener(dialogInterface -> {
            String initialType = transaction.getType();
            updateDialogButtonColor(alertDialog, initialType);
        });

        alertDialog.show();
    }

    private void updateDialogButtonColor(android.app.AlertDialog dialog, String type) {
        android.widget.Button btn = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        if (btn != null) {
            int color = "INCOME".equalsIgnoreCase(type) ? Color.parseColor("#27C38A") : Color.parseColor("#BA1A1A");
            btn.setBackgroundTintList(ColorStateList.valueOf(color));
            btn.setTextColor(Color.WHITE);
        }
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
