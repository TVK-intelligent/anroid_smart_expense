package com.example.smartexpense.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.R;
import com.example.smartexpense.adapters.TransactionAdapter;
import com.example.smartexpense.adapters.WalletAdapter;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.Category;
import com.example.smartexpense.models.Notification;
import com.example.smartexpense.models.Transaction;
import com.example.smartexpense.models.Wallet;
import com.example.smartexpense.models.dashboard.BudgetWarning;
import com.example.smartexpense.models.dashboard.DashboardResponse;
import com.example.smartexpense.models.dashboard.TopExpenseCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvGreetingName, tvTotalBalance, tvMonthlyIncome, tvMonthlyExpense;
    private TextView tvMonthlyNet;
    private TextView tvDashboardState;
    private View progressDashboard;
    private LinearLayout layoutGuidance;
    private TextView btnGuidanceAddWallet;
    private TextView btnGuidanceAddTransaction;

    private RecyclerView rvWallets;
    private View progressWallets;
    private TextView tvWalletsState;
    private TextView btnAddWallet;
    private WalletAdapter walletAdapter;
    private final List<Wallet> walletList = new ArrayList<>();

    private RecyclerView rvRecentTransactions;
    private TransactionAdapter recentTransactionAdapter;
    private final List<Transaction> recentTransactionList = new ArrayList<>();

    private TextView tvTopExpenseCategories;
    private TextView tvBudgetWarnings;

    private LinearLayout layoutAlerts;
    private TextView tvEmptyAlerts;

    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvGreetingName = view.findViewById(R.id.tv_greeting_name);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvMonthlyIncome = view.findViewById(R.id.tv_monthly_income);
        tvMonthlyExpense = view.findViewById(R.id.tv_monthly_expense);
        tvMonthlyNet = view.findViewById(R.id.tv_monthly_net);
        progressDashboard = view.findViewById(R.id.progress_dashboard);
        tvDashboardState = view.findViewById(R.id.tv_dashboard_state);
        layoutGuidance = view.findViewById(R.id.layout_dashboard_guidance);
        btnGuidanceAddWallet = view.findViewById(R.id.btn_guidance_add_wallet);
        btnGuidanceAddTransaction = view.findViewById(R.id.btn_guidance_add_transaction);

        rvWallets = view.findViewById(R.id.rv_wallets);
        progressWallets = view.findViewById(R.id.progress_wallets);
        tvWalletsState = view.findViewById(R.id.tv_wallets_state);
        layoutAlerts = view.findViewById(R.id.layout_alerts);
        tvEmptyAlerts = view.findViewById(R.id.tv_empty_alerts);
        btnAddWallet = view.findViewById(R.id.btn_add_wallet);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        tvTopExpenseCategories = view.findViewById(R.id.tv_top_expense_categories);
        tvBudgetWarnings = view.findViewById(R.id.tv_budget_warnings);

        rvWallets.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        walletAdapter = new WalletAdapter(walletList, new WalletAdapter.OnWalletActionListener() {
            @Override
            public void onWalletClick(Wallet wallet) {
                showEditWalletDialog(wallet);
            }

            @Override
            public void onWalletLongClick(Wallet wallet) {
                showWalletActionsDialog(wallet);
            }
        });
        rvWallets.setAdapter(walletAdapter);

        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        recentTransactionAdapter = new TransactionAdapter(recentTransactionList, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                // read-only on dashboard
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                // read-only on dashboard
            }
        });
        rvRecentTransactions.setAdapter(recentTransactionAdapter);

        if (btnAddWallet != null) {
            btnAddWallet.setOnClickListener(v -> showAddWalletDialog());
        }
        if (btnGuidanceAddWallet != null) {
            btnGuidanceAddWallet.setOnClickListener(v -> showAddWalletDialog());
        }
        if (btnGuidanceAddTransaction != null) {
            btnGuidanceAddTransaction.setOnClickListener(v -> {
                if (getActivity() == null) return;
                // Switch to AddTransactionFragment (same as bottom nav add)
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddTransactionFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        loadDashboardData();

        return view;
    }

    public void loadDashboardData() {
        if (getContext() == null) return;

        int userId = requireActivity()
                .getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
                .getInt("user_id", 1);

        setDashboardLoading(true);

        // Fetch categories dynamically to populate CategoryCache and refresh adapters
        ApiClient.getApiService().getCategories(userId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.smartexpense.api.CategoryCache.setCategories(response.body());
                    recentTransactionAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });

        ApiClient.getApiService().getDashboard(userId).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                setDashboardLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindDashboard(response.body());
                } else {
                    setDashboardError("Không tải được dashboard");
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                setDashboardLoading(false);
                setDashboardError("Lỗi mạng khi tải dashboard");
            }
        });

        // Get dynamic notifications / warnings
        ApiClient.getApiService().getNotifications(userId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notifications = response.body();
                    updateAlertsUI(notifications);
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {}
        });

        // Wallet list is still needed for the wallet carousel
        setWalletsLoading(true);
        ApiClient.getApiService().getWallets(userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                setWalletsLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    walletList.clear();
                    walletList.addAll(response.body());
                    walletAdapter.notifyDataSetChanged();
                    updateWalletsState();
                    updateGuidanceVisibility();
                } else {
                    setWalletsError("Không tải được danh sách ví");
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                setWalletsLoading(false);
                setWalletsError("Lỗi mạng khi tải ví");
            }
        });
    }

    private void bindDashboard(DashboardResponse dashboard) {
        BigDecimal totalBalance = dashboard.getTotalBalance() != null ? dashboard.getTotalBalance() : BigDecimal.ZERO;
        BigDecimal income = dashboard.getMonthlyIncome() != null ? dashboard.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal expense = dashboard.getMonthlyExpense() != null ? dashboard.getMonthlyExpense() : BigDecimal.ZERO;
        BigDecimal net = dashboard.getMonthlyNet() != null ? dashboard.getMonthlyNet() : income.subtract(expense);

        tvTotalBalance.setText(formatter.format(totalBalance) + "đ");
        tvMonthlyIncome.setText("+" + formatter.format(income) + "đ");
        tvMonthlyExpense.setText("-" + formatter.format(expense) + "đ");
        if (tvMonthlyNet != null) {
            String prefix = net.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            tvMonthlyNet.setText("Chênh lệch tháng này: " + prefix + formatter.format(net) + "đ");
        }

        recentTransactionList.clear();
        if (dashboard.getRecentTransactions() != null) {
            recentTransactionList.addAll(dashboard.getRecentTransactions());
        }
        recentTransactionAdapter.notifyDataSetChanged();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvTopExpenseCategories.setText(android.text.Html.fromHtml(formatTopExpenseCategories(dashboard.getTopExpenseCategories()), android.text.Html.FROM_HTML_MODE_LEGACY));
            tvBudgetWarnings.setText(android.text.Html.fromHtml(formatBudgetWarnings(dashboard.getBudgetWarnings()), android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTopExpenseCategories.setText(android.text.Html.fromHtml(formatTopExpenseCategories(dashboard.getTopExpenseCategories())));
            tvBudgetWarnings.setText(android.text.Html.fromHtml(formatBudgetWarnings(dashboard.getBudgetWarnings())));
        }

        updateGuidanceVisibility();
        if (tvDashboardState != null) tvDashboardState.setVisibility(View.GONE);
    }

    private String formatTopExpenseCategories(List<TopExpenseCategory> items) {
        if (items == null || items.isEmpty()) return "<i>Chưa có dữ liệu</i>";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            TopExpenseCategory c = items.get(i);
            String name = c.getCategoryName() != null ? c.getCategoryName() : "Hạng mục";
            BigDecimal spent = c.getTotalSpent() != null ? c.getTotalSpent() : BigDecimal.ZERO;
            sb.append("<font color='#0B1C30'><b>")
              .append(i + 1)
              .append(". ")
              .append(name)
              .append("</b></font>: <font color='#2170E4'><b>")
              .append(formatter.format(spent))
              .append("đ</b></font>");
            if (i < items.size() - 1) sb.append("<br/>");
        }
        return sb.toString();
    }

    private String formatBudgetWarnings(List<BudgetWarning> items) {
        if (items == null || items.isEmpty()) return "<font color='#27C38A'><b>✓ Không có cảnh báo hạn mức</b></font>";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            BudgetWarning w = items.get(i);
            String name = w.getCategoryName() != null ? w.getCategoryName() : "Hạng mục";
            BigDecimal percent = w.getSpentPercent() != null ? w.getSpentPercent() : BigDecimal.ZERO;
            String status = w.getStatus() != null ? w.getStatus() : "";

            String color = "#F59E0B"; // orange for warning
            String statusText = "Sắp chạm hạn mức";
            if ("OVER_LIMIT".equalsIgnoreCase(status) || percent.compareTo(new BigDecimal("100")) >= 0) {
                color = "#BA1A1A"; // crimson for over limit
                statusText = "Vượt ngân sách";
            }

            sb.append("<font color='").append(color).append("'><b>• ").append(name).append("</b>: ")
              .append(percent.setScale(0, RoundingMode.HALF_UP)).append("% (")
              .append(statusText).append(")</font>");
            if (i < items.size() - 1) sb.append("<br/>");
        }
        return sb.toString();
    }

    private void setDashboardLoading(boolean loading) {
        if (progressDashboard == null) return;
        progressDashboard.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (tvDashboardState != null) tvDashboardState.setVisibility(View.GONE);
        if (layoutGuidance != null) layoutGuidance.setVisibility(View.GONE);
    }

    private void setDashboardError(String message) {
        if (tvDashboardState == null) return;
        tvDashboardState.setText(message);
        tvDashboardState.setVisibility(View.VISIBLE);
        if (layoutGuidance != null) layoutGuidance.setVisibility(View.GONE);
    }

    private void updateGuidanceVisibility() {
        if (layoutGuidance == null) return;
        boolean noWallet = walletList.isEmpty();
        boolean noTransactions = recentTransactionList.isEmpty();
        layoutGuidance.setVisibility((noWallet || noTransactions) ? View.VISIBLE : View.GONE);
    }

    private void updateAlertsUI(List<Notification> notifications) {
        if (getContext() == null || layoutAlerts == null) return;

        // Clear previous views
        layoutAlerts.removeAllViews();

        if (notifications.isEmpty()) {
            // Re-create empty placeholder since removeAllViews detached it
            TextView emptyView = new TextView(getContext());
            emptyView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (72 * getResources().getDisplayMetrics().density)));
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setText("Không có cảnh báo nào. Hệ thống đang ổn định.");
            emptyView.setTextColor(getResources().getColor(R.color.text_secondary));
            emptyView.setTextSize(12);
            layoutAlerts.addView(emptyView);
            return;
        }

        // Get set of already shown notification IDs from SharedPreferences to avoid duplicate alerts
        android.content.SharedPreferences sp = requireActivity().getSharedPreferences("smart_expense_prefs", android.content.Context.MODE_PRIVATE);
        String shownIdsStr = sp.getString("shown_notification_ids", "");
        Set<String> shownIds = new HashSet<>(Arrays.asList(shownIdsStr.split(",")));
        boolean hasNewAlert = false;
        StringBuilder newAlertIds = new StringBuilder(shownIdsStr);

        // Dynamically add up to 3 alerts in 72dp lists
        int count = 0;
        for (Notification noti : notifications) {
            if (count >= 3) break;
            count++;

            String notiTitle = noti.getTitle() != null ? noti.getTitle() : "";
            boolean isAnomaly = notiTitle.toLowerCase().contains("bất thường");

            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, layoutAlerts, false);
            
            ImageView ivIcon = item.findViewById(R.id.iv_trans_icon);
            TextView tvTitle = item.findViewById(R.id.tv_trans_category);
            TextView tvContent = item.findViewById(R.id.tv_trans_note);
            TextView tvLabel = item.findViewById(R.id.tv_trans_amount);

            ivIcon.setImageResource(isAnomaly ? android.R.drawable.stat_sys_warning : android.R.drawable.ic_menu_info_details);
            ivIcon.setColorFilter(getResources().getColor(isAnomaly ? R.color.crimson_expense : R.color.amber_warning));

            tvTitle.setText(noti.getTitle());
            tvContent.setText(noti.getContent());
            tvLabel.setText(isAnomaly ? "ALERT" : "SPEED");
            tvLabel.setTextColor(getResources().getColor(isAnomaly ? R.color.crimson_expense : R.color.amber_warning));

            layoutAlerts.addView(item);

            // Trigger Push Notification for new unread notifications
            if (noti.getNotificationId() != null) {
                String idStr = String.valueOf(noti.getNotificationId());
                if (!shownIds.contains(idStr)) {
                    showPushNotification(noti.getTitle(), noti.getContent());
                    if (newAlertIds.length() > 0) newAlertIds.append(",");
                    newAlertIds.append(idStr);
                    hasNewAlert = true;
                }
            }
        }

        if (hasNewAlert) {
            sp.edit().putString("shown_notification_ids", newAlertIds.toString()).apply();
        }
    }

    private void showPushNotification(String title, String content) {
        if (getContext() == null) return;

        String channelId = "smart_expense_alerts";
        String channelName = "SmartExpense Alerts";
        
        android.app.NotificationManager notificationManager = 
                (android.app.NotificationManager) getContext().getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Cảnh báo vượt hạn mức chi tiêu & bất thường");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        androidx.core.app.NotificationCompat.Builder builder = 
                new androidx.core.app.NotificationCompat.Builder(getContext(), channelId)
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle().bigText(content))
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void updateCardPreview(String name, String type, String balanceStr, RelativeLayout layoutBg, TextView tvType, TextView tvName, TextView tvBalance) {
        if (tvName != null) {
            tvName.setText(name.isEmpty() ? "Tên Ví Tài Khoản" : name);
        }
        if (tvType != null) {
            tvType.setText(type.isEmpty() ? "BANK ACCOUNT" : type.toUpperCase());
        }
        if (tvBalance != null) {
            if (balanceStr.isEmpty()) {
                tvBalance.setText("0đ");
            } else {
                try {
                    BigDecimal val = new BigDecimal(balanceStr);
                    tvBalance.setText(formatter.format(val) + "đ");
                } catch (Exception e) {
                    tvBalance.setText("0đ");
                }
            }
        }
        if (layoutBg != null) {
            String cleanType = type.toLowerCase().trim();
            int bgResId = R.drawable.grad_wallet_default;
            if (cleanType.contains("bank")) {
                bgResId = R.drawable.grad_wallet_bank;
            } else if (cleanType.contains("credit")) {
                bgResId = R.drawable.grad_wallet_credit;
            } else if (cleanType.contains("cash") || cleanType.contains("tiền mặt")) {
                bgResId = R.drawable.grad_wallet_cash;
            } else if (cleanType.contains("e-wallet") || cleanType.contains("ewallet") || cleanType.contains("ví") || cleanType.contains("momo") || cleanType.contains("pay")) {
                bgResId = R.drawable.grad_wallet_ewallet;
            }
            layoutBg.setBackgroundResource(bgResId);
        }
    }

    private void showAddWalletDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wallet_form, null);
        
        RelativeLayout layoutPreviewBg = dialogView.findViewById(R.id.layout_preview_bg);
        TextView tvPreviewType = dialogView.findViewById(R.id.tv_preview_wallet_type);
        TextView tvPreviewName = dialogView.findViewById(R.id.tv_preview_wallet_name);
        TextView tvPreviewBalance = dialogView.findViewById(R.id.tv_preview_wallet_balance);

        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        AutoCompleteTextView actType = dialogView.findViewById(R.id.act_wallet_type);
        EditText etBal = dialogView.findViewById(R.id.et_wallet_balance);

        // Prepopulate wallet types list
        String[] typesList = new String[]{"Bank", "Credit", "Cash", "E-Wallet"};
        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                typesList
        );
        actType.setAdapter(typeAdapter);
        actType.setText("Bank", false);

        // Default card preview state
        updateCardPreview("", "Bank", "", layoutPreviewBg, tvPreviewType, tvPreviewName, tvPreviewBalance);

        // TextWatcher to update preview in real-time
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCardPreview(
                        etName.getText().toString().trim(),
                        actType.getText().toString().trim(),
                        etBal.getText().toString().trim(),
                        layoutPreviewBg, tvPreviewType, tvPreviewName, tvPreviewBalance
                );
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(watcher);
        etBal.addTextChangedListener(watcher);

        actType.setOnItemClickListener((parent, view, position, id) -> {
            updateCardPreview(
                    etName.getText().toString().trim(),
                    parent.getItemAtPosition(position).toString(),
                    etBal.getText().toString().trim(),
                    layoutPreviewBg, tvPreviewType, tvPreviewName, tvPreviewBalance
            );
        });

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Thêm ví tài khoản mới");
        builder.setView(dialogView);

        builder.setPositiveButton("Tạo ví", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String type = actType.getText().toString().trim();
            String balStr = etBal.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(getContext(), "Không được để trống tên/loại ví!", Toast.LENGTH_SHORT).show();
                return;
            }

            Wallet wallet = new Wallet();
            int userId = requireActivity()
                    .getSharedPreferences("smart_expense_prefs", android.content.Context.MODE_PRIVATE)
                    .getInt("user_id", 1);
            wallet.setUserId(userId);
            wallet.setName(name);
            wallet.setType(type);
            if (balStr.isEmpty()) {
                wallet.setBalance(BigDecimal.ZERO);
            } else {
                try {
                    wallet.setBalance(new BigDecimal(balStr));
                } catch (Exception ignored) {
                    Toast.makeText(getContext(), "Số dư không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            ApiClient.getApiService().createWallet(wallet).enqueue(new Callback<Wallet>() {
                @Override
                public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã tạo ví " + name + " thành công!", Toast.LENGTH_SHORT).show();
                        loadDashboardData();
                    } else {
                        Toast.makeText(getContext(), "Không tạo được ví, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Wallet> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng kết nối tới Spring Boot!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditWalletDialog(Wallet wallet) {
        if (getContext() == null || wallet == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wallet_form, null);

        RelativeLayout layoutPreviewBg = dialogView.findViewById(R.id.layout_preview_bg);
        TextView tvPreviewType = dialogView.findViewById(R.id.tv_preview_wallet_type);
        TextView tvPreviewName = dialogView.findViewById(R.id.tv_preview_wallet_name);
        TextView tvPreviewBalance = dialogView.findViewById(R.id.tv_preview_wallet_balance);

        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        AutoCompleteTextView actType = dialogView.findViewById(R.id.act_wallet_type);
        EditText etBal = dialogView.findViewById(R.id.et_wallet_balance);

        // Prepopulate wallet types list
        String[] typesList = new String[]{"Bank", "Credit", "Cash", "E-Wallet"};
        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                typesList
        );
        actType.setAdapter(typeAdapter);

        // Seed with current wallet data
        etName.setText(wallet.getName() != null ? wallet.getName() : "");
        String currentType = wallet.getType() != null ? wallet.getType() : "Bank";
        actType.setText(currentType, false);
        String currentBal = wallet.getBalance() != null ? wallet.getBalance().toPlainString() : "0";
        etBal.setText(currentBal);

        // Initial preview binding
        updateCardPreview(
                etName.getText().toString().trim(),
                actType.getText().toString().trim(),
                etBal.getText().toString().trim(),
                layoutPreviewBg, tvPreviewType, tvPreviewName, tvPreviewBalance
        );

        // TextWatcher to update preview in real-time
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCardPreview(
                        etName.getText().toString().trim(),
                        actType.getText().toString().trim(),
                        etBal.getText().toString().trim(),
                        layoutPreviewBg, tvPreviewType, tvPreviewName, tvPreviewBalance
                );
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(watcher);
        etBal.addTextChangedListener(watcher);

        actType.setOnItemClickListener((parent, view, position, id) -> {
            updateCardPreview(
                    etName.getText().toString().trim(),
                    parent.getItemAtPosition(position).toString(),
                    etBal.getText().toString().trim(),
                    layoutPreviewBg, tvPreviewType, tvPreviewName, tvPreviewBalance
            );
        });

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Sửa ví");
        builder.setView(dialogView);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String type = actType.getText().toString().trim();
            String balStr = etBal.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(getContext(), "Không được để trống tên/loại ví!", Toast.LENGTH_SHORT).show();
                return;
            }

            Wallet payload = new Wallet();
            payload.setName(name);
            payload.setType(type);
            if (!balStr.isEmpty()) {
                try {
                    payload.setBalance(new BigDecimal(balStr));
                } catch (Exception ignored) {
                    Toast.makeText(getContext(), "Số dư không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            int userId = requireActivity()
                    .getSharedPreferences("smart_expense_prefs", android.content.Context.MODE_PRIVATE)
                    .getInt("user_id", 1);

            ApiClient.getApiService().updateWallet(wallet.getWalletId(), userId, payload).enqueue(new Callback<Wallet>() {
                @Override
                public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã cập nhật ví!", Toast.LENGTH_SHORT).show();
                        loadDashboardData();
                    } else {
                        String msg = "Không cập nhật được ví";
                        try {
                            if (response.errorBody() != null) msg = response.errorBody().string();
                        } catch (Exception ignored) {}
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Wallet> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng khi cập nhật ví!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void setWalletsLoading(boolean loading) {
        if (rvWallets == null || progressWallets == null || tvWalletsState == null) return;
        if (loading) {
            progressWallets.setVisibility(View.VISIBLE);
            tvWalletsState.setVisibility(View.GONE);
            rvWallets.setVisibility(View.GONE);
        } else {
            progressWallets.setVisibility(View.GONE);
            updateWalletsState();
        }
    }

    private void setWalletsError(String message) {
        if (rvWallets == null || tvWalletsState == null) return;
        tvWalletsState.setText(message);
        tvWalletsState.setVisibility(View.VISIBLE);
        rvWallets.setVisibility(View.GONE);
    }

    private void updateWalletsState() {
        if (rvWallets == null || tvWalletsState == null) return;
        if (walletList.isEmpty()) {
            tvWalletsState.setText("Chưa có ví nào");
            tvWalletsState.setVisibility(View.VISIBLE);
            rvWallets.setVisibility(View.GONE);
        } else {
            tvWalletsState.setVisibility(View.GONE);
            rvWallets.setVisibility(View.VISIBLE);
        }
    }

    private void showWalletActionsDialog(Wallet wallet) {
        if (getContext() == null) return;
        String[] items = new String[]{"Sửa ví", "Xóa ví"};
        new android.app.AlertDialog.Builder(getContext())
                .setTitle(wallet.getName() != null ? wallet.getName() : "Ví")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showEditWalletDialog(wallet);
                    } else {
                        confirmDeleteWallet(wallet);
                    }
                })
                .show();
    }

    private void confirmDeleteWallet(Wallet wallet) {
        if (getContext() == null) return;
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa ví")
                .setMessage("Bạn chắc chắn muốn xóa ví này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteWallet(wallet))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteWallet(Wallet wallet) {
        if (getContext() == null || wallet == null) return;
        int userId = requireActivity()
                .getSharedPreferences("smart_expense_prefs", android.content.Context.MODE_PRIVATE)
                .getInt("user_id", 1);

        ApiClient.getApiService().deleteWallet(wallet.getWalletId(), userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa ví!", Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                } else {
                    String msg = "Không xóa được ví";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi xóa ví!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
