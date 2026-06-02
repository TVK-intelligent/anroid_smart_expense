package com.example.smartexpense.activities;

import android.content.Context;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGotoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGotoRegister = findViewById(R.id.tv_goto_register);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            ApiClient.getApiService().login(credentials).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        SharedPreferences sp = getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
                        sp.edit().putBoolean("is_logged_in", true)
                                 .putInt("user_id", user.getUserId())
                                 .putString("user_name", user.getName())
                                 .putString("user_email", user.getEmail())
                                 .apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // Fallback to offline SharedPreferences if backend server is not reachable
                    SharedPreferences sp = getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
                    String savedEmail = sp.getString("user_email", "alex@smartexpense.com");
                    String savedPassword = sp.getString("user_password", "password123");

                    if (email.equals(savedEmail) && password.equals(savedPassword)) {
                        sp.edit().putBoolean("is_logged_in", true)
                                 .putInt("user_id", 1)
                                 .putString("user_name", sp.getString("user_name", "Alex Johnson"))
                                 .putString("user_email", email)
                                 .apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Không thể kết nối máy chủ Spring Boot & đăng nhập sai!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        tvGotoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
