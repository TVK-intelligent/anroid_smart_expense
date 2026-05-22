package com.example.smartexpense.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.smartexpense.R;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.fragments.AddTransactionFragment;
import com.example.smartexpense.fragments.AnalyticsFragment;
import com.example.smartexpense.fragments.DashboardFragment;
import com.example.smartexpense.fragments.ProfileFragment;
import com.example.smartexpense.fragments.TransactionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private LinearLayout btnResetMock;
    private ProgressBar seedProgress;
    private ImageView seedIcon;
    private BottomNavigationView bottomNav;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnResetMock = findViewById(R.id.btn_reset_mock);
        seedProgress = findViewById(R.id.seed_progress);
        seedIcon = findViewById(R.id.seed_icon);
        bottomNav = findViewById(R.id.bottom_nav);

        // Load Dashboard by default or restore correct tab if recreated
        if (savedInstanceState != null) {
            int selectedId = bottomNav.getSelectedItemId();
            currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment == null) {
                currentFragment = getFragmentForId(selectedId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, currentFragment)
                        .commit();
            }
        } else {
            currentFragment = new DashboardFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, currentFragment)
                    .commit();
        }

        setupNavigation();
        setupSeeder();
        loadCategoriesFromServer();
        
        // Pre-warm TextToSpeech
        com.example.smartexpense.utils.TextToSpeechHelper.getInstance(this);
    }

    private Fragment getFragmentForId(int id) {
        if (id == R.id.nav_dashboard) {
            return new DashboardFragment();
        } else if (id == R.id.nav_history) {
            return new TransactionFragment();
        } else if (id == R.id.nav_add) {
            return new AddTransactionFragment();
        } else if (id == R.id.nav_analytics) {
            return new AnalyticsFragment();
        } else if (id == R.id.nav_profile) {
            return new ProfileFragment();
        }
        return new DashboardFragment();
    }

    private void loadCategoriesFromServer() {
        int userId = getSharedPreferences("smart_expense_prefs", MODE_PRIVATE)
                .getInt("user_id", 1);

        ApiClient.getApiService().getCategories(userId).enqueue(new Callback<java.util.List<com.example.smartexpense.models.Category>>() {
            @Override
            public void onResponse(Call<java.util.List<com.example.smartexpense.models.Category>> call, Response<java.util.List<com.example.smartexpense.models.Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.smartexpense.api.CategoryCache.setCategories(response.body());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<com.example.smartexpense.models.Category>> call, Throwable t) {
                // Fail silently
            }
        });
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected = getFragmentForId(id);

            if (selected != null) {
                currentFragment = selected;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void setupSeeder() {
        btnResetMock.setOnClickListener(v -> {
            seedIcon.setVisibility(View.GONE);
            seedProgress.setVisibility(View.VISIBLE);

            ApiClient.getApiService().seedDatabase().enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    seedProgress.setVisibility(View.GONE);
                    seedIcon.setVisibility(View.VISIBLE);

                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Khởi tạo dữ liệu mẫu thành công!", Toast.LENGTH_SHORT).show();
                        loadCategoriesFromServer();
                        // Refresh if current is Dashboard
                        if (currentFragment instanceof DashboardFragment) {
                            ((DashboardFragment) currentFragment).loadDashboardData();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Lỗi phản hồi từ server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    seedProgress.setVisibility(View.GONE);
                    seedIcon.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Thất bại. Hãy kiểm tra kết nối Spring Boot ở cổng 8080!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.example.smartexpense.utils.TextToSpeechHelper.getInstance(this).shutdown();
    }
}
