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

        fabAddDebt.setOnClickListener(v -> showAddDebtDialog(null));
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

    private void showAddDebtDialog(final DebtLoan dlToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_debt_loan, null);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        EditText etPersonName = dialogView.findViewById(R.id.et_person_name);
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        TextView tvDueDate = dialogView.findViewById(R.id.tv_due_date);
        EditText etInterestRate = dialogView.findViewById(R.id.et_interest_rate);
        android.widget.AutoCompleteTextView actInterestTerm = dialogView.findViewById(R.id.act_interest_term);
        EditText etNote = dialogView.findViewById(R.id.et_note);

        final Calendar cal = Calendar.getInstance();
        tvDueDate.setText(dateFormat.format(cal.getTime()));

        // Setup Interest Term dropdown
        String[] terms = {"% / tháng", "% / năm"};
        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, terms);
        actInterestTerm.setAdapter(termAdapter);
        actInterestTerm.setText(terms[0], false); // default to "% / tháng"

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

        if (dlToEdit != null) {
            spinnerType.setSelection("DEBT".equalsIgnoreCase(dlToEdit.getType()) ? 0 : 1);
            etPersonName.setText(dlToEdit.getPersonName());
            etAmount.setText(dlToEdit.getAmount() != null ? dlToEdit.getAmount().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() : "");
            tvDueDate.setText(dlToEdit.getDueDate());
            if (dlToEdit.getInterestRate() != null && dlToEdit.getInterestRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal monthlyRate = dlToEdit.getInterestRate().divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
                etInterestRate.setText(monthlyRate.stripTrailingZeros().toPlainString());
            } else {
                etInterestRate.setText("");
            }
            etNote.setText(dlToEdit.getNote());
        }

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle(dlToEdit == null ? "Thêm Ghi Chép Nợ" : "Sửa Ghi Chép Nợ")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etPersonName.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    String interestStr = etInterestRate.getText().toString().trim();
                    String termStr = actInterestTerm.getText().toString().trim();
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
                            // If term is monthly, convert to yearly by multiplying by 12
                            if ("% / tháng".equalsIgnoreCase(termStr)) {
                                interest = interest.multiply(new BigDecimal("12"));
                            }
                        } catch (Exception ignored) {}
                    }

                    DebtLoan dl = dlToEdit;
                    if (dl == null) {
                        dl = new DebtLoan();
                        dl.setStatus("UNPAID");
                    }
                    dl.setUserId(userId);
                    dl.setPersonName(name);
                    dl.setType(type);
                    dl.setAmount(amount);
                    dl.setInterestRate(interest);
                    dl.setDueDate(date);
                    dl.setNote(note);

                    if (dlToEdit == null) {
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
                    } else {
                        ApiClient.getApiService().updateDebt(dlToEdit.getDebtId(), userId, dl).enqueue(new Callback<DebtLoan>() {
                            @Override
                            public void onResponse(Call<DebtLoan> call, Response<DebtLoan> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(DebtLoanActivity.this, "Đã cập nhật khoản nợ thành công!", Toast.LENGTH_SHORT).show();
                                    loadDebtsFromServer();
                                } else {
                                    Toast.makeText(DebtLoanActivity.this, "Lỗi khi cập nhật khoản nợ", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<DebtLoan> call, Throwable t) {
                                Toast.makeText(DebtLoanActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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

    @Override
    public void onEdit(DebtLoan dl) {
        showAddDebtDialog(dl);
    }

    @Override
    public void onDetails(DebtLoan dl) {
        StringBuilder details = new StringBuilder();
        details.append("Loại giao dịch: ").append("DEBT".equalsIgnoreCase(dl.getType()) ? "Tôi đi vay (DEBT)" : "Tôi cho vay (LOAN)").append("\n\n");
        details.append("Đối tác: ").append(dl.getPersonName()).append("\n\n");
        
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String amountStr = nf.format(dl.getAmount().setScale(0, java.math.RoundingMode.HALF_UP)) + "đ";
        details.append("Số tiền gốc: ").append(amountStr).append("\n\n");
        
        if (dl.getInterestRate() != null && dl.getInterestRate().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal yearlyRate = dl.getInterestRate();
            BigDecimal monthlyRate = yearlyRate.divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
            details.append("Lãi suất năm: ").append(yearlyRate).append("%\n");
            details.append("Lãi suất tháng: ").append(monthlyRate).append("%\n\n");
            
            // Calculate elapsed days
            long days = 0;
            if (dl.getCreatedAt() != null) {
                try {
                    String clean = dl.getCreatedAt();
                    if (clean.contains("T")) clean = clean.split("T")[0];
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                    java.util.Date created = sdf.parse(clean);
                    java.util.Date today = new java.util.Date();
                    long diff = today.getTime() - created.getTime();
                    days = diff / (24 * 60 * 60 * 1000);
                    if (days < 0) days = 0;
                } catch(Exception ignored){}
            }
            details.append("Số ngày tính lãi: ").append(days).append(" ngày\n");
            BigDecimal daysDec = new BigDecimal(days);
            BigDecimal rateDec = yearlyRate.divide(new BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP);
            BigDecimal interestVal = dl.getAmount().multiply(rateDec).multiply(daysDec)
                    .divide(new BigDecimal("365"), 2, java.math.RoundingMode.HALF_UP);
            
            String interestStr = nf.format(interestVal.setScale(0, java.math.RoundingMode.HALF_UP)) + "đ";
            details.append("Lãi tích lũy hiện tại: ").append(interestStr).append("\n");
            
            String totalStr = nf.format(dl.getAmount().add(interestVal).setScale(0, java.math.RoundingMode.HALF_UP)) + "đ";
            details.append("Tổng tiền cần thanh toán: ").append(totalStr).append("\n\n");
        } else {
            details.append("Lãi suất: Không có lãi\n\n");
        }
        
        details.append("Ngày tạo: ").append(dl.getCreatedAt() != null ? dl.getCreatedAt().replace("T", " ") : "N/A").append("\n\n");
        details.append("Hạn thanh toán: ").append(dl.getDueDate() != null ? dl.getDueDate() : "Không có hạn").append("\n\n");
        details.append("Trạng thái: ").append("PAID".equalsIgnoreCase(dl.getStatus()) ? "ĐÃ THANH TOÁN" : "CHƯA THANH TOÁN").append("\n\n");
        details.append("Ghi chú: ").append(dl.getNote() != null && !dl.getNote().isEmpty() ? dl.getNote() : "(Trống)");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chi Tiết Khoản Vay/Mượn")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

