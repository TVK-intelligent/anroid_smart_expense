package com.example.smartexpense.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.adapters.DebtLoanAdapter;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.DebtLoan;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class DebtLoanActivity extends BaseActivity implements DebtLoanAdapter.OnDebtActionListener {

    private ImageView btnBack;
    private TextView btnTabAll, btnTabDebts, btnTabLoans;
    private ProgressBar progressDebts;
    private TextView tvEmptyDebts;
    private RecyclerView rvDebts;
    private FloatingActionButton fabAddDebt;

    private DebtLoanAdapter adapter;
    private final List<DebtLoan> fullList = new ArrayList<>();
    private final List<DebtLoan> filteredList = new ArrayList<>();
    private int userId = 1;
    private String activeTab = "ALL"; // "ALL", "DEBT", "LOAN"
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_loan);

        userId = getSharedPreferences("smart_expense_prefs", MODE_PRIVATE).getInt("user_id", 1);

        btnBack = findViewById(R.id.btn_back);
        btnTabAll = findViewById(R.id.btn_tab_all);
        btnTabDebts = findViewById(R.id.btn_tab_debts);
        btnTabLoans = findViewById(R.id.btn_tab_loans);
        progressDebts = findViewById(R.id.progress_debts);
        tvEmptyDebts = findViewById(R.id.tv_empty_debts);
        rvDebts = findViewById(R.id.rv_debts);
        fabAddDebt = findViewById(R.id.fab_add_debt);

        setupRecyclerView();
        setupActions();
        loadDebtsFromServer();
    }

    private void setupRecyclerView() {
        adapter = new DebtLoanAdapter(filteredList, this);
        rvDebts.setLayoutManager(new LinearLayoutManager(this));
        rvDebts.setAdapter(adapter);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnTabAll.setOnClickListener(v -> selectTab("ALL"));
        btnTabDebts.setOnClickListener(v -> selectTab("DEBT"));
        btnTabLoans.setOnClickListener(v -> selectTab("LOAN"));

        fabAddDebt.setOnClickListener(v -> showAddDebtDialog());
    }

    private void selectTab(String tab) {
        activeTab = tab;

        // Visual feedback for tabs
        int activeBg = R.drawable.bg_toggle_expense_active;
        int activeTextColor = getResources().getColor(R.color.surface_white);
        int inactiveTextColor = getResources().getColor(R.color.text_secondary);

        // Reset all tabs
        btnTabAll.setBackground(null);
        btnTabAll.setTextColor(inactiveTextColor);
        btnTabDebts.setBackground(null);
        btnTabDebts.setTextColor(inactiveTextColor);
        btnTabLoans.setBackground(null);
        btnTabLoans.setTextColor(inactiveTextColor);

        if ("ALL".equals(tab)) {
            btnTabAll.setBackgroundResource(activeBg);
            btnTabAll.setTextColor(activeTextColor);
        } else if ("DEBT".equals(tab)) {
            btnTabDebts.setBackgroundResource(activeBg);
            btnTabDebts.setTextColor(activeTextColor);
        } else if ("LOAN".equals(tab)) {
            btnTabLoans.setBackgroundResource(activeBg);
            btnTabLoans.setTextColor(activeTextColor);
        }

        applyFilter();
    }

    private void loadDebtsFromServer() {
        progressDebts.setVisibility(View.VISIBLE);
        tvEmptyDebts.setVisibility(View.GONE);
        rvDebts.setVisibility(View.GONE);

        ApiClient.getApiService().getDebts(userId).enqueue(new Callback<List<DebtLoan>>() {
            @Override
            public void onResponse(Call<List<DebtLoan>> call, Response<List<DebtLoan>> response) {
                progressDebts.setVisibility(View.GONE);
                rvDebts.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    fullList.clear();
                    fullList.addAll(response.body());
                    applyFilter();
                } else {
                    Toast.makeText(DebtLoanActivity.this, "Không thể tải dữ liệu nợ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DebtLoan>> call, Throwable t) {
                progressDebts.setVisibility(View.GONE);
                rvDebts.setVisibility(View.VISIBLE);
                // Offline Mock Fallback
                fullList.clear();
                applyFilter();
                Toast.makeText(DebtLoanActivity.this, "Lỗi kết nối máy chủ, hiển thị chế độ offline", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        filteredList.clear();
        for (DebtLoan dl : fullList) {
            if ("ALL".equals(activeTab)) {
                filteredList.add(dl);
            } else if ("DEBT".equals(activeTab) && "DEBT".equalsIgnoreCase(dl.getType())) {
                filteredList.add(dl);
            } else if ("LOAN".equals(activeTab) && "LOAN".equalsIgnoreCase(dl.getType())) {
                filteredList.add(dl);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            tvEmptyDebts.setVisibility(View.VISIBLE);
        } else {
            tvEmptyDebts.setVisibility(View.GONE);
        }
    }

    private void showAddDebtDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_debt_loan, null);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        EditText etPersonName = dialogView.findViewById(R.id.et_person_name);
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        TextView tvDueDate = dialogView.findViewById(R.id.tv_due_date);
        EditText etInterestRate = dialogView.findViewById(R.id.et_interest_rate);
        EditText etNote = dialogView.findViewById(R.id.et_note);

        final Calendar cal = Calendar.getInstance();
        tvDueDate.setText(dateFormat.format(cal.getTime()));

        tvDueDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);
                        tvDueDate.setText(dateFormat.format(picked.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etPersonName.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    String interestStr = etInterestRate.getText().toString().trim();
                    String note = etNote.getText().toString().trim();
                    String date = tvDueDate.getText().toString().trim();
                    int selectedTypePos = spinnerType.getSelectedItemPosition();
                    String type = (selectedTypePos == 0) ? "DEBT" : "LOAN";

                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên đối tác và số tiền!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (Exception e) {
                        Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BigDecimal interest = BigDecimal.ZERO;
                    if (!interestStr.isEmpty()) {
                        try {
                            interest = new BigDecimal(interestStr);
                        } catch (Exception ignored) {}
                    }

                    DebtLoan dl = new DebtLoan();
                    dl.setUserId(userId);
                    dl.setPersonName(name);
                    dl.setType(type);
                    dl.setAmount(amount);
                    dl.setInterestRate(interest);
                    dl.setDueDate(date);
                    dl.setStatus("UNPAID");
                    dl.setNote(note);

                    ApiClient.getApiService().createDebt(dl).enqueue(new Callback<DebtLoan>() {
                        @Override
                        public void onResponse(Call<DebtLoan> call, Response<DebtLoan> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DebtLoanActivity.this, "Đã lưu khoản nợ thành công!", Toast.LENGTH_SHORT).show();
                                loadDebtsFromServer();
                            } else {
                                Toast.makeText(DebtLoanActivity.this, "Lỗi khi lưu khoản nợ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<DebtLoan> call, Throwable t) {
                            Toast.makeText(DebtLoanActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onComplete(DebtLoan dl) {
        // Mark as PAID
        dl.setStatus("PAID");
        ApiClient.getApiService().updateDebt(dl.getDebtId(), userId, dl).enqueue(new Callback<DebtLoan>() {
            @Override
            public void onResponse(Call<DebtLoan> call, Response<DebtLoan> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DebtLoanActivity.this, "Đã cập nhật trạng thái thanh toán!", Toast.LENGTH_SHORT).show();
                    loadDebtsFromServer();
                } else {
                    Toast.makeText(DebtLoanActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DebtLoan> call, Throwable t) {
                Toast.makeText(DebtLoanActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDelete(DebtLoan dl) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa Ghi Chép Nợ")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chép nợ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteDebt(dl.getDebtId(), userId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful() || response.code() == 204) {
                                Toast.makeText(DebtLoanActivity.this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                                loadDebtsFromServer();
                            } else {
                                Toast.makeText(DebtLoanActivity.this, "Không thể xóa ghi chép", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(DebtLoanActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
