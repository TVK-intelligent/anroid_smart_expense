package com.example.smartexpense.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.smartexpense.R;
import com.example.smartexpense.activities.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView tvAvatarChar, tvProfileName, tvProfileEmail;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvAvatarChar = view.findViewById(R.id.tv_avatar_char);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        loadProfileData();

        btnLogout.setOnClickListener(v -> {
            SharedPreferences sp = requireActivity().getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
            sp.edit().putBoolean("is_logged_in", false).apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadProfileData() {
        SharedPreferences sp = requireActivity().getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
        String name = sp.getString("user_name", "Alex Johnson");
        String email = sp.getString("user_email", "alex@smartexpense.com");

        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        if (!name.isEmpty()) {
            tvAvatarChar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }
}
