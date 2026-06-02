package com.example.smartexpense.activities;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.SavingsGoal;
import com.example.smartexpense.models.Wallet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavingsGoalsActivity extends BaseActivity {

    private ImageView btnBack;
    private LinearLayout layoutGoalsContainer;
    private TextView tvEmptyState;
    private MaterialButton btnAddGoal;

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        currentUserId = getSharedPreferences("smart_expense_prefs", MODE_PRIVATE).getInt("user_id", 1);

        btnBack = findViewById(R.id.btn_back);
        layoutGoalsContainer = findViewById(R.id.layout_goals_container);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnAddGoal = findViewById(R.id.btn_add_goal);

        btnBack.setOnClickListener(v -> finish());
        btnAddGoal.setOnClickListener(v -> showAddGoalDialog());

        loadSavingsGoals();
    }

    private void loadSavingsGoals() {
        ApiClient.getApiService().getSavingsGoals(currentUserId).enqueue(new Callback<List<SavingsGoal>>() {
            @Override
            public void onResponse(Call<List<SavingsGoal>> call, Response<List<SavingsGoal>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    layoutGoalsContainer.removeAllViews();
                }
            }

            @Override
            public void onFailure(Call<List<SavingsGoal>> call, Throwable t) {
                Toast.makeText(SavingsGoalsActivity.this, 
                        Locale.getDefault().getLanguage().equals("vi") ? "Lỗi kết nối tới server!" : "Server connection error!", 
                        Toast.LENGTH_SHORT).show();
                tvEmptyState.setVisibility(View.VISIBLE);
                layoutGoalsContainer.removeAllViews();
            }
        });
    }

    private void updateUI(List<SavingsGoal> list) {
        layoutGoalsContainer.removeAllViews();

        if (list == null || list.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);

        float density = getResources().getDisplayMetrics().density;

        for (SavingsGoal goal : list) {
            MaterialCardView card = new MaterialCardView(this);
            card.setRadius(16 * density);
            card.setCardElevation(2 * density);
            card.setCardBackgroundColor(getResources().getColor(R.color.surface_white));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, (int) (16 * density));
            card.setLayoutParams(cardParams);

            LinearLayout rootLayout = new LinearLayout(this);
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * density);
            rootLayout.setPadding(padding, padding, padding, padding);

            // Row 1: Goal Name (Left) and Status badge (Right)
            RelativeLayout row1 = new RelativeLayout(this);
            RelativeLayout.LayoutParams row1Params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            row1.setLayoutParams(row1Params);

            TextView tvStatus = new TextView(this);
            tvStatus.setId(View.generateViewId());

            BigDecimal target = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal current = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;

            int percentVal = 0;
            if (target.compareTo(BigDecimal.ZERO) > 0) {
                percentVal = current.multiply(new BigDecimal(100)).divide(target, 0, RoundingMode.HALF_UP).intValue();
            }

            boolean isCompleted = percentVal >= 100;
            tvStatus.setText(isCompleted ? 
                    (Locale.getDefault().getLanguage().equals("vi") ? "Hoàn thành" : "Completed") : 
                    (Locale.getDefault().getLanguage().equals("vi") ? "Đang tiến hành" : "In Progress"));
            tvStatus.setTextColor(getResources().getColor(R.color.surface_white));
            tvStatus.setTextSize(11);
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
            tvStatus.setPadding((int) (8 * density), (int) (4 * density), (int) (8 * density), (int) (4 * density));

            android.graphics.drawable.GradientDrawable statusBg = new android.graphics.drawable.GradientDrawable();
            statusBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            statusBg.setCornerRadius(12 * density);
            statusBg.setColor(getResources().getColor(isCompleted ? R.color.emerald_income : R.color.accent_blue));
            tvStatus.setBackground(statusBg);

            RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            statusParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            statusParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvStatus.setLayoutParams(statusParams);

            TextView tvName = new TextView(this);
            tvName.setText(goal.getGoalName());
            tvName.setTextColor(getResources().getColor(R.color.text_primary));
            tvName.setTextSize(16);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            nameParams.addRule(RelativeLayout.CENTER_VERTICAL);
            nameParams.addRule(RelativeLayout.LEFT_OF, tvStatus.getId());
            nameParams.setMarginEnd((int) (8 * density));
            tvName.setLayoutParams(nameParams);

            row1.addView(tvStatus);
            row1.addView(tvName);
            rootLayout.addView(row1);

            // Row 2: Deadline
            TextView tvDeadline = new TextView(this);
            String deadlineText = getString(R.string.savings_goals_label_deadline) + (goal.getDeadline() != null ? goal.getDeadline() : "N/A");
            tvDeadline.setText(deadlineText);
            tvDeadline.setTextColor(getResources().getColor(R.color.text_secondary));
            tvDeadline.setTextSize(12);
            tvDeadline.setPadding(0, (int) (6 * density), 0, (int) (8 * density));
            rootLayout.addView(tvDeadline);

            // Progress Bar
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setProgress(Math.min(100, percentVal));
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.emerald_income)));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.progress_track)));

            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (8 * density)
            );
            progressParams.setMargins(0, 0, 0, (int) (8 * density));
            progressBar.setLayoutParams(progressParams);
            rootLayout.addView(progressBar);

            // Progress Text
            TextView tvProgressText = new TextView(this);
            String percentStr = percentVal + "%";
            String progressTextStr = getString(R.string.savings_goals_label_saved) + 
                    formatter.format(current) + "đ / " + 
                    formatter.format(target) + "đ (" + percentStr + ")";
            tvProgressText.setText(progressTextStr);
            tvProgressText.setTextColor(getResources().getColor(R.color.text_primary));
            tvProgressText.setTextSize(12);
            tvProgressText.setTypeface(null, android.graphics.Typeface.BOLD);
            tvProgressText.setPadding(0, 0, 0, (int) (12 * density));
            rootLayout.addView(tvProgressText);

            // Divider
            View divider = new View(this);
            divider.setBackgroundColor(getResources().getColor(R.color.progress_track));
            LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (1 * density)
            );
            divParams.setMargins(0, 0, 0, (int) (12 * density));
            divider.setLayoutParams(divParams);
            rootLayout.addView(divider);

            // Action Row
            RelativeLayout actionLayout = new RelativeLayout(this);
            RelativeLayout.LayoutParams actionLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            actionLayout.setLayoutParams(actionLayoutParams);

            MaterialButton btnDeposit = new MaterialButton(this);
            btnDeposit.setText(getString(R.string.savings_goals_label_add_funds));
            btnDeposit.setTextColor(getResources().getColor(R.color.surface_white));
            btnDeposit.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            btnDeposit.setCornerRadius((int) (12 * density));
            btnDeposit.setTextSize(11);
            btnDeposit.setTypeface(null, android.graphics.Typeface.BOLD);
            btnDeposit.setPadding((int) (12 * density), 0, (int) (12 * density), 0);

            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    (int) (36 * density)
            );
            btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnDeposit.setLayoutParams(btnParams);
            btnDeposit.setOnClickListener(v -> showAddFundsDialog(goal));

            actionLayout.addView(btnDeposit);
            rootLayout.addView(actionLayout);

            card.addView(rootLayout);
            layoutGoalsContainer.addView(card);
        }
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);

        TextInputEditText etName = dialogView.findViewById(R.id.et_goal_name);
        TextInputEditText etTarget = dialogView.findViewById(R.id.et_target_amount);
        TextInputEditText etInitial = dialogView.findViewById(R.id.et_initial_amount);
        TextInputEditText etDeadline = dialogView.findViewById(R.id.et_deadline);

        etDeadline.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.savings_goals_hint_deadline))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                etDeadline.setText(sdf.format(new Date(selection)));
            });
            datePicker.show(getSupportFragmentManager(), "SAVINGS_DEADLINE_PICKER");
        });

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.savings_goals_label_create), (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String targetStr = etTarget.getText() != null ? etTarget.getText().toString().trim() : "";
                    String initialStr = etInitial.getText() != null ? etInitial.getText().toString().trim() : "";
                    String deadline = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Vui lòng nhập tên mục tiêu!" : "Please enter goal name!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(targetStr)) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Vui lòng nhập số tiền mục tiêu!" : "Please enter target amount!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BigDecimal targetAmt;
                    try {
                        targetAmt = new BigDecimal(targetStr);
                        if (targetAmt.compareTo(BigDecimal.ZERO) <= 0) {
                            Toast.makeText(this, 
                                    Locale.getDefault().getLanguage().equals("vi") ? "Số tiền mục tiêu phải lớn hơn 0!" : "Target amount must be greater than 0!", 
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Số tiền mục tiêu không hợp lệ!" : "Invalid target amount!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BigDecimal initialAmt = BigDecimal.ZERO;
                    if (!TextUtils.isEmpty(initialStr)) {
                        try {
                            initialAmt = new BigDecimal(initialStr);
                            if (initialAmt.compareTo(BigDecimal.ZERO) < 0) {
                                Toast.makeText(this, 
                                        Locale.getDefault().getLanguage().equals("vi") ? "Số tiền tích lũy ban đầu không được âm!" : "Initial deposit cannot be negative!", 
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (initialAmt.compareTo(targetAmt) > 0) {
                                Toast.makeText(this, 
                                        Locale.getDefault().getLanguage().equals("vi") ? "Số tiền ban đầu không được lớn hơn số tiền mục tiêu!" : "Initial deposit cannot exceed target amount!", 
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, 
                                    Locale.getDefault().getLanguage().equals("vi") ? "Số tiền tích lũy ban đầu không hợp lệ!" : "Invalid initial deposit!", 
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (TextUtils.isEmpty(deadline)) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Vui lòng chọn hạn chót!" : "Please select a deadline!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SavingsGoal newGoal = new SavingsGoal();
                    newGoal.setUserId(currentUserId);
                    newGoal.setGoalName(name);
                    newGoal.setTargetAmount(targetAmt);
                    newGoal.setCurrentAmount(initialAmt);
                    newGoal.setDeadline(deadline);
                    newGoal.setStatus("IN_PROGRESS");

                    createGoalOnServer(newGoal);
                })
                .setNegativeButton(getString(R.string.savings_goals_label_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void createGoalOnServer(SavingsGoal goal) {
        ApiClient.getApiService().createSavingsGoal(goal).enqueue(new Callback<SavingsGoal>() {
            @Override
            public void onResponse(Call<SavingsGoal> call, Response<SavingsGoal> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SavingsGoalsActivity.this, 
                            Locale.getDefault().getLanguage().equals("vi") ? "Tạo mục tiêu thành công!" : "Savings goal created successfully!", 
                            Toast.LENGTH_SHORT).show();
                    loadSavingsGoals();
                } else {
                    Toast.makeText(SavingsGoalsActivity.this, 
                            Locale.getDefault().getLanguage().equals("vi") ? "Không tạo được mục tiêu!" : "Failed to create savings goal!", 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SavingsGoal> call, Throwable t) {
                Toast.makeText(SavingsGoalsActivity.this, 
                        Locale.getDefault().getLanguage().equals("vi") ? "Lỗi mạng, vui lòng thử lại!" : "Network error, please try again!", 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFundsDialog(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_funds, null);

        AutoCompleteTextView actWallet = dialogView.findViewById(R.id.act_source_wallet);
        TextInputEditText etDeposit = dialogView.findViewById(R.id.et_deposit_amount);

        final List<Wallet> walletList = new ArrayList<>();
        final List<String> walletDisplayList = new ArrayList<>();

        ApiClient.getApiService().getWallets(currentUserId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    walletList.addAll(response.body());
                    for (Wallet w : walletList) {
                        String displayName = w.getName() + " (" + formatter.format(w.getBalance()) + "đ)";
                        walletDisplayList.add(displayName);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            SavingsGoalsActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            walletDisplayList
                    );
                    actWallet.setAdapter(adapter);
                } else {
                    Toast.makeText(SavingsGoalsActivity.this, 
                            Locale.getDefault().getLanguage().equals("vi") ? "Không lấy được danh sách ví!" : "Failed to load wallets!", 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(SavingsGoalsActivity.this, 
                        Locale.getDefault().getLanguage().equals("vi") ? "Lỗi mạng khi tải ví!" : "Network error loading wallets!", 
                        Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.savings_goals_label_confirm), (dialog, which) -> {
                    String selectedWalletStr = actWallet.getText().toString();
                    String depositStr = etDeposit.getText() != null ? etDeposit.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(selectedWalletStr)) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Vui lòng chọn ví nguồn!" : "Please select a source wallet!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedIndex = walletDisplayList.indexOf(selectedWalletStr);
                    if (selectedIndex < 0) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Ví không hợp lệ!" : "Invalid wallet selected!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Wallet selectedWallet = walletList.get(selectedIndex);

                    if (TextUtils.isEmpty(depositStr)) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Vui lòng nhập số tiền nạp!" : "Please enter deposit amount!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BigDecimal depositAmt;
                    try {
                        depositAmt = new BigDecimal(depositStr);
                        if (depositAmt.compareTo(BigDecimal.ZERO) <= 0) {
                            Toast.makeText(this, 
                                    Locale.getDefault().getLanguage().equals("vi") ? "Số tiền nạp phải lớn hơn 0!" : "Deposit amount must be greater than 0!", 
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Số tiền nạp không hợp lệ!" : "Invalid deposit amount!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedWallet.getBalance() == null || selectedWallet.getBalance().compareTo(depositAmt) < 0) {
                        Toast.makeText(this, 
                                Locale.getDefault().getLanguage().equals("vi") ? "Số dư ví không đủ để chuyển!" : "Insufficient wallet balance!", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    processDeposit(goal, selectedWallet, depositAmt);
                })
                .setNegativeButton(getString(R.string.savings_goals_label_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void processDeposit(SavingsGoal goal, Wallet wallet, BigDecimal amount) {
        ApiClient.getApiService().addFundsToGoal(goal.getGoalId(), amount).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Wallet updatedWallet = new Wallet();
                    updatedWallet.setName(wallet.getName());
                    updatedWallet.setType(wallet.getType());
                    updatedWallet.setBalance(wallet.getBalance().subtract(amount));

                    ApiClient.getApiService().updateWallet(wallet.getWalletId(), currentUserId, updatedWallet).enqueue(new Callback<Wallet>() {
                        @Override
                        public void onResponse(Call<Wallet> call, Response<Wallet> walletResponse) {
                            if (walletResponse.isSuccessful()) {
                                Toast.makeText(SavingsGoalsActivity.this, 
                                        Locale.getDefault().getLanguage().equals("vi") ? "Nạp quỹ tiết kiệm thành công!" : "Funds successfully deposited!", 
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SavingsGoalsActivity.this, 
                                        Locale.getDefault().getLanguage().equals("vi") ? "Đã nạp quỹ nhưng không thể trừ tiền ví nguồn!" : "Deposited, but failed to update wallet balance!", 
                                        Toast.LENGTH_LONG).show();
                            }
                            loadSavingsGoals();
                        }

                        @Override
                        public void onFailure(Call<Wallet> call, Throwable t) {
                            Toast.makeText(SavingsGoalsActivity.this, 
                                    Locale.getDefault().getLanguage().equals("vi") ? "Đã nạp quỹ nhưng lỗi kết nối khi trừ tiền ví nguồn!" : "Deposited, but network error updating wallet balance!", 
                                    Toast.LENGTH_LONG).show();
                            loadSavingsGoals();
                        }
                    });
                } else {
                    Toast.makeText(SavingsGoalsActivity.this, 
                            Locale.getDefault().getLanguage().equals("vi") ? "Không nạp được quỹ tiết kiệm!" : "Failed to deposit funds!", 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(SavingsGoalsActivity.this, 
                        Locale.getDefault().getLanguage().equals("vi") ? "Lỗi mạng kết nối tới server!" : "Network error, failed to deposit funds!", 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
