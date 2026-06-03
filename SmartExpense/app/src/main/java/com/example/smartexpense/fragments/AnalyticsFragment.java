package com.example.smartexpense.fragments;

import android.app.AlertDialog;
import android.content.Context;
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
import android.app.DatePickerDialog;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.example.smartexpense.models.Category;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
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

    // Health score views (Tab 1)
    private TextView tvHealthScore, tvHealthStatus, tvHealthDesc;

    // Global budget views (Tab 2)
    private TextView tvBudgetGlobalDesc;
    private ProgressBar pbBudgetGlobal;

    private TextView tvAiSmartInsight;
    private TextView tvWeeklyComparison;
    private List<BudgetDetailResponse> lastBudgets = new ArrayList<>();
    private List<SavingsGoal> lastGoals = new ArrayList<>();

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final List<Category> categoriesList = new ArrayList<>();

    private int getUserId() {
        if (getActivity() == null) return 1;
        return getActivity()
                .getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
                .getInt("user_id", 1);
    }

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

        // Find Health score components (Tab 1)
        tvHealthScore = view.findViewById(R.id.tv_health_score);
        tvHealthStatus = view.findViewById(R.id.tv_health_status);
        tvHealthDesc = view.findViewById(R.id.tv_health_desc);

        // Find Global budget components (Tab 2)
        tvBudgetGlobalDesc = view.findViewById(R.id.tv_budget_global_desc);
        pbBudgetGlobal = view.findViewById(R.id.pb_budget_global);
        tvAiSmartInsight = view.findViewById(R.id.tv_ai_smart_insight);
        tvWeeklyComparison = view.findViewById(R.id.tv_weekly_comparison);
        
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
        java.time.LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startStr = startOfLastWeek.format(dtf);
        String endStr = endOfWeek.format(dtf);

        ApiClient.getApiService().filterTransactions(getUserId(), startStr, endStr, null, null, "EXPENSE")
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

        BigDecimal thisWeekTotal = BigDecimal.ZERO;
        BigDecimal lastWeekTotal = BigDecimal.ZERO;

        java.time.format.DateTimeFormatter parser = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (com.example.smartexpense.models.Transaction t : transactions) {
            if (t.getTransactionDate() != null && t.getAmount() != null) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(t.getTransactionDate(), parser);
                    if (!date.isBefore(startOfWeek)) {
                        thisWeekTotal = thisWeekTotal.add(t.getAmount());
                        int dayIndex = date.getDayOfWeek().getValue() - 1; // MONDAY is 1, SUNDAY is 7 -> index 0 to 6
                        if (dayIndex >= 0 && dayIndex < 7) {
                            daySums[dayIndex] = daySums[dayIndex].add(t.getAmount());
                        }
                    } else {
                        lastWeekTotal = lastWeekTotal.add(t.getAmount());
                    }
                } catch (Exception ignored) {}
            }
        }

        // Update comparison label
        if (tvWeeklyComparison != null) {
            if (lastWeekTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = thisWeekTotal.subtract(lastWeekTotal);
                double pct = diff.multiply(new BigDecimal("100"))
                        .divide(lastWeekTotal, 2, java.math.RoundingMode.HALF_UP).doubleValue();
                if (pct >= 0) {
                    tvWeeklyComparison.setText("+" + String.format(Locale.US, "%.1f", pct) + "% so với tuần trước");
                    tvWeeklyComparison.setTextColor(getResources().getColor(R.color.crimson_expense));
                } else {
                    tvWeeklyComparison.setText(String.format(Locale.US, "%.1f", pct) + "% so với tuần trước");
                    tvWeeklyComparison.setTextColor(getResources().getColor(R.color.emerald_income));
                }
            } else {
                if (thisWeekTotal.compareTo(BigDecimal.ZERO) > 0) {
                    tvWeeklyComparison.setText("Tuần trước không chi tiêu");
                } else {
                    tvWeeklyComparison.setText("Không có chi tiêu tuần này");
                }
                tvWeeklyComparison.setTextColor(getResources().getColor(R.color.text_secondary));
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
            ApiClient.getApiService().checkBurnRate(getUserId(), 1).enqueue(new Callback<BurnRateResponse>() {
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
        ApiClient.getApiService().getCategories(getUserId()).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriesList.clear();
                    for (Category c : response.body()) {
                        if ("EXPENSE".equalsIgnoreCase(c.getType())) {
                            categoriesList.add(c);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });

        ApiClient.getApiService().getBudgetDetails(getUserId()).enqueue(new Callback<List<BudgetDetailResponse>>() {
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

        lastBudgets = budgets;
        updateAiSmartInsight(lastBudgets, lastGoals);

        layoutBudgetsContainer.removeAllViews();

        if (budgets == null || budgets.isEmpty()) {
            tvBudgetSpendTotal.setText("0đ");
            tvBudgetLimitTotal.setText(" / 0đ");
            if (tvHealthScore != null) tvHealthScore.setText("--");
            if (tvHealthStatus != null) {
                tvHealthStatus.setText("Trạng thái: Chưa có dữ liệu");
                tvHealthStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            }
            if (tvHealthDesc != null) {
                tvHealthDesc.setText("Vui lòng thiết lập ngân sách chi tiêu ở tab Ngân Sách để AI đánh giá sức khỏe tài chính.");
            }
            if (tvBudgetGlobalDesc != null) tvBudgetGlobalDesc.setText("Chưa có ngân sách được thiết lập");
            if (pbBudgetGlobal != null) pbBudgetGlobal.setProgress(0);
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

        // Dynamic Global Progress calculation
        int globalPercent = 0;
        BigDecimal remaining = totalLimit.subtract(totalSpent);
        if (totalLimit.compareTo(BigDecimal.ZERO) > 0) {
            globalPercent = totalSpent.multiply(new BigDecimal("100"))
                    .divide(totalLimit, 0, java.math.RoundingMode.HALF_UP).intValue();
        }

        if (tvBudgetGlobalDesc != null) {
            if (remaining.compareTo(BigDecimal.ZERO) >= 0) {
                tvBudgetGlobalDesc.setText("Đã chi tiêu " + globalPercent + "% ngân sách • Còn lại " + formatter.format(remaining) + "đ");
            } else {
                BigDecimal overSpent = remaining.negate();
                tvBudgetGlobalDesc.setText("Đã chi tiêu " + globalPercent + "% ngân sách • Vượt hạn mức " + formatter.format(overSpent) + "đ");
            }
        }
        if (pbBudgetGlobal != null) {
            pbBudgetGlobal.setProgress(Math.min(globalPercent, 100));
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                pbBudgetGlobal.setProgressTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.crimson_expense)));
            } else {
                pbBudgetGlobal.setProgressTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.secondary)));
            }
        }

        // Dynamic Health Score calculation
        int baseScore = 100;
        for (BudgetDetailResponse b : budgets) {
            String status = b.getStatus() != null ? b.getStatus() : "NORMAL";
            if ("OVER_LIMIT".equals(status)) {
                baseScore -= 15;
            } else if ("NEAR_LIMIT".equals(status)) {
                baseScore -= 7;
            } else if (b.getSpentPercent() != null && b.getSpentPercent().doubleValue() > 50.0) {
                baseScore -= 2;
            }
        }

        // Deduct points if spending speed is too fast compared to time elapsed in the month
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        double timePercent = ((double) dayOfMonth / maxDays) * 100.0;
        
        if (totalLimit.compareTo(BigDecimal.ZERO) > 0) {
            double globalPercentVal = totalSpent.multiply(new BigDecimal("100"))
                    .divide(totalLimit, 2, java.math.RoundingMode.HALF_UP).doubleValue();
            
            // Nếu phần trăm đã chi vượt quá tiến độ thời gian + 30%: trừ 25 điểm
            if (globalPercentVal > timePercent + 30.0) {
                baseScore -= 25;
            }
            // Nếu phần trăm đã chi vượt quá tiến độ thời gian + 15%: trừ 10 điểm
            else if (globalPercentVal > timePercent + 15.0) {
                baseScore -= 10;
            }
        }

        final int finalScore = Math.max(30, Math.min(baseScore, 100));

        if (tvHealthScore != null) {
            tvHealthScore.setText(String.valueOf(finalScore));
        }

        if (tvHealthStatus != null && tvHealthDesc != null) {
            if (finalScore >= 90) {
                tvHealthStatus.setText("Trạng thái: Xuất sắc");
                tvHealthStatus.setTextColor(getResources().getColor(R.color.emerald_income));
                tvHealthDesc.setText("Thói quen chi tiêu cực kỳ tốt. Hãy tiếp tục duy trì nhé!");
            } else if (finalScore >= 80) {
                tvHealthStatus.setText("Trạng thái: Tốt");
                tvHealthStatus.setTextColor(getResources().getColor(R.color.emerald_income));
                tvHealthDesc.setText("Thói quen tích lũy của bạn tốt hơn phần lớn người dùng.");
            } else if (finalScore >= 65) {
                tvHealthStatus.setText("Trạng thái: Trung bình");
                tvHealthStatus.setTextColor(getResources().getColor(R.color.accent_blue));
                tvHealthDesc.setText("Bạn đang chi tiêu khá sát giới hạn. Nên cân nhắc tiết giảm.");
            } else {
                tvHealthStatus.setText("Trạng thái: Cảnh báo");
                tvHealthStatus.setTextColor(getResources().getColor(R.color.crimson_expense));
                tvHealthDesc.setText("Bạn đã chi tiêu vượt quá giới hạn ngân sách nhiều hạng mục. Hãy thắt chặt chi tiêu!");
            }
        }
    }

    private void loadSavingsGoals() {
        ApiClient.getApiService().getSavingsGoals(getUserId()).enqueue(new Callback<List<SavingsGoal>>() {
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

        lastGoals = goals;
        updateAiSmartInsight(lastBudgets, lastGoals);

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
            ApiClient.getApiService().addFundsToGoal(goalId, amount, null).enqueue(new Callback<Map<String, Object>>() {
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

    private Category selectedBudgetCategory = null;

    private void showAddBudgetDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_budget, null);
        AutoCompleteTextView actCat = dialogView.findViewById(R.id.act_budget_category);
        TextInputEditText etAmt = dialogView.findViewById(R.id.et_budget_amount);

        selectedBudgetCategory = null;

        // Populate Categories Dropdown
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoriesList
        );
        actCat.setAdapter(catAdapter);

        // Pre-select first category if available
        if (!categoriesList.isEmpty()) {
            selectedBudgetCategory = categoriesList.get(0);
            actCat.setText(selectedBudgetCategory.getName(), false);
        }

        actCat.setOnItemClickListener((parent, view, position, id) -> {
            selectedBudgetCategory = categoriesList.get(position);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        builder.setPositiveButton("Thiết Lập", (dialog, which) -> {
            String amtStr = etAmt.getText() != null ? etAmt.getText().toString().trim() : "";

            if (selectedBudgetCategory == null || selectedBudgetCategory.getCategoryId() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn hạng mục chi tiêu!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amtStr.isEmpty()) {
                Toast.makeText(getContext(), "Hạn mức không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amtStr);
            } catch (Exception ignored) {
                Toast.makeText(getContext(), "Hạn mức không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getContext(), "Hạn mức phải > 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            Budget budget = new Budget();
            budget.setUserId(getUserId());
            budget.setCategoryId(selectedBudgetCategory.getCategoryId());
            budget.setAmount(amount);

            ApiClient.getApiService().createBudget(budget).enqueue(new Callback<Budget>() {
                @Override
                public void onResponse(Call<Budget> call, Response<Budget> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Thiết lập ngân sách thành công!", Toast.LENGTH_SHORT).show();
                        loadBudgets(); // Refresh list
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thiết lập ngân sách!", Toast.LENGTH_SHORT).show();
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

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_goal_name);
        TextInputEditText etTarget = dialogView.findViewById(R.id.et_target_amount);
        TextInputEditText etInitial = dialogView.findViewById(R.id.et_initial_amount);
        TextInputEditText etDeadline = dialogView.findViewById(R.id.et_deadline);

        etDeadline.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        etDeadline.setText(sdf.format(picked.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        builder.setPositiveButton("Thiết Lập Ngay", (dialog, which) -> {
            String nameStr = etName.getText() != null ? etName.getText().toString().trim() : "";
            String tarStr = etTarget.getText() != null ? etTarget.getText().toString().trim() : "";
            String initStr = etInitial.getText() != null ? etInitial.getText().toString().trim() : "";
            String dlStr = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";

            if (tarStr.isEmpty()) {
                Toast.makeText(getContext(), "Số tiền mục tiêu không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal targetAmt;
            try {
                targetAmt = new BigDecimal(tarStr);
            } catch (Exception ignored) {
                Toast.makeText(getContext(), "Số tiền mục tiêu không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal initialAmt = BigDecimal.ZERO;
            if (!initStr.isEmpty()) {
                try {
                    initialAmt = new BigDecimal(initStr);
                } catch (Exception ignored) {}
            }

            SavingsGoal goal = new SavingsGoal();
            goal.setUserId(getUserId());
            goal.setGoalName(nameStr.isEmpty() ? "Mục Tiêu Tích Lũy" : nameStr);
            goal.setTargetAmount(targetAmt);
            goal.setCurrentAmount(initialAmt);
            if (!dlStr.isEmpty()) {
                goal.setDeadline(dlStr);
            }
            goal.setStatus("IN_PROGRESS");

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
        ApiClient.getApiService().getSavingsSuggestions(getUserId()).enqueue(new Callback<List<com.example.smartexpense.models.SavingsSuggestion>>() {
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

                    ApiClient.getApiService().addFundsToGoal(sug.getGoalId(), sug.getAllocatedAmount(), null).enqueue(new Callback<Map<String, Object>>() {
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

    private void updateAiSmartInsight(List<BudgetDetailResponse> budgets, List<SavingsGoal> goals) {
        if (tvAiSmartInsight == null) return;

        StringBuilder insight = new StringBuilder("\"");

        // Calculate total budget limits and spent
        BigDecimal totalLimit = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        List<String> overBudgets = new ArrayList<>();
        List<String> nearBudgets = new ArrayList<>();
        if (budgets != null) {
            for (BudgetDetailResponse b : budgets) {
                totalLimit = totalLimit.add(b.getLimitAmount() != null ? b.getLimitAmount() : BigDecimal.ZERO);
                totalSpent = totalSpent.add(b.getSpentAmount() != null ? b.getSpentAmount() : BigDecimal.ZERO);
                if ("OVER_LIMIT".equals(b.getStatus())) {
                    overBudgets.add(b.getCategoryName());
                } else if ("NEAR_LIMIT".equals(b.getStatus())) {
                    nearBudgets.add(b.getCategoryName());
                }
            }
        }

        // Check if all goals are completed
        boolean allGoalsCompleted = true;
        if (goals != null && !goals.isEmpty()) {
            for (SavingsGoal g : goals) {
                BigDecimal target = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
                BigDecimal current = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
                if (current.compareTo(target) < 0) {
                    allGoalsCompleted = false;
                    break;
                }
            }
        } else {
            allGoalsCompleted = false;
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        double timePercent = ((double) dayOfMonth / maxDays) * 100.0;

        double spentPercent = 0.0;
        if (totalLimit.compareTo(BigDecimal.ZERO) > 0) {
            spentPercent = totalSpent.multiply(new BigDecimal("100"))
                    .divide(totalLimit, 2, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        if (!overBudgets.isEmpty()) {
            insight.append("⚠️ Cảnh báo chi tiêu: Bạn đã vượt hạn mức ngân sách ở các danh mục: ")
                   .append(String.join(", ", overBudgets))
                   .append(". Hãy cắt giảm chi tiêu khẩn cấp để đảm bảo an toàn tài chính!");
        } else if (!nearBudgets.isEmpty()) {
            insight.append("⚠️ Cảnh báo tốc độ: Bạn sắp chạm hạn mức ngân sách ở danh mục: ")
                   .append(String.join(", ", nearBudgets))
                   .append(". Nên hạn chế mua sắm thêm ở các mục này.");
        } else if (totalLimit.compareTo(BigDecimal.ZERO) > 0 && spentPercent > timePercent + 15.0) {
            insight.append(String.format("⚠️ Cảnh báo tốc độ chi tiêu: Bạn đã dùng %.0f%% ngân sách tháng này trong khi mới đi qua %.0f%% thời gian của tháng (%d/%d ngày). Tốc độ chi tiêu này là QUÁ NHANH!",
                    spentPercent, timePercent, dayOfMonth, maxDays));
        } else if (allGoalsCompleted) {
            insight.append("🎉 Chúc mừng! Bạn đã hoàn thành xuất sắc tất cả mục tiêu tích lũy đặt ra. Hãy tiếp tục thiết lập những mục tiêu mới tiếp theo nhé!");
        } else if (totalLimit.compareTo(BigDecimal.ZERO) > 0 && spentPercent < 30.0) {
            insight.append(String.format("🌟 Tiết kiệm vượt trội: Bạn mới chỉ dùng hết %.1f%% ngân sách tháng này. Kế hoạch kiểm soát chi tiêu của bạn đang vô cùng xuất sắc!",
                    spentPercent));
        } else if (goals != null && !goals.isEmpty()) {
            // Suggest allocating surplus money to goals
            BigDecimal totalAllocated = BigDecimal.ZERO;
            String mainGoalName = "";
            for (com.example.smartexpense.models.SavingsSuggestion s : savingsSuggestionsMap.values()) {
                totalAllocated = totalAllocated.add(s.getAllocatedAmount() != null ? s.getAllocatedAmount() : BigDecimal.ZERO);
            }
            for (SavingsGoal g : goals) {
                BigDecimal target = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
                BigDecimal current = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
                if (current.compareTo(target) < 0) {
                    mainGoalName = g.getGoalName();
                    break;
                }
            }

            if (totalAllocated.compareTo(BigDecimal.ZERO) > 0 && !mainGoalName.isEmpty()) {
                insight.append("💡 Gợi ý AI: Tháng này bạn dự kiến có khoảng +")
                       .append(formatter.format(totalAllocated))
                       .append("đ dòng tiền nhàn rỗi. Tích lũy vào mục tiêu '")
                       .append(mainGoalName)
                       .append("' sẽ giúp bạn hoàn thành kế hoạch sớm hơn!");
            } else {
                insight.append("💡 Lời khuyên tài chính: Bạn đang chi tiêu ổn định. Tuy nhiên dòng tiền nhàn rỗi tháng này khá thấp, hãy tối ưu thêm chi tiêu không thiết yếu nhé.");
            }
        } else {
            insight.append("💡 Lời khuyên tài chính: Bạn chưa thiết lập ngân sách và mục tiêu cá nhân. Hãy tạo ngay để AI bắt đầu phân tích hành vi và tối ưu điểm sức khỏe tài chính của bạn!");
        }

        insight.append("\"");
        tvAiSmartInsight.setText(insight.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
        loadSavingsSuggestions();
    }
}
