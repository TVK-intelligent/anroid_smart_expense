package com.example.smartexpense.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartexpense.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
