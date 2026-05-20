package com.example.smartexpense.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.adapters.WalletAdapter;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.Notification;
import com.example.smartexpense.models.Wallet;
import java.math.BigDecimal;
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
    private RecyclerView rvWallets;
    private LinearLayout layoutAlerts;
    private TextView tvEmptyAlerts;
    private TextView btnAddWallet;
    private WalletAdapter walletAdapter;
    private List<Wallet> walletList = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvGreetingName = view.findViewById(R.id.tv_greeting_name);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvMonthlyIncome = view.findViewById(R.id.tv_monthly_income);
        tvMonthlyExpense = view.findViewById(R.id.tv_monthly_expense);
        rvWallets = view.findViewById(R.id.rv_wallets);
        layoutAlerts = view.findViewById(R.id.layout_alerts);
        tvEmptyAlerts = view.findViewById(R.id.tv_empty_alerts);
        btnAddWallet = view.findViewById(R.id.btn_add_wallet);

        rvWallets.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        walletAdapter = new WalletAdapter(walletList);
        rvWallets.setAdapter(walletAdapter);

        if (btnAddWallet != null) {
            btnAddWallet.setOnClickListener(v -> showAddWalletDialog());
        }

        loadDashboardData();

        return view;
    }

    public void loadDashboardData() {
        if (getContext() == null) return;

        // Get total wallets balance
        ApiClient.getApiService().getWallets(1).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    walletList.clear();
                    walletList.addAll(response.body());
                    walletAdapter.notifyDataSetChanged();

                    BigDecimal sum = BigDecimal.ZERO;
                    for (Wallet w : walletList) {
                        sum = sum.add(w.getBalance());
                    }
                    tvTotalBalance.setText(formatter.format(sum) + "đ");
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });

        // Get dynamic notifications / warnings
        ApiClient.getApiService().getNotifications(1).enqueue(new Callback<List<Notification>>() {
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
    }

    private void updateAlertsUI(List<Notification> notifications) {
        if (getContext() == null || layoutAlerts == null) return;

        // Clear previous except empty placeholder
        layoutAlerts.removeAllViews();

        if (notifications.isEmpty()) {
            layoutAlerts.addView(tvEmptyAlerts);
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

            boolean isAnomaly = noti.getTitle().toLowerCase().contains("bất thường");

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

    private void showAddWalletDialog() {
        if (getContext() == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Thêm ví tài khoản mới");

        LinearLayout layoutContainer = new LinearLayout(getContext());
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layoutContainer.setPadding(padding, padding, padding, padding);

        // Wallet Name TextInputLayout
        com.google.android.material.textfield.TextInputLayout layoutName = new com.google.android.material.textfield.TextInputLayout(getContext());
        layoutName.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutName.setHint("Tên tài khoản / Ví (Ví dụ: Techcombank, MoMo)");
        com.google.android.material.textfield.TextInputEditText etName = new com.google.android.material.textfield.TextInputEditText(getContext());
        layoutName.addView(etName);
        layoutContainer.addView(layoutName);

        // Spacer
        View space = new View(getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(1, (int)(8 * getResources().getDisplayMetrics().density)));
        layoutContainer.addView(space);

        // Wallet Type TextInputLayout
        com.google.android.material.textfield.TextInputLayout layoutType = new com.google.android.material.textfield.TextInputLayout(getContext());
        layoutType.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutType.setHint("Loại ví (Cash, Bank, Credit, E-Wallet)");
        com.google.android.material.textfield.TextInputEditText etType = new com.google.android.material.textfield.TextInputEditText(getContext());
        etType.setText("Bank");
        layoutType.addView(etType);
        layoutContainer.addView(layoutType);

        // Spacer 2
        View space2 = new View(getContext());
        space2.setLayoutParams(new LinearLayout.LayoutParams(1, (int)(8 * getResources().getDisplayMetrics().density)));
        layoutContainer.addView(space2);

        // Wallet Balance TextInputLayout
        com.google.android.material.textfield.TextInputLayout layoutBal = new com.google.android.material.textfield.TextInputLayout(getContext());
        layoutBal.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutBal.setHint("Số dư khởi tạo (VND)");
        com.google.android.material.textfield.TextInputEditText etBal = new com.google.android.material.textfield.TextInputEditText(getContext());
        etBal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layoutBal.addView(etBal);
        layoutContainer.addView(layoutBal);

        builder.setView(layoutContainer);

        builder.setPositiveButton("Tạo ví", (dialog, which) -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String type = etType.getText() != null ? etType.getText().toString().trim() : "";
            String balStr = etBal.getText() != null ? etBal.getText().toString().trim() : "";

            if (name.isEmpty() || type.isEmpty() || balStr.isEmpty()) {
                Toast.makeText(getContext(), "Không được để trống thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Wallet wallet = new Wallet();
            wallet.setUserId(1); // Seed User ID matching backend
            wallet.setName(name);
            wallet.setType(type);
            wallet.setBalance(new BigDecimal(balStr));

            ApiClient.getApiService().createWallet(wallet).enqueue(new Callback<Wallet>() {
                @Override
                public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã tạo ví " + name + " thành công!", Toast.LENGTH_SHORT).show();
                        loadDashboardData(); // Instantly reload wallets list & total balance!
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
}
