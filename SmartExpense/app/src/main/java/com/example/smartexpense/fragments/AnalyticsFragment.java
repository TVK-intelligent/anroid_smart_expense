package com.example.smartexpense.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.Budget;
import com.example.smartexpense.models.SavingsGoal;
import com.example.smartexpense.models.BurnRateResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsFragment extends Fragment {

    // Tab buttons
    private TextView tabAnalytics, tabBudgets, tabSavings;
    
    // Tab layouts
    private LinearLayout layoutAnalyticsTab, layoutBudgetTab, layoutSavingsTab;

    // Real MPAndroidChart component
    private BarChart chartSpendingTrends;

    // Burn rate views
    private MaterialButton btnCheckBurnRate;
    private LinearLayout layoutBurnRateResult;
    private TextView tvBurnRateTitle, tvBurnRateDesc;

    // Dynamic lists & details
    private LinearLayout layoutSavingsSuggestions;
    
    // Budget Category Views (for Tab 2)
    private TextView tvBudgetSpendTotal, tvBudgetLimitTotal;
    private MaterialButton btnAddBudgetCategory;
    private MaterialButton btnCreateSavingsGoal;

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final Integer CURRENT_USER_ID = 1; // Seed/Mock User ID matching backend

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        // Find Tab Buttons
        tabAnalytics = view.findViewById(R.id.tab_analytics);
        tabBudgets = view.findViewById(R.id.tab_budgets);
        tabSavings = view.findViewById(R.id.tab_savings);

        // Find Tab Layouts
        layoutAnalyticsTab = view.findViewById(R.id.layout_analytics_tab);
        layoutBudgetTab = view.findViewById(R.id.layout_budget_tab);
        layoutSavingsTab = view.findViewById(R.id.layout_savings_tab);

        // Find MPAndroidChart BarChart
        chartSpendingTrends = view.findViewById(R.id.chart_spending_trends);

        // Find Burn rate components
        btnCheckBurnRate = view.findViewById(R.id.btn_check_burn_rate);
        layoutBurnRateResult = view.findViewById(R.id.layout_burn_rate_result);
        tvBurnRateTitle = view.findViewById(R.id.tv_burn_rate_title);
        tvBurnRateDesc = view.findViewById(R.id.tv_burn_rate_desc);

        // Find Savings list layout
        layoutSavingsSuggestions = view.findViewById(R.id.layout_savings_suggestions);

        // Find Budget info components (Tab 2)
        tvBudgetSpendTotal = view.findViewById(R.id.tv_budget_spend_total);
        tvBudgetLimitTotal = view.findViewById(R.id.tv_budget_limit_total);
        
        setupTabs();
        setupBurnRateAction();
        setupActionButtons(view);
        setupChart();
        
        // Load initial data
        loadBudgets();
        loadSavingsGoals();

        return view;
    }

    private void setupTabs() {
        tabAnalytics.setOnClickListener(v -> selectTab(0));
        tabBudgets.setOnClickListener(v -> selectTab(1));
        tabSavings.setOnClickListener(v -> selectTab(2));
    }

    private void selectTab(int index) {
        if (index == 0) {
            tabAnalytics.setBackgroundResource(R.drawable.bg_pill_chip_active);
            tabAnalytics.setTextColor(getResources().getColor(R.color.primary));
            tabBudgets.setBackgroundResource(android.R.color.transparent);
            tabBudgets.setTextColor(getResources().getColor(R.color.text_secondary));
            tabSavings.setBackgroundResource(android.R.color.transparent);
            tabSavings.setTextColor(getResources().getColor(R.color.text_secondary));

            layoutAnalyticsTab.setVisibility(View.VISIBLE);
            layoutBudgetTab.setVisibility(View.GONE);
            layoutSavingsTab.setVisibility(View.GONE);
        } else if (index == 1) {
            tabBudgets.setBackgroundResource(R.drawable.bg_pill_chip_active);
            tabBudgets.setTextColor(getResources().getColor(R.color.primary));
            tabAnalytics.setBackgroundResource(android.R.color.transparent);
            tabAnalytics.setTextColor(getResources().getColor(R.color.text_secondary));
            tabSavings.setBackgroundResource(android.R.color.transparent);
            tabSavings.setTextColor(getResources().getColor(R.color.text_secondary));

            layoutAnalyticsTab.setVisibility(View.GONE);
            layoutBudgetTab.setVisibility(View.VISIBLE);
            layoutSavingsTab.setVisibility(View.GONE);
            loadBudgets();
        } else if (index == 2) {
            tabSavings.setBackgroundResource(R.drawable.bg_pill_chip_active);
            tabSavings.setTextColor(getResources().getColor(R.color.primary));
            tabAnalytics.setBackgroundResource(android.R.color.transparent);
            tabAnalytics.setTextColor(getResources().getColor(R.color.text_secondary));
            tabBudgets.setBackgroundResource(android.R.color.transparent);
            tabBudgets.setTextColor(getResources().getColor(R.color.text_secondary));

            layoutAnalyticsTab.setVisibility(View.GONE);
            layoutBudgetTab.setVisibility(View.GONE);
            layoutSavingsTab.setVisibility(View.VISIBLE);
            loadSavingsGoals();
        }
    }

    private void setupChart() {
        if (chartSpendingTrends == null) return;

        // Populate beautiful dummy entries for Monday - Sunday spending
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 600000f));
        entries.add(new BarEntry(1f, 800000f));
        entries.add(new BarEntry(2f, 400000f));
        entries.add(new BarEntry(3f, 950000f));
        entries.add(new BarEntry(4f, 300000f));
        entries.add(new BarEntry(5f, 500000f));
        entries.add(new BarEntry(6f, 200000f));

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu ngày (đ)");
        
        int primaryColor = getResources().getColor(R.color.accent_blue);
        int secondaryColor = getResources().getColor(R.color.progress_track);
        
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (i == 1 || i == 3) {
                colors.add(primaryColor);
            } else {
                colors.add(secondaryColor);
            }
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_secondary));
        dataSet.setValueTextSize(8f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.45f);

        chartSpendingTrends.setData(barData);

        // Styling chart aesthetics
        chartSpendingTrends.getDescription().setEnabled(false);
        chartSpendingTrends.getLegend().setEnabled(false);
        chartSpendingTrends.setDrawGridBackground(false);
        chartSpendingTrends.setDrawBarShadow(false);

        // X Axis customization
        XAxis xAxis = chartSpendingTrends.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary));
        xAxis.setTextSize(9f);
        
        final String[] days = new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < days.length) {
                    return days[idx];
                }
                return "";
            }
        });

        // Y Axis customization (Keep grid clean and minimalist)
        YAxis leftAxis = chartSpendingTrends.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);

        YAxis rightAxis = chartSpendingTrends.getAxisRight();
        rightAxis.setEnabled(false);

        chartSpendingTrends.animateY(800);
        chartSpendingTrends.invalidate();
    }

    private void setupBurnRateAction() {
        btnCheckBurnRate.setOnClickListener(v -> {
            ApiClient.getApiService().checkBurnRate(CURRENT_USER_ID, 1).enqueue(new Callback<BurnRateResponse>() {
                @Override
                public void onResponse(Call<BurnRateResponse> call, Response<BurnRateResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BurnRateResponse br = response.body();
                        layoutBurnRateResult.setVisibility(View.VISIBLE);
                        tvBurnRateDesc.setText(br.getAlertMessage());

                        if (br.isHasAlert()) {
                            tvBurnRateTitle.setText("⚠️ CHI TIÊU VƯỢT TIẾN ĐỘ!");
                            tvBurnRateTitle.setTextColor(getResources().getColor(R.color.crimson_expense));
                            layoutBurnRateResult.setBackgroundResource(R.drawable.bg_pill_chip);
                        } else {
                            tvBurnRateTitle.setText("✅ TỐC ĐỘ CHI TIÊU AN TOÀN");
                            tvBurnRateTitle.setTextColor(getResources().getColor(R.color.emerald_income));
                            layoutBurnRateResult.setBackgroundResource(R.drawable.bg_pill_chip_active);
                        }
                    }
                }

                @Override
                public void onFailure(Call<BurnRateResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối tới Spring Boot cổng 8080!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupActionButtons(View view) {
        btnAddBudgetCategory = view.findViewById(R.id.btn_add_budget);
        if (btnAddBudgetCategory != null) {
            btnAddBudgetCategory.setOnClickListener(v -> showAddBudgetDialog());
        }

        btnCreateSavingsGoal = view.findViewById(R.id.btn_create_goal);
        if (btnCreateSavingsGoal != null) {
            btnCreateSavingsGoal.setOnClickListener(v -> showCreateGoalDialog());
        }
    }

    private void loadBudgets() {
        ApiClient.getApiService().getBudgets(CURRENT_USER_ID).enqueue(new Callback<List<Budget>>() {
            @Override
            public void onResponse(Call<List<Budget>> call, Response<List<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Budget> budgets = response.body();
                    updateBudgetsUI(budgets);
                }
            }

            @Override
            public void onFailure(Call<List<Budget>> call, Throwable t) {
                // Fallback handled beautifully by XML templates
            }
        });
    }

    private void updateBudgetsUI(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty() || getContext() == null) return;

        BigDecimal totalLimit = BigDecimal.ZERO;
        for (Budget b : budgets) {
            totalLimit = totalLimit.add(b.getAmount());
        }

        // Update the header summary
        tvBudgetLimitTotal.setText(" / " + formatter.format(totalLimit) + "đ");
    }

    private void loadSavingsGoals() {
        ApiClient.getApiService().getSavingsGoals(CURRENT_USER_ID).enqueue(new Callback<List<SavingsGoal>>() {
            @Override
            public void onResponse(Call<List<SavingsGoal>> call, Response<List<SavingsGoal>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SavingsGoal> goals = response.body();
                    updateSavingsGoalsUI(goals);
                }
            }

            @Override
            public void onFailure(Call<List<SavingsGoal>> call, Throwable t) {
                // Fallback handled beautifully by XML templates
            }
        });
    }

    private void updateSavingsGoalsUI(List<SavingsGoal> goals) {
        if (getContext() == null || layoutSavingsSuggestions == null) return;

        if (goals == null || goals.isEmpty()) {
            return;
        }

        layoutSavingsSuggestions.removeAllViews();

        for (SavingsGoal goal : goals) {
            MaterialCardView card = new MaterialCardView(getContext());
            card.setRadius(16 * getResources().getDisplayMetrics().density);
            card.setCardElevation(2 * getResources().getDisplayMetrics().density);
            card.setCardBackgroundColor(getResources().getColor(R.color.surface_white));
            
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
            card.setLayoutParams(cardParams);

            LinearLayout rootLayout = new LinearLayout(getContext());
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            rootLayout.setPadding(padding, padding, padding, padding);

            // Row 1: Title and % percentage
            RelativeLayout row1 = new RelativeLayout(getContext());
            
            TextView tvTitle = new TextView(getContext());
            tvTitle.setId(View.generateViewId());
            tvTitle.setText(goal.getGoalId() == 1 ? "Tích Lũy Mua Laptop Workstation" : goal.getGoalId() == 2 ? "Đi Du Lịch Phú Quốc" : "Mục Tiêu Mới Thiết Lập");
            tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tvTitle.setLayoutParams(titleParams);

            TextView tvPct = new TextView(getContext());
            tvPct.setId(View.generateViewId());
            
            double progressPct = 0;
            if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                progressPct = goal.getCurrentAmount().multiply(new BigDecimal("100"))
                        .divide(goal.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            tvPct.setText(String.format("%.0f%%", progressPct));
            tvPct.setTextColor(getResources().getColor(R.color.accent_blue));
            tvPct.setTextSize(16);
            tvPct.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams pctParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            pctParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tvPct.setLayoutParams(pctParams);

            row1.addView(tvTitle);
            row1.addView(tvPct);

            // Row 2: Subtitle
            TextView tvSub = new TextView(getContext());
            tvSub.setText("Hạn chót: " + goal.getDeadline());
            tvSub.setTextColor(getResources().getColor(R.color.text_secondary));
            tvSub.setTextSize(11);
            tvSub.setPadding(0, 2, 0, 8);

            // Value text
            TextView tvVal = new TextView(getContext());
            tvVal.setText("Đã tích lũy: " + formatter.format(goal.getCurrentAmount()) + "đ / " + formatter.format(goal.getTargetAmount()) + "đ");
            tvVal.setTextColor(getResources().getColor(R.color.text_secondary));
            tvVal.setTextSize(12);
            tvVal.setPadding(0, 0, 0, 8);

            // Progress bar
            ProgressBar bar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress((int) progressPct);
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.accent_blue)));
            bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.progress_track)));
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (4 * getResources().getDisplayMetrics().density)
            );
            barParams.setMargins(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
            bar.setLayoutParams(barParams);

            // Row 3: Action Buttons
            RelativeLayout row3 = new RelativeLayout(getContext());
            
            TextView tvTip = new TextView(getContext());
            tvTip.setText("💡 Trích lũy thông minh được bật");
            tvTip.setTextColor(getResources().getColor(R.color.primary));
            tvTip.setTextSize(10);
            tvTip.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams tipParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            tipParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tipParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvTip.setLayoutParams(tipParams);

            MaterialButton btnAddFunds = new MaterialButton(getContext());
            btnAddFunds.setText("Nạp Quỹ");
            btnAddFunds.setTextColor(getResources().getColor(R.color.surface_white));
            btnAddFunds.setTextSize(10);
            btnAddFunds.setTypeface(null, android.graphics.Typeface.BOLD);
            btnAddFunds.setPadding(0, 0, 0, 0);
            btnAddFunds.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            btnAddFunds.setCornerRadius((int) (8 * getResources().getDisplayMetrics().density));
            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                    (int) (90 * getResources().getDisplayMetrics().density),
                    (int) (36 * getResources().getDisplayMetrics().density)
            );
            btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnAddFunds.setLayoutParams(btnParams);
            
            final Integer finalGoalId = goal.getGoalId();
            btnAddFunds.setOnClickListener(v -> showAddFundsDialog(finalGoalId));

            row3.addView(tvTip);
            row3.addView(btnAddFunds);

            rootLayout.addView(row1);
            rootLayout.addView(tvSub);
            rootLayout.addView(tvVal);
            rootLayout.addView(bar);
            rootLayout.addView(row3);

            card.addView(rootLayout);
            layoutSavingsSuggestions.addView(card);
        }
    }

    private void showAddFundsDialog(Integer goalId) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nạp thêm vào quỹ tích lũy");

        TextInputLayout layoutInput = new TextInputLayout(getContext());
        layoutInput.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutInput.setBoxStrokeColor(getResources().getColor(R.color.accent_blue));
        layoutInput.setHint("Số tiền nạp (VND)");
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin/2, margin, margin/2);
        layoutInput.setLayoutParams(params);

        TextInputEditText etAmount = new TextInputEditText(getContext());
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        etAmount.setTextSize(14);
        layoutInput.addView(etAmount);

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(layoutInput);
        builder.setView(container);

        builder.setPositiveButton("Nạp Ngay", (dialog, which) -> {
            String valStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (valStr.isEmpty()) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount = new BigDecimal(valStr);
            ApiClient.getApiService().addFundsToGoal(goalId, amount).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã nạp +" + formatter.format(amount) + "đ thành công!", Toast.LENGTH_SHORT).show();
                        loadSavingsGoals(); // Refresh values
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showAddBudgetDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thiết lập ngân sách mới");

        LinearLayout layoutContainer = new LinearLayout(getContext());
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layoutContainer.setPadding(padding, padding, padding, padding);

        // Input Category ID
        TextInputLayout layoutCat = new TextInputLayout(getContext());
        layoutCat.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutCat.setHint("Mã hạng mục (Ví dụ: 1-Ăn uống, 2-Di chuyển)");
        TextInputEditText etCat = new TextInputEditText(getContext());
        etCat.setInputType(InputType.TYPE_CLASS_NUMBER);
        layoutCat.addView(etCat);
        layoutContainer.addView(layoutCat);

        // Input Amount
        TextInputLayout layoutAmt = new TextInputLayout(getContext());
        layoutAmt.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutAmt.setHint("Hạn mức chi tiêu tối đa (VND)");
        TextInputEditText etAmt = new TextInputEditText(getContext());
        etAmt.setInputType(InputType.TYPE_CLASS_NUMBER);
        layoutAmt.addView(etAmt);
        layoutContainer.addView(layoutAmt);

        builder.setView(layoutContainer);

        builder.setPositiveButton("Thiết Lập", (dialog, which) -> {
            String catStr = etCat.getText() != null ? etCat.getText().toString().trim() : "";
            String amtStr = etAmt.getText() != null ? etAmt.getText().toString().trim() : "";

            if (catStr.isEmpty() || amtStr.isEmpty()) {
                Toast.makeText(getContext(), "Thông tin không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            Budget budget = new Budget();
            budget.setUserId(CURRENT_USER_ID);
            budget.setCategoryId(Integer.parseInt(catStr));
            budget.setAmount(new BigDecimal(amtStr));

            ApiClient.getApiService().createBudget(budget).enqueue(new Callback<Budget>() {
                @Override
                public void onResponse(Call<Budget> call, Response<Budget> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Thiết lập ngân sách thành công!", Toast.LENGTH_SHORT).show();
                        loadBudgets(); // Refresh list
                    }
                }

                @Override
                public void onFailure(Call<Budget> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showCreateGoalDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thiết lập mục tiêu tích lũy mới");

        LinearLayout layoutContainer = new LinearLayout(getContext());
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layoutContainer.setPadding(padding, padding, padding, padding);

        // Input Goal target
        TextInputLayout layoutTar = new TextInputLayout(getContext());
        layoutTar.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutTar.setHint("Số tiền mục tiêu tích lũy (VND)");
        TextInputEditText etTar = new TextInputEditText(getContext());
        etTar.setInputType(InputType.TYPE_CLASS_NUMBER);
        layoutTar.addView(etTar);
        layoutContainer.addView(layoutTar);

        builder.setView(layoutContainer);

        builder.setPositiveButton("Thiết Lập", (dialog, which) -> {
            String tarStr = etTar.getText() != null ? etTar.getText().toString().trim() : "";

            if (tarStr.isEmpty()) {
                Toast.makeText(getContext(), "Số tiền không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            SavingsGoal goal = new SavingsGoal();
            goal.setUserId(CURRENT_USER_ID);
            goal.setTargetAmount(new BigDecimal(tarStr));

            ApiClient.getApiService().createSavingsGoal(goal).enqueue(new Callback<SavingsGoal>() {
                @Override
                public void onResponse(Call<SavingsGoal> call, Response<SavingsGoal> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Thiết lập mục tiêu thành công!", Toast.LENGTH_SHORT).show();
                        loadSavingsGoals(); // Refresh list
                    }
                }

                @Override
                public void onFailure(Call<SavingsGoal> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
