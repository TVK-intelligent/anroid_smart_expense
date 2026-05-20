package com.example.smartexpense.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.RecurringTransaction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecurringTransactionsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout layoutRecurringContainer;
    private TextView tvEmptyState;
    private MaterialButton btnAddRecurring;

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final Integer CURRENT_USER_ID = 1; // Mock User ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transactions);

        btnBack = findViewById(R.id.btn_back);
        layoutRecurringContainer = findViewById(R.id.layout_recurring_container);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnAddRecurring = findViewById(R.id.btn_add_recurring);

        btnBack.setOnClickListener(v -> finish());
        btnAddRecurring.setOnClickListener(v -> showAddRecurringDialog());

        loadRecurringTransactions();
    }

    private void loadRecurringTransactions() {
        ApiClient.getApiService().getRecurringTransactions(CURRENT_USER_ID).enqueue(new Callback<List<RecurringTransaction>>() {
            @Override
            public void onResponse(Call<List<RecurringTransaction>> call, Response<List<RecurringTransaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RecurringTransaction> list = response.body();
                    updateUI(list);
                } else {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    layoutRecurringContainer.removeAllViews();
                }
            }

            @Override
            public void onFailure(Call<List<RecurringTransaction>> call, Throwable t) {
                Toast.makeText(RecurringTransactionsActivity.this, "Lỗi kết nối tới server!", Toast.LENGTH_SHORT).show();
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
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

            // Row 1: Note and Amount
            RelativeLayout row1 = new RelativeLayout(this);

            TextView tvNote = new TextView(this);
            tvNote.setId(View.generateViewId());
            tvNote.setText(rt.getNote() != null && !rt.getNote().isEmpty() ? rt.getNote() : "Giao dịch định kỳ");
            tvNote.setTextColor(getResources().getColor(R.color.text_primary));
            tvNote.setTextSize(14);
            tvNote.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams noteParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tvNote.setLayoutParams(noteParams);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(formatter.format(rt.getAmount()) + "đ");
            tvAmount.setTextColor(getResources().getColor(R.color.crimson_expense)); // default red, since we usually recurring pay
            tvAmount.setTextSize(16);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams amtParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            amtParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tvAmount.setLayoutParams(amtParams);

            row1.addView(tvNote);
            row1.addView(tvAmount);

            rootLayout.addView(row1);

            // Row 2: Frequency & Next Due Date
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

            // Row 3: Action Buttons (Toggle state & Delete)
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

            // Premium vector delete icon button
            ImageView btnDelete = new ImageView(this);
            btnDelete.setImageResource(R.drawable.ic_delete);
            int paddingDelete = (int) (8 * density);
            btnDelete.setPadding(paddingDelete, paddingDelete, paddingDelete, paddingDelete);
            
            // Add ripple background effect
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            btnDelete.setBackgroundResource(outValue.resourceId);
            btnDelete.setClickable(true);
            btnDelete.setFocusable(true);

            RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(
                    (int) (36 * density),
                    (int) (36 * density)
            );
            deleteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            deleteParams.addRule(RelativeLayout.CENTER_VERTICAL);
            btnDelete.setLayoutParams(deleteParams);

            btnDelete.setOnClickListener(v -> deleteRecurring(id));

            row3.addView(btnToggle);
            row3.addView(btnDelete);

            rootLayout.addView(row3);

            card.addView(rootLayout);
            layoutRecurringContainer.addView(card);
        }
    }

    private void toggleStatus(Integer id, boolean newStatus) {
        ApiClient.getApiService().updateRecurringTransactionStatus(id, CURRENT_USER_ID, newStatus).enqueue(new Callback<Void>() {
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
                    ApiClient.getApiService().deleteRecurringTransaction(id, CURRENT_USER_ID).enqueue(new Callback<Void>() {
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

    private void showAddRecurringDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm giao dịch định kỳ mới");

        LinearLayout layoutContainer = new LinearLayout(this);
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        float density = getResources().getDisplayMetrics().density;
        int padding = (int) (16 * density);
        layoutContainer.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        itemParams.setMargins(0, 0, 0, (int) (12 * density));

        // Input Category ID (Selection Dropdown style)
        TextInputLayout layoutCat = new TextInputLayout(this);
        layoutCat.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutCat.setHint("Chọn Hạng Mục Chi Tiêu");
        layoutCat.setLayoutParams(itemParams);
        TextInputEditText etCat = new TextInputEditText(this);
        etCat.setFocusable(false);
        etCat.setClickable(true);
        etCat.setText("1: Ăn uống"); // Default
        layoutCat.addView(etCat);
        layoutContainer.addView(layoutCat);

        String[] categories = {"1: Ăn uống", "2: Di chuyển", "3: Giải trí", "4: Giáo dục", "5: Sức khỏe"};
        etCat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Chọn hạng mục")
                .setItems(categories, (dialog, which) -> {
                    etCat.setText(categories[which]);
                })
                .show();
        });

        // Input Amount
        TextInputLayout layoutAmt = new TextInputLayout(this);
        layoutAmt.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutAmt.setHint("Số tiền giao dịch (VND)");
        layoutAmt.setLayoutParams(itemParams);
        TextInputEditText etAmt = new TextInputEditText(this);
        etAmt.setInputType(InputType.TYPE_CLASS_NUMBER);
        layoutAmt.addView(etAmt);
        layoutContainer.addView(layoutAmt);

        // Input Frequency (Selection Dropdown style)
        TextInputLayout layoutFreq = new TextInputLayout(this);
        layoutFreq.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutFreq.setHint("Chọn Tần Suất Lặp Lại");
        layoutFreq.setLayoutParams(itemParams);
        TextInputEditText etFreq = new TextInputEditText(this);
        etFreq.setFocusable(false);
        etFreq.setClickable(true);
        etFreq.setText("MONTHLY (Hằng tháng)"); // Default
        layoutFreq.addView(etFreq);
        layoutContainer.addView(layoutFreq);

        String[] frequencies = {"DAILY (Hằng ngày)", "WEEKLY (Hằng tuần)", "MONTHLY (Hằng tháng)", "YEARLY (Hằng năm)"};
        etFreq.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Chọn tần suất")
                .setItems(frequencies, (dialog, which) -> {
                    etFreq.setText(frequencies[which]);
                })
                .show();
        });

        // Input Note
        TextInputLayout layoutNote = new TextInputLayout(this);
        layoutNote.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layoutNote.setHint("Ghi chú");
        layoutNote.setLayoutParams(itemParams);
        TextInputEditText etNote = new TextInputEditText(this);
        layoutNote.addView(etNote);
        layoutContainer.addView(layoutNote);

        builder.setView(layoutContainer);

        builder.setPositiveButton("Lên lịch", (dialog, which) -> {
            String catText = etCat.getText() != null ? etCat.getText().toString().trim() : "";
            String amtStr = etAmt.getText() != null ? etAmt.getText().toString().trim() : "";
            String freqText = etFreq.getText() != null ? etFreq.getText().toString().trim() : "";
            String noteStr = etNote.getText() != null ? etNote.getText().toString().trim() : "";

            if (catText.isEmpty() || amtStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ hạng mục và số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId = 1;
            try {
                categoryId = Integer.parseInt(catText.split(":")[0].trim());
            } catch (Exception e) {
                // Fallback
            }

            String freqStr = "MONTHLY";
            try {
                freqStr = freqText.split(" ")[0].trim();
            } catch (Exception e) {
                // Fallback
            }

            RecurringTransaction rt = new RecurringTransaction();
            rt.setUserId(CURRENT_USER_ID);
            rt.setWalletId(1); // Mặc định ví chính
            rt.setCategoryId(categoryId);
            rt.setAmount(new BigDecimal(amtStr));
            rt.setFrequency(freqStr);
            rt.setNote(noteStr);
            rt.setIsActive(true);

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
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
