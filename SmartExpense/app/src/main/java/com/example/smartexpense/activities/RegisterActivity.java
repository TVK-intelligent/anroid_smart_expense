package com.example.smartexpense.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private MaterialButton btnRegister;
    private TextView tvGotoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvGotoLogin = findViewById(R.id.tv_goto_login);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ các trường thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(password);
            user.setCurrency("VND");

            ApiClient.getApiService().register(user).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        SharedPreferences sp = getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
                        sp.edit().putString("user_name", name)
                                 .putString("user_email", email)
                                 .putString("user_password", password)
                                 .apply();

                        Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: Email có thể đã tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // Fallback to local SharedPreferences
                    SharedPreferences sp = getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
                    sp.edit().putString("user_name", name)
                             .putString("user_email", email)
                             .putString("user_password", password)
                             .apply();

                    Toast.makeText(RegisterActivity.this, "Spring Boot ngoại tuyến: Đã lưu đăng ký ngoại tuyến thành công!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });

        tvGotoLogin.setOnClickListener(v -> finish());
    }
}
