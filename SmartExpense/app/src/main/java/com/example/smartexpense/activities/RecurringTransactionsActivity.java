package com.example.smartexpense.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.api.CategoryCache;
import com.example.smartexpense.models.Category;
import com.example.smartexpense.models.RecurringTransaction;
import com.example.smartexpense.models.Wallet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecurringTransactionsActivity extends BaseActivity {

    private TextView btnToggleExpense, btnToggleIncome;
    private boolean isExpenseMode = true;
    private List<RecurringTransaction> allRecurringTransactions = new ArrayList<>();
    private ImageView btnBack;
    private LinearLayout layoutRecurringContainer;
    private TextView tvEmptyState;
    private MaterialButton btnAddRecurring;

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private int userId = 1;
    private int defaultWalletId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transactions);

        userId = getSharedPreferences("smart_expense_prefs", MODE_PRIVATE).getInt("user_id", 1);

        btnBack = findViewById(R.id.btn_back);
        layoutRecurringContainer = findViewById(R.id.layout_recurring_container);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnAddRecurring = findViewById(R.id.btn_add_recurring);
        btnToggleExpense = findViewById(R.id.btn_toggle_expense);
        btnToggleIncome = findViewById(R.id.btn_toggle_income);

        btnBack.setOnClickListener(v -> finish());
        btnAddRecurring.setOnClickListener(v -> showAddRecurringDialog(null));
        setupToggles();

        loadWallets();
        triggerAndLoadRecurring();
    }

    private void triggerAndLoadRecurring() {
        ApiClient.getApiService().triggerRecurringTransactions().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                loadRecurringTransactions();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadRecurringTransactions();
            }
        });
    }

    private void setupToggles() {
        btnToggleExpense.setOnClickListener(v -> {
            isExpenseMode = true;
            btnToggleExpense.setBackgroundResource(R.drawable.bg_toggle_expense_active);
            btnToggleExpense.setTextColor(getResources().getColor(R.color.surface_white));
            btnToggleIncome.setBackground(null);
            btnToggleIncome.setTextColor(getResources().getColor(R.color.text_secondary));
            filterAndDisplay();
        });

        btnToggleIncome.setOnClickListener(v -> {
            isExpenseMode = false;
            btnToggleIncome.setBackgroundResource(R.drawable.bg_toggle_income_active);
            btnToggleIncome.setTextColor(getResources().getColor(R.color.surface_white));
            btnToggleExpense.setBackground(null);
            btnToggleExpense.setTextColor(getResources().getColor(R.color.text_secondary));
            filterAndDisplay();
        });
    }

    private void loadWallets() {
        ApiClient.getApiService().getWallets(userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    defaultWalletId = response.body().get(0).getWalletId();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });
    }

    private void loadRecurringTransactions() {
        ApiClient.getApiService().getRecurringTransactions(userId).enqueue(new Callback<List<RecurringTransaction>>() {
            @Override
            public void onResponse(Call<List<RecurringTransaction>> call, Response<List<RecurringTransaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRecurringTransactions = response.body();
                    filterAndDisplay();
                } else {
                    allRecurringTransactions.clear();
                    filterAndDisplay();
                }
            }

            @Override
            public void onFailure(Call<List<RecurringTransaction>> call, Throwable t) {
                Toast.makeText(RecurringTransactionsActivity.this, "Lỗi kết nối tới server!", Toast.LENGTH_SHORT).show();
                allRecurringTransactions.clear();
                filterAndDisplay();
            }
        });
    }

    private void filterAndDisplay() {
        List<RecurringTransaction> filtered = new ArrayList<>();
        for (RecurringTransaction rt : allRecurringTransactions) {
            Category matchedCat = null;
            List<Category> categoriesList = CategoryCache.getCategories();
            if (categoriesList != null) {
                for (Category c : categoriesList) {
                    if (c.getCategoryId() != null && c.getCategoryId().equals(rt.getCategoryId())) {
                        matchedCat = c;
                        break;
                    }
                }
            }
            boolean isIncome = matchedCat != null && "INCOME".equalsIgnoreCase(matchedCat.getType());
            if (isExpenseMode && !isIncome) {
                filtered.add(rt);
            } else if (!isExpenseMode && isIncome) {
                filtered.add(rt);
            }
        }
        updateUI(filtered);
    }

    private void updateUI(List<RecurringTransaction> list) {
        layoutRecurringContainer.removeAllViews();

        if (list == null || list.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);

        for (RecurringTransaction rt : list) {
            MaterialCardView card = new MaterialCardView(this);
            card.setRadius(16 * getResources().getDisplayMetrics().density);
            card.setCardElevation(2 * getResources().getDisplayMetrics().density);
            card.setCardBackgroundColor(getResources().getColor(R.color.surface_white));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
            card.setLayoutParams(cardParams);

            LinearLayout rootLayout = new LinearLayout(this);
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            rootLayout.setPadding(padding, padding, padding, padding);

            // Resolve Category from cache
            Category matchedCat = null;
            List<Category> categoriesList = CategoryCache.getCategories();
            if (categoriesList != null) {
                for (Category c : categoriesList) {
                    if (c.getCategoryId() != null && c.getCategoryId().equals(rt.getCategoryId())) {
                        matchedCat = c;
                        break;
                    }
                }
            }

            boolean isIncome = matchedCat != null && "INCOME".equalsIgnoreCase(matchedCat.getType());
            String catName = matchedCat != null ? matchedCat.getName() : "Định kỳ";

            // Resolve Category Icon Resource ID
            String iconName = matchedCat != null ? matchedCat.getIcon() : "other";
            int iconRes = CategoryCache.getIconResource(iconName);

            // Row 1: Icon, Note and Amount
            RelativeLayout row1 = new RelativeLayout(this);

            ImageView ivIcon = new ImageView(this);
            ivIcon.setId(View.generateViewId());
            ivIcon.setImageResource(iconRes);
            ivIcon.setColorFilter(getResources().getColor(isIncome ? R.color.emerald_income : R.color.accent_blue));
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
                    (int) (24 * getResources().getDisplayMetrics().density),
                    (int) (24 * getResources().getDisplayMetrics().density)
            );
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
            ivIcon.setLayoutParams(iconParams);
            row1.addView(ivIcon);

            TextView tvAmount = new TextView(this);
            tvAmount.setId(View.generateViewId());
            if (isIncome) {
                tvAmount.setText("+" + formatter.format(rt.getAmount()) + "đ");
                tvAmount.setTextColor(getResources().getColor(R.color.emerald_income));
            } else {
                tvAmount.setText("-" + formatter.format(rt.getAmount()) + "đ");
                tvAmount.setTextColor(getResources().getColor(R.color.crimson_expense));
            }
            tvAmount.setTextSize(16);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams amtParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            amtParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            amtParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvAmount.setLayoutParams(amtParams);

            TextView tvNote = new TextView(this);
            tvNote.setId(View.generateViewId());
            String displayTitle = catName + (rt.getNote() != null && !rt.getNote().isEmpty() ? " - " + rt.getNote() : "");
            tvNote.setText(displayTitle);
            tvNote.setTextColor(getResources().getColor(R.color.text_primary));
            tvNote.setTextSize(14);
            tvNote.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams noteParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.addRule(RelativeLayout.RIGHT_OF, ivIcon.getId());
            noteParams.addRule(RelativeLayout.LEFT_OF, tvAmount.getId());
            noteParams.addRule(RelativeLayout.CENTER_VERTICAL);
            noteParams.leftMargin = (int) (8 * getResources().getDisplayMetrics().density);
            noteParams.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
            tvNote.setLayoutParams(noteParams);

            row1.addView(tvAmount);
            row1.addView(tvNote);

            rootLayout.addView(row1);

            TextView tvFreq = new TextView(this);
            String freqText = "Tần suất: " + rt.getFrequency();
            tvFreq.setText(freqText);
            tvFreq.setTextColor(getResources().getColor(R.color.text_secondary));
            tvFreq.setTextSize(12);
            tvFreq.setPadding(0, 4, 0, 2);
            rootLayout.addView(tvFreq);

            TextView tvDue = new TextView(this);
            String dueText = "Ngày đến hạn tiếp theo: " + rt.getNextDueDate();
            tvDue.setText(dueText);
            tvDue.setTextColor(getResources().getColor(R.color.accent_blue));
            tvDue.setTextSize(12);
            tvDue.setPadding(0, 0, 0, 8);
            rootLayout.addView(tvDue);

            // Divider
            View divider = new View(this);
            divider.setBackgroundColor(getResources().getColor(R.color.progress_track));
            LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (1 * getResources().getDisplayMetrics().density)
            );
            divParams.setMargins(0, 4, 0, 8);
            divider.setLayoutParams(divParams);
            rootLayout.addView(divider);

            // Row 3: Action Buttons (Toggle state & Edit & Delete)
            RelativeLayout row3 = new RelativeLayout(this);
            float density = getResources().getDisplayMetrics().density;

            MaterialButton btnToggle = new MaterialButton(this);
            boolean isActive = rt.getIsActive() != null ? rt.getIsActive() : true;
            btnToggle.setText(isActive ? "Đang hoạt động" : "Đã tạm dừng");
            btnToggle.setTextColor(getResources().getColor(R.color.surface_white));
            btnToggle.setTextSize(11);
            btnToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    isActive ? getResources().getColor(R.color.accent_blue) : getResources().getColor(R.color.text_secondary)
            ));
            btnToggle.setCornerRadius((int) (12 * density));
            RelativeLayout.LayoutParams toggleParams = new RelativeLayout.LayoutParams(
                    (int) (130 * density),
                    (int) (36 * density)
            );
            toggleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            btnToggle.setLayoutParams(toggleParams);

            final Integer id = rt.getRecurringId();
            final boolean finalIsActive = isActive;
            btnToggle.setOnClickListener(v -> toggleStatus(id, !finalIsActive));

            // Edit button (pencil icon)
            ImageView btnEdit = new ImageView(this);
            btnEdit.setId(View.generateViewId());
            btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
            int paddingEdit = (int) (8 * density);
            btnEdit.setPadding(paddingEdit, paddingEdit, paddingEdit, paddingEdit);
            
            // Add ripple background effect
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            btnEdit.setBackgroundResource(outValue.resourceId);
            btnEdit.setClickable(true);
            btnEdit.setFocusable(true);
            btnEdit.setColorFilter(getResources().getColor(R.color.text_secondary));

            RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(
                    (int) (36 * density),
                    (int) (36 * density)
            );
            editParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            editParams.addRule(RelativeLayout.CENTER_VERTICAL);
            btnEdit.setLayoutParams(editParams);
            btnEdit.setOnClickListener(v -> showAddRecurringDialog(rt));

            // Premium vector delete icon button
            ImageView btnDelete = new ImageView(this);
            btnDelete.setImageResource(R.drawable.ic_delete);
            int paddingDelete = (int) (8 * density);
            btnDelete.setPadding(paddingDelete, paddingDelete, paddingDelete, paddingDelete);
            btnDelete.setBackgroundResource(outValue.resourceId);
            btnDelete.setClickable(true);
            btnDelete.setFocusable(true);

            RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(
                    (int) (36 * density),
                    (int) (36 * density)
            );
            deleteParams.addRule(RelativeLayout.LEFT_OF, btnEdit.getId());
            deleteParams.addRule(RelativeLayout.CENTER_VERTICAL);
            btnDelete.setLayoutParams(deleteParams);

            btnDelete.setOnClickListener(v -> deleteRecurring(id));

            row3.addView(btnToggle);
            row3.addView(btnEdit);
            row3.addView(btnDelete);

            rootLayout.addView(row3);

            card.addView(rootLayout);
            layoutRecurringContainer.addView(card);
        }
    }

    private void toggleStatus(Integer id, boolean newStatus) {
        ApiClient.getApiService().updateRecurringTransactionStatus(id, userId, newStatus).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RecurringTransactionsActivity.this, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                    loadRecurringTransactions();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RecurringTransactionsActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRecurring(Integer id) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa lịch giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteRecurringTransaction(id, userId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(RecurringTransactionsActivity.this, "Đã xóa lịch giao dịch!", Toast.LENGTH_SHORT).show();
                                loadRecurringTransactions();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RecurringTransactionsActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAddRecurringDialog(final RecurringTransaction rtToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recurring_transaction, null);
        builder.setView(dialogView);

        AutoCompleteTextView actCategory = dialogView.findViewById(R.id.act_dialog_category);
        AutoCompleteTextView actWallet = dialogView.findViewById(R.id.act_dialog_wallet);
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_dialog_amount);
        AutoCompleteTextView actFrequency = dialogView.findViewById(R.id.act_dialog_frequency);
        TextInputEditText etNote = dialogView.findViewById(R.id.et_dialog_note);
        TextInputEditText etDueDate = dialogView.findViewById(R.id.et_dialog_due_date);
        TextInputLayout tilCategory = dialogView.findViewById(R.id.til_dialog_category);

        if (tilCategory != null) {
            tilCategory.setHint(isExpenseMode ? "Danh mục chi tiêu" : "Danh mục thu nhập");
        }

        final java.util.Calendar cal = java.util.Calendar.getInstance();
        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        etDueDate.setText(sdf.format(cal.getTime()));

        etDueDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        java.util.Calendar picked = java.util.Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);
                        etDueDate.setText(sdf.format(picked.getTime()));
                    },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Load Wallets
        final List<Wallet> walletList = new ArrayList<>();
        final List<String> walletDisplayList = new ArrayList<>();
        ApiClient.getApiService().getWallets(userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    walletList.addAll(response.body());
                    for (Wallet w : walletList) {
                        walletDisplayList.add(w.getName() + " (" + formatter.format(w.getBalance()) + "đ)");
                    }
                    ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(
                            RecurringTransactionsActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            walletDisplayList
                    );
                    actWallet.setAdapter(walletAdapter);

                    // Preselect wallet
                    if (rtToEdit != null) {
                        for (int i = 0; i < walletList.size(); i++) {
                            if (walletList.get(i).getWalletId().equals(rtToEdit.getWalletId())) {
                                actWallet.setText(walletDisplayList.get(i), false);
                                break;
                            }
                        }
                    } else if (!walletDisplayList.isEmpty()) {
                        actWallet.setText(walletDisplayList.get(0), false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });

        // Load Categories from Cache and filter by current tab
        List<Category> allCategories = CategoryCache.getCategories();
        List<Category> categoriesList = new ArrayList<>();
        String typeFilter = isExpenseMode ? "EXPENSE" : "INCOME";
        for (Category c : allCategories) {
            if (typeFilter.equalsIgnoreCase(c.getType())) {
                categoriesList.add(c);
            }
        }
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoriesList);
        actCategory.setAdapter(catAdapter);
        if (!categoriesList.isEmpty()) {
            actCategory.setText(categoriesList.get(0).getName(), false);
        }

        // Set frequencies
        String[] frequencies = {"DAILY", "WEEKLY", "MONTHLY", "YEARLY"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frequencies);
        actFrequency.setAdapter(freqAdapter);
        actFrequency.setText(frequencies[2], false); // MONTHLY default

        if (rtToEdit != null) {
            etAmount.setText(rtToEdit.getAmount() != null ? rtToEdit.getAmount().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() : "");
            etNote.setText(rtToEdit.getNote());
            actFrequency.setText(rtToEdit.getFrequency(), false);
            if (rtToEdit.getNextDueDate() != null) {
                etDueDate.setText(rtToEdit.getNextDueDate());
            }
            
            // preselect category
            for (Category cat : categoriesList) {
                if (cat.getCategoryId() != null && cat.getCategoryId().equals(rtToEdit.getCategoryId())) {
                    actCategory.setText(cat.getName(), false);
                    break;
                }
            }
        }

        builder.setPositiveButton(rtToEdit == null ? "Lên lịch" : "Cập nhật", (dialog, which) -> {
            String catText = actCategory.getText() != null ? actCategory.getText().toString().trim() : "";
            String walletText = actWallet.getText().toString();
            String amtStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String freqStr = actFrequency.getText() != null ? actFrequency.getText().toString().trim() : "MONTHLY";
            String noteStr = etNote.getText() != null ? etNote.getText().toString().trim() : "";
            String dueStr = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

            if (catText.isEmpty() || amtStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ hạng mục và số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId = 1;
            // Match dynamic category by name
            for (Category cat : categoriesList) {
                if (cat.getName() != null && cat.getName().equalsIgnoreCase(catText)) {
                    if (cat.getCategoryId() != null) {
                        categoryId = cat.getCategoryId();
                    }
                    break;
                }
            }

            int selectedWalletId = defaultWalletId != -1 ? defaultWalletId : 1;
            int selectedIndex = walletDisplayList.indexOf(walletText);
            if (selectedIndex >= 0) {
                selectedWalletId = walletList.get(selectedIndex).getWalletId();
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amtStr);
            } catch (Exception e) {
                Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            RecurringTransaction rt = rtToEdit;
            if (rt == null) {
                rt = new RecurringTransaction();
                rt.setIsActive(true);
            }
            rt.setUserId(userId);
            rt.setWalletId(selectedWalletId);
            rt.setCategoryId(categoryId);
            rt.setAmount(amount);
            rt.setFrequency(freqStr);
            rt.setNote(noteStr);
            rt.setNextDueDate(dueStr);

            if (rtToEdit == null) {
                ApiClient.getApiService().createRecurringTransaction(rt).enqueue(new Callback<RecurringTransaction>() {
                    @Override
                    public void onResponse(Call<RecurringTransaction> call, Response<RecurringTransaction> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(RecurringTransactionsActivity.this, "Đã lên lịch giao dịch thành công!", Toast.LENGTH_SHORT).show();
                            loadRecurringTransactions();
                        } else {
                            Toast.makeText(RecurringTransactionsActivity.this, "Lỗi tạo lịch giao dịch!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecurringTransaction> call, Throwable t) {
                        Toast.makeText(RecurringTransactionsActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                updateRecurringOnServer(rt);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateRecurringOnServer(RecurringTransaction rt) {
        ApiClient.getApiService().updateRecurringTransaction(rt.getRecurringId(), userId, rt).enqueue(new Callback<RecurringTransaction>() {
            @Override
            public void onResponse(Call<RecurringTransaction> call, Response<RecurringTransaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RecurringTransactionsActivity.this, "Đã cập nhật giao dịch định kỳ!", Toast.LENGTH_SHORT).show();
                    loadRecurringTransactions();
                } else {
                    Toast.makeText(RecurringTransactionsActivity.this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RecurringTransaction> call, Throwable t) {
                Toast.makeText(RecurringTransactionsActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

