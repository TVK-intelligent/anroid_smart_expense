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
import com.example.smartexpense.models.BudgetDetailResponse;
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
    private LinearLayout layoutBudgetsContainer;
    
    // Tab 3 Header & AI Suggestion Views
    private TextView tvSavingsTotalCurrent, tvSavingsTotalTarget, tvSavingsTotalPercent;
    private ProgressBar pbSavingsTotal;
    private LinearLayout layoutAiSurplusContainer;
    private TextView tvAiSurplusAmount;
    private MaterialButton btnAiAutoAllocate;

    // Local Map to cache AI savings suggestions
    private final java.util.Map<Integer, com.example.smartexpense.models.SavingsSuggestion> savingsSuggestionsMap = new java.util.HashMap<>();

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

        // Find Tab 3 Dynamic Header and AI Suggestion Views
        tvSavingsTotalCurrent = view.findViewById(R.id.tv_savings_total_current);
        tvSavingsTotalTarget = view.findViewById(R.id.tv_savings_total_target);
        tvSavingsTotalPercent = view.findViewById(R.id.tv_savings_total_percent);
        pbSavingsTotal = view.findViewById(R.id.pb_savings_total);
        layoutAiSurplusContainer = view.findViewById(R.id.layout_ai_surplus_container);
        tvAiSurplusAmount = view.findViewById(R.id.tv_ai_surplus_amount);
        btnAiAutoAllocate = view.findViewById(R.id.btn_ai_auto_allocate);

        // Find Budget info components (Tab 2)
        tvBudgetSpendTotal = view.findViewById(R.id.tv_budget_spend_total);
        tvBudgetLimitTotal = view.findViewById(R.id.tv_budget_limit_total);
        layoutBudgetsContainer = view.findViewById(R.id.layout_budgets_container);
        
        setupTabs();
        setupBurnRateAction();
        setupActionButtons(view);
        setupAiAutoAllocateAction();
        setupChart();
        
        // Load initial data
        loadBudgets();
        loadSavingsSuggestions(); // Gọi load suggestions trước để có map gợi ý, sau đó load goals!

        return view;
    }

    private void setupTabs() {
        tabAnalytics.setOnClickListener(v -> selectTab(0));
        tabBudgets.setOnClickListener(v -> selectTab(1));
        tabSavings.setOnClickListener(v -> selectTab(2));
    }

    private void setupChart() {
        loadWeeklySpendingChart();
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
            loadSavingsSuggestions();
        }
    }

    private void loadWeeklySpendingChart() {
        if (chartSpendingTrends == null || getContext() == null) return;

        // Compute current week's Monday and Sunday
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        java.time.LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startStr = startOfWeek.format(dtf);
        String endStr = endOfWeek.format(dtf);

        ApiClient.getApiService().filterTransactions(CURRENT_USER_ID, startStr, endStr, null, null, "EXPENSE")
                .enqueue(new Callback<List<com.example.smartexpense.models.Transaction>>() {
                    @Override
                    public void onResponse(Call<List<com.example.smartexpense.models.Transaction>> call, Response<List<com.example.smartexpense.models.Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            populateChartWithRealData(response.body(), startOfWeek);
                        } else {
                            populateChartWithRealData(new ArrayList<>(), startOfWeek);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.smartexpense.models.Transaction>> call, Throwable t) {
                        populateChartWithRealData(new ArrayList<>(), startOfWeek);
                    }
                });
    }

    private void populateChartWithRealData(List<com.example.smartexpense.models.Transaction> transactions, java.time.LocalDate startOfWeek) {
        if (chartSpendingTrends == null || getContext() == null) return;

        BigDecimal[] daySums = new BigDecimal[7];
        for (int i = 0; i < 7; i++) {
            daySums[i] = BigDecimal.ZERO;
        }

        java.time.format.DateTimeFormatter parser = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (com.example.smartexpense.models.Transaction t : transactions) {
            if (t.getTransactionDate() != null && t.getAmount() != null) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(t.getTransactionDate(), parser);
                    int dayIndex = date.getDayOfWeek().getValue() - 1; // MONDAY is 1, SUNDAY is 7 -> index 0 to 6
                    if (dayIndex >= 0 && dayIndex < 7) {
                        daySums[dayIndex] = daySums[dayIndex].add(t.getAmount());
                    }
                } catch (Exception ignored) {}
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, daySums[i].floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu ngày (đ)");
        
        int primaryColor = getResources().getColor(R.color.accent_blue);
        int secondaryColor = getResources().getColor(R.color.progress_track);
        
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getY() > 0) {
                colors.add(primaryColor);
            } else {
                colors.add(secondaryColor);
            }
        }
        dataSet.setColors(colors);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_secondary));
        dataSet.setValueTextSize(8f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) return "";
                return formatter.format(value) + "đ";
            }
        });

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

        // Y Axis customization
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
                            
                            // Speak burn rate voice warning
                            com.example.smartexpense.utils.TextToSpeechHelper.getInstance(getContext())
                                    .speak(getContext(), br.getAlertMessage());
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
        ApiClient.getApiService().getBudgetDetails(CURRENT_USER_ID).enqueue(new Callback<List<BudgetDetailResponse>>() {
            @Override
            public void onResponse(Call<List<BudgetDetailResponse>> call, Response<List<BudgetDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BudgetDetailResponse> budgets = response.body();
                    updateBudgetsUI(budgets);
                }
            }

            @Override
            public void onFailure(Call<List<BudgetDetailResponse>> call, Throwable t) {
                // Fallback
            }
        });
    }

    private void updateBudgetsUI(List<BudgetDetailResponse> budgets) {
        if (getContext() == null || layoutBudgetsContainer == null) return;

        layoutBudgetsContainer.removeAllViews();

        if (budgets == null || budgets.isEmpty()) {
            tvBudgetSpendTotal.setText("0đ");
            tvBudgetLimitTotal.setText(" / 0đ");
            return;
        }

        BigDecimal totalLimit = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (BudgetDetailResponse b : budgets) {
            totalLimit = totalLimit.add(b.getLimitAmount() != null ? b.getLimitAmount() : BigDecimal.ZERO);
            totalSpent = totalSpent.add(b.getSpentAmount() != null ? b.getSpentAmount() : BigDecimal.ZERO);

            // Dựng card động cho từng ngân sách
            MaterialCardView card = new MaterialCardView(getContext());
            card.setRadius(16 * getResources().getDisplayMetrics().density);
            card.setCardElevation(1 * getResources().getDisplayMetrics().density);
            card.setCardBackgroundColor(getResources().getColor(R.color.surface_white));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
            card.setLayoutParams(cardParams);

            LinearLayout rootLayout = new LinearLayout(getContext());
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            rootLayout.setPadding(padding, padding, padding, padding);

            // Row 1: Title & Values
            RelativeLayout row1 = new RelativeLayout(getContext());

            TextView tvTitle = new TextView(getContext());
            tvTitle.setId(View.generateViewId());
            tvTitle.setText(b.getCategoryName());
            tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
            tvTitle.setTextSize(13);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tvTitle.setLayoutParams(titleParams);

            TextView tvValue = new TextView(getContext());
            tvValue.setText(formatter.format(b.getSpentAmount()) + "đ / " + formatter.format(b.getLimitAmount()) + "đ");
            tvValue.setTextColor(getResources().getColor(R.color.text_secondary));
            tvValue.setTextSize(11);
            RelativeLayout.LayoutParams valParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            valParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            valParams.addRule(RelativeLayout.ALIGN_BASELINE, tvTitle.getId());
            tvValue.setLayoutParams(valParams);

            row1.addView(tvTitle);
            row1.addView(tvValue);

            rootLayout.addView(row1);

            // Row 2: Warning Alert (nếu vượt/gần vượt)
            String status = b.getStatus() != null ? b.getStatus() : "NORMAL";
            TextView tvAlert = null;
            int progressColor = getResources().getColor(R.color.accent_blue);

            if ("OVER_LIMIT".equals(status)) {
                tvAlert = new TextView(getContext());
                BigDecimal over = b.getSpentAmount().subtract(b.getLimitAmount());
                tvAlert.setText("❌ Vượt ngân sách +" + formatter.format(over) + "đ");
                tvAlert.setTextColor(getResources().getColor(R.color.crimson_expense));
                tvAlert.setTextSize(10);
                tvAlert.setTypeface(null, android.graphics.Typeface.BOLD);
                tvAlert.setPadding(0, 2, 0, 0);
                progressColor = getResources().getColor(R.color.crimson_expense);
            } else if ("NEAR_LIMIT".equals(status)) {
                tvAlert = new TextView(getContext());
                tvAlert.setText("⚠️ Sắp chạm hạn mức (" + b.getSpentPercent() + "%)");
                tvAlert.setTextColor(getResources().getColor(R.color.crimson_expense));
                tvAlert.setTextSize(10);
                tvAlert.setTypeface(null, android.graphics.Typeface.BOLD);
                tvAlert.setPadding(0, 2, 0, 0);
                progressColor = getResources().getColor(R.color.crimson_expense);
            }

            if (tvAlert != null) {
                rootLayout.addView(tvAlert);
            }

            // ProgressBar
            ProgressBar bar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            int percentVal = b.getSpentPercent() != null ? b.getSpentPercent().intValue() : 0;
            bar.setProgress(Math.min(percentVal, 100));
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(progressColor));
            bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.progress_track)));

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (4 * getResources().getDisplayMetrics().density)
            );
            barParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
            bar.setLayoutParams(barParams);

            rootLayout.addView(bar);

            card.addView(rootLayout);
            layoutBudgetsContainer.addView(card);
        }

        // Cập nhật text tổng quan ở Header
        tvBudgetSpendTotal.setText(formatter.format(totalSpent) + "đ");
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
            if (tvSavingsTotalCurrent != null) tvSavingsTotalCurrent.setText("0đ");
            if (tvSavingsTotalTarget != null) tvSavingsTotalTarget.setText("Mục tiêu: 0đ");
            if (tvSavingsTotalPercent != null) tvSavingsTotalPercent.setText("Đã đạt 0% mục tiêu chung");
            if (pbSavingsTotal != null) pbSavingsTotal.setProgress(0);
            if (layoutAiSurplusContainer != null) layoutAiSurplusContainer.setVisibility(View.GONE);
            layoutSavingsSuggestions.removeAllViews();
            return;
        }

        layoutSavingsSuggestions.removeAllViews();

        // ===== TÍNH TOÁN TỔNG HEADER ĐỘNG =====
        BigDecimal totalCurrent = BigDecimal.ZERO;
        BigDecimal totalTarget = BigDecimal.ZERO;
        for (SavingsGoal goal : goals) {
            totalCurrent = totalCurrent.add(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
            totalTarget = totalTarget.add(goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO);
        }

        if (tvSavingsTotalCurrent != null) tvSavingsTotalCurrent.setText(formatter.format(totalCurrent) + "đ");
        if (tvSavingsTotalTarget != null) tvSavingsTotalTarget.setText("Mục tiêu: " + formatter.format(totalTarget) + "đ");

        double totalProgressPct = 0;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            totalProgressPct = totalCurrent.multiply(new BigDecimal("100"))
                    .divide(totalTarget, 2, java.math.RoundingMode.HALF_UP).doubleValue();
        }
        if (tvSavingsTotalPercent != null)
            tvSavingsTotalPercent.setText(String.format("Đã đạt %.0f%% mục tiêu chung", totalProgressPct));
        if (pbSavingsTotal != null)
            pbSavingsTotal.setProgress((int) Math.min(totalProgressPct, 100));

        // ===== TÍNH TỔNG DÒNG DƯ GỢI Ý AI =====
        BigDecimal totalAllocated = BigDecimal.ZERO;
        for (com.example.smartexpense.models.SavingsSuggestion s : savingsSuggestionsMap.values()) {
            totalAllocated = totalAllocated.add(s.getAllocatedAmount() != null ? s.getAllocatedAmount() : BigDecimal.ZERO);
        }

        if (layoutAiSurplusContainer != null) {
            if (totalAllocated.compareTo(BigDecimal.ZERO) > 0) {
                layoutAiSurplusContainer.setVisibility(View.VISIBLE);
                if (tvAiSurplusAmount != null)
                    tvAiSurplusAmount.setText("💡 Dòng tiền nhàn rỗi dự kiến tháng này: +" + formatter.format(totalAllocated) + "đ");
            } else {
                layoutAiSurplusContainer.setVisibility(View.GONE);
            }
        }

        // ===== VẼ CÁC CARD GOAL ĐỘNG =====
        float dp = getResources().getDisplayMetrics().density;
        for (SavingsGoal goal : goals) {
            MaterialCardView card = new MaterialCardView(getContext());
            card.setRadius(16 * dp);
            card.setCardElevation(2 * dp);
            card.setCardBackgroundColor(getResources().getColor(R.color.surface_white));
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, (int) (12 * dp));
            card.setLayoutParams(cardParams);

            LinearLayout rootLayout = new LinearLayout(getContext());
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * dp);
            rootLayout.setPadding(padding, padding, padding, padding);

            // --- Row 1: Tên Goal + Phần trăm ---
            RelativeLayout row1 = new RelativeLayout(getContext());
            TextView tvTitle = new TextView(getContext());
            tvTitle.setId(View.generateViewId());
            String goalName = goal.getGoalName() != null && !goal.getGoalName().isEmpty()
                    ? goal.getGoalName()
                    : (goal.getGoalId() == 1 ? "Tích Lũy Mua Laptop Workstation"
                        : goal.getGoalId() == 2 ? "Đi Du Lịch Phú Quốc"
                        : goal.getGoalId() == 3 ? "Quỹ Dự Phòng Khẩn Cấp"
                        : "Mục Tiêu Tích Lũy #" + goal.getGoalId());
            tvTitle.setText(goalName);
            tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            titleParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvTitle.setLayoutParams(titleParams);

            double progressPct = 0;
            BigDecimal currentAmt = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
            BigDecimal targetAmt = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;
            if (targetAmt.compareTo(BigDecimal.ZERO) > 0) {
                progressPct = currentAmt.multiply(new BigDecimal("100"))
                        .divide(targetAmt, 2, java.math.RoundingMode.HALF_UP).doubleValue();
            }
            boolean isCompleted = currentAmt.compareTo(targetAmt) >= 0;

            TextView tvPct = new TextView(getContext());
            tvPct.setId(View.generateViewId());
            tvPct.setText(isCompleted ? "100%" : String.format("%.0f%%", progressPct));
            tvPct.setTextColor(isCompleted
                    ? getResources().getColor(R.color.emerald_income)
                    : getResources().getColor(R.color.accent_blue));
            tvPct.setTextSize(16);
            tvPct.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams pctParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            pctParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            pctParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvPct.setLayoutParams(pctParams);

            row1.addView(tvTitle);
            row1.addView(tvPct);

            // --- Row 2: Hạn chót ---
            TextView tvSub = new TextView(getContext());
            tvSub.setText("Hạn chót: " + (goal.getDeadline() != null ? goal.getDeadline() : "Chưa đặt"));
            tvSub.setTextColor(getResources().getColor(R.color.text_secondary));
            tvSub.setTextSize(11);
            tvSub.setPadding(0, 4, 0, 6);

            // --- Số tiền đã tích lũy ---
            TextView tvVal = new TextView(getContext());
            tvVal.setText("Đã tích lũy: " + formatter.format(currentAmt) + "đ / " + formatter.format(targetAmt) + "đ");
            tvVal.setTextColor(getResources().getColor(R.color.text_secondary));
            tvVal.setTextSize(12);
            tvVal.setPadding(0, 0, 0, 8);

            // --- Progress Bar ---
            ProgressBar bar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress((int) Math.min(progressPct, 100));
            int barColor = isCompleted
                    ? getResources().getColor(R.color.emerald_income)
                    : getResources().getColor(R.color.accent_blue);
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(barColor));
            bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.progress_track)));
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (4 * dp));
            barParams.setMargins(0, 0, 0, (int) (12 * dp));
            bar.setLayoutParams(barParams);

            // --- Row 3: Gợi ý AI + Nút Hành động ---
            RelativeLayout row3 = new RelativeLayout(getContext());

            // Tip Text
            final com.example.smartexpense.models.SavingsSuggestion sug = savingsSuggestionsMap.get(goal.getGoalId());
            final BigDecimal suggestedAmount = sug != null ? sug.getAllocatedAmount() : null;

            TextView tvTip = new TextView(getContext());
            tvTip.setTextSize(10);
            tvTip.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams tipParams = new RelativeLayout.LayoutParams(
                    (int) (210 * dp), RelativeLayout.LayoutParams.WRAP_CONTENT);
            tipParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tipParams.addRule(RelativeLayout.CENTER_VERTICAL);
            tvTip.setLayoutParams(tipParams);

            if (isCompleted) {
                tvTip.setText("🎉 Bạn đã đạt mục tiêu này!");
                tvTip.setTextColor(getResources().getColor(R.color.emerald_income));
            } else if (sug != null && suggestedAmount != null && suggestedAmount.compareTo(BigDecimal.ZERO) > 0) {
                String pctStr = sug.getAllocationPercent() != null ? sug.getAllocationPercent().toString() + "%" : "";
                tvTip.setText(String.format("💡 Gợi ý trích: +%sđ/tháng (%s dòng dư)",
                        formatter.format(suggestedAmount), pctStr));
                tvTip.setTextColor(getResources().getColor(R.color.accent_blue));
            } else {
                tvTip.setText("💡 Trích lũy thông minh được bật");
                tvTip.setTextColor(getResources().getColor(R.color.text_secondary));
            }

            row3.addView(tvTip);

            final Integer finalGoalId = goal.getGoalId();
            if (isCompleted) {
                // Badge Đã Hoàn Thành thay thế nút Nạp Quỹ
                TextView tvCompleted = new TextView(getContext());
                tvCompleted.setText("✅ ĐÃ HOÀN THÀNH");
                tvCompleted.setTextColor(getResources().getColor(R.color.emerald_income));
                tvCompleted.setTextSize(11);
                tvCompleted.setTypeface(null, android.graphics.Typeface.BOLD);
                RelativeLayout.LayoutParams compParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                compParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                compParams.addRule(RelativeLayout.CENTER_VERTICAL);
                tvCompleted.setLayoutParams(compParams);
                row3.addView(tvCompleted);
            } else {
                // Nút Nạp Quỹ (tự điền sẵn số tiền gợi ý)
                MaterialButton btnAddFunds = new MaterialButton(getContext());
                btnAddFunds.setText("Nạp Quỹ");
                btnAddFunds.setTextColor(getResources().getColor(R.color.surface_white));
                btnAddFunds.setTextSize(10);
                btnAddFunds.setTypeface(null, android.graphics.Typeface.BOLD);
                btnAddFunds.setPadding(0, 0, 0, 0);
                btnAddFunds.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(R.color.primary)));
                btnAddFunds.setCornerRadius((int) (8 * dp));
                RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                        (int) (90 * dp), (int) (36 * dp));
                btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
                btnAddFunds.setLayoutParams(btnParams);
                btnAddFunds.setOnClickListener(v -> showAddFundsDialog(finalGoalId, suggestedAmount));
                row3.addView(btnAddFunds);
            }

            rootLayout.addView(row1);
            rootLayout.addView(tvSub);
            rootLayout.addView(tvVal);
            rootLayout.addView(bar);
            rootLayout.addView(row3);

            card.addView(rootLayout);
            layoutSavingsSuggestions.addView(card);
        }
    }

    private void showAddFundsDialog(Integer goalId, BigDecimal suggestedAmount) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(suggestedAmount != null
                ? "Nạp quỹ tích lũy (AI gợi ý)"
                : "Nạp thêm vào quỹ tích lũy");

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(margin, margin / 2, margin, 0);

        // Nếu có gợi ý AI, hiển thị dòng chú thích
        if (suggestedAmount != null && suggestedAmount.compareTo(BigDecimal.ZERO) > 0) {
            TextView tvHint = new TextView(getContext());
            tvHint.setText("💡 AI gợi ý nạp: +" + formatter.format(suggestedAmount) + "đ tháng này");
            tvHint.setTextColor(getResources().getColor(R.color.accent_blue));
            tvHint.setTextSize(12);
            tvHint.setTypeface(null, android.graphics.Typeface.BOLD);
            tvHint.setPadding(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
            container.addView(tvHint);
        }

        TextInputLayout layoutInput = new TextInputLayout(getContext());
        layoutInput.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutInput.setBoxStrokeColor(getResources().getColor(R.color.accent_blue));
        layoutInput.setHint(suggestedAmount != null ? "Số tiền nạp (đã điền sẵn gợi ý AI)" : "Số tiền nạp (VND)");

        TextInputEditText etAmount = new TextInputEditText(getContext());
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        etAmount.setTextSize(14);

        // Tự động điền sẵn số tiền gợi ý
        if (suggestedAmount != null && suggestedAmount.compareTo(BigDecimal.ZERO) > 0) {
            etAmount.setText(suggestedAmount.setScale(0, java.math.RoundingMode.HALF_UP).toString());
            etAmount.setSelection(etAmount.getText() != null ? etAmount.getText().length() : 0);
        }

        layoutInput.addView(etAmount);
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
                        Toast.makeText(getContext(), "🎉 Đã nạp +" + formatter.format(amount) + "đ vào quỹ!", Toast.LENGTH_SHORT).show();
                        loadSavingsSuggestions(); // Refresh cả gợi ý lẫn Goals
                    } else {
                        Toast.makeText(getContext(), "Nạp quỹ thất bại, thử lại!", Toast.LENGTH_SHORT).show();
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
        builder.setTitle("🎯 Thiết lập mục tiêu tích lũy mới");

        LinearLayout layoutContainer = new LinearLayout(getContext());
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layoutContainer.setPadding(padding, padding, padding, padding);

        // Input Goal Name
        TextInputLayout layoutName = new TextInputLayout(getContext());
        layoutName.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutName.setHint("Tên mục tiêu (Ví dụ: Mua Laptop, Du Lịch...)");
        TextInputEditText etName = new TextInputEditText(getContext());
        etName.setInputType(InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        layoutName.addView(etName);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
        layoutName.setLayoutParams(nameParams);
        layoutContainer.addView(layoutName);

        // Input Goal Target Amount
        TextInputLayout layoutTar = new TextInputLayout(getContext());
        layoutTar.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutTar.setHint("Số tiền mục tiêu tích lũy (VND)");
        TextInputEditText etTar = new TextInputEditText(getContext());
        etTar.setInputType(InputType.TYPE_CLASS_NUMBER);
        layoutTar.addView(etTar);
        layoutContainer.addView(layoutTar);

        // Input Deadline (optional)
        TextInputLayout layoutDl = new TextInputLayout(getContext());
        layoutDl.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutDl.setHint("Hạn chót (Định dạng: YYYY-MM-DD, tùy chọn)");
        TextInputEditText etDl = new TextInputEditText(getContext());
        etDl.setInputType(InputType.TYPE_CLASS_DATETIME);
        layoutDl.addView(etDl);
        LinearLayout.LayoutParams dlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dlParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
        layoutDl.setLayoutParams(dlParams);
        layoutContainer.addView(layoutDl);

        builder.setView(layoutContainer);

        builder.setPositiveButton("Thiết Lập Ngay", (dialog, which) -> {
            String nameStr = etName.getText() != null ? etName.getText().toString().trim() : "";
            String tarStr = etTar.getText() != null ? etTar.getText().toString().trim() : "";
            String dlStr = etDl.getText() != null ? etDl.getText().toString().trim() : "";

            if (tarStr.isEmpty()) {
                Toast.makeText(getContext(), "Số tiền mục tiêu không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            SavingsGoal goal = new SavingsGoal();
            goal.setUserId(CURRENT_USER_ID);
            goal.setGoalName(nameStr.isEmpty() ? "Mục Tiêu Tích Lũy" : nameStr);
            goal.setTargetAmount(new BigDecimal(tarStr));
            if (!dlStr.isEmpty()) {
                goal.setDeadline(dlStr);
            }

            ApiClient.getApiService().createSavingsGoal(goal).enqueue(new Callback<SavingsGoal>() {
                @Override
                public void onResponse(Call<SavingsGoal> call, Response<SavingsGoal> response) {
                    if (response.isSuccessful()) {
                        String gName = nameStr.isEmpty() ? "Mục tiêu" : nameStr;
                        Toast.makeText(getContext(), "🎉 Đã tạo mục tiêu \"" + gName + "\" thành công!", Toast.LENGTH_SHORT).show();
                        loadSavingsSuggestions(); // Refresh cả suggestions + goals
                    } else {
                        Toast.makeText(getContext(), "Tạo mục tiêu thất bại!", Toast.LENGTH_SHORT).show();
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

    private void loadSavingsSuggestions() {
        ApiClient.getApiService().getSavingsSuggestions(CURRENT_USER_ID).enqueue(new Callback<List<com.example.smartexpense.models.SavingsSuggestion>>() {
            @Override
            public void onResponse(Call<List<com.example.smartexpense.models.SavingsSuggestion>> call, Response<List<com.example.smartexpense.models.SavingsSuggestion>> response) {
                savingsSuggestionsMap.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (com.example.smartexpense.models.SavingsSuggestion suggestion : response.body()) {
                        savingsSuggestionsMap.put(suggestion.getGoalId(), suggestion);
                    }
                }
                loadSavingsGoals();
            }

            @Override
            public void onFailure(Call<List<com.example.smartexpense.models.SavingsSuggestion>> call, Throwable t) {
                loadSavingsGoals();
            }
        });
    }

    private void setupAiAutoAllocateAction() {
        if (btnAiAutoAllocate == null) return;
        btnAiAutoAllocate.setOnClickListener(v -> {
            if (savingsSuggestionsMap.isEmpty()) return;

            BigDecimal surplusTemp = BigDecimal.ZERO;
            for (com.example.smartexpense.models.SavingsSuggestion s : savingsSuggestionsMap.values()) {
                surplusTemp = surplusTemp.add(s.getAllocatedAmount() != null ? s.getAllocatedAmount() : BigDecimal.ZERO);
            }
            final BigDecimal totalSurplus = surplusTemp; // effectively final để dùng trong lambda

            if (totalSurplus.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getContext(), "Không có dòng tiền nhàn rỗi để phân bổ!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Tối ưu hóa dòng tiền bằng AI 💡");
            builder.setMessage(String.format("Bạn có muốn tự động trích phân bổ tổng cộng +%sđ dòng tiền nhàn rỗi dự kiến vào các mục tiêu tích lũy theo tỷ lệ tối ưu của AI không?", formatter.format(totalSurplus)));

            builder.setPositiveButton("Phân Bổ Ngay", (dialog, which) -> {
                AlertDialog progressDialog = new AlertDialog.Builder(getContext())
                        .setMessage("AI đang tự động phân bổ dòng tiền nhàn rỗi của bạn...")
                        .setCancelable(false)
                        .show();

                final List<com.example.smartexpense.models.SavingsSuggestion> suggestions = new ArrayList<>(savingsSuggestionsMap.values());
                final int totalCalls = suggestions.size();
                final int[] successCount = {0};
                final int[] completedCalls = {0};
                final BigDecimal finalTotalSurplus = totalSurplus;

                for (com.example.smartexpense.models.SavingsSuggestion sug : suggestions) {
                    if (sug.getAllocatedAmount() == null || sug.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        completedCalls[0]++;
                        if (completedCalls[0] == totalCalls) {
                            progressDialog.dismiss();
                            finishAllocation(successCount[0], finalTotalSurplus);
                        }
                        continue;
                    }

                    ApiClient.getApiService().addFundsToGoal(sug.getGoalId(), sug.getAllocatedAmount()).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                successCount[0]++;
                            }
                            completedCalls[0]++;
                            if (completedCalls[0] == totalCalls) {
                                progressDialog.dismiss();
                                finishAllocation(successCount[0], finalTotalSurplus);
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            completedCalls[0]++;
                            if (completedCalls[0] == totalCalls) {
                                progressDialog.dismiss();
                                finishAllocation(successCount[0], finalTotalSurplus);
                            }
                        }
                    });
                }
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void finishAllocation(int successCount, BigDecimal totalAllocated) {
        if (successCount > 0) {
            Toast.makeText(getContext(), String.format("Chúc mừng! AI đã phân bổ thành công +%sđ vào các quỹ tích lũy!", formatter.format(totalAllocated)), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Không phân bổ được mục nào. Vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
        }
        loadSavingsSuggestions(); // Refresh everything
    }
}
