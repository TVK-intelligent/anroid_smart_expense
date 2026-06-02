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
import com.example.smartexpense.activities.RecurringTransactionsActivity;
import com.example.smartexpense.activities.DebtLoanActivity;
import com.example.smartexpense.utils.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {

    private TextView tvAvatarChar, tvProfileName, tvProfileEmail;
    private MaterialButton btnLogout;
    private MaterialButton btnRecurring;
    private MaterialButton btnSavingsGoals;
    private MaterialButton btnDebts;
    private View btnChangeLanguage;
    private TextView tvCurrentLanguage;
    private com.google.android.material.switchmaterial.SwitchMaterial switchVoiceWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvAvatarChar = view.findViewById(R.id.tv_avatar_char);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnRecurring = view.findViewById(R.id.btn_recurring_transactions);
        btnSavingsGoals = view.findViewById(R.id.btn_savings_goals);
        btnDebts = view.findViewById(R.id.btn_debts);
        btnChangeLanguage = view.findViewById(R.id.btn_change_language);
        tvCurrentLanguage = view.findViewById(R.id.tv_current_language);
        switchVoiceWarning = view.findViewById(R.id.switch_voice_warning);

        SharedPreferences sp = requireActivity().getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
        boolean isVoiceWarningEnabled = sp.getBoolean("enable_voice_warnings", true);
        switchVoiceWarning.setChecked(isVoiceWarningEnabled);

        switchVoiceWarning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("enable_voice_warnings", isChecked).apply();
        });

        loadProfileData();

        btnRecurring.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RecurringTransactionsActivity.class);
            startActivity(intent);
        });

        btnSavingsGoals.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.smartexpense.activities.SavingsGoalsActivity.class);
            startActivity(intent);
        });

        btnDebts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DebtLoanActivity.class);
            startActivity(intent);
        });

        btnChangeLanguage.setOnClickListener(v -> showLanguageSelectionDialog());

        btnLogout.setOnClickListener(v -> {
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

        // Set the active language label
        String currentLang = LocaleHelper.getLanguage(requireContext());
        if ("vi".equals(currentLang)) {
            tvCurrentLanguage.setText(R.string.profile_lang_vi);
        } else {
            tvCurrentLanguage.setText(R.string.profile_lang_en);
        }
    }

    private void showLanguageSelectionDialog() {
        String currentLang = LocaleHelper.getLanguage(requireContext());
        int checkedItem = "vi".equals(currentLang) ? 1 : 0;

        String[] languages = {
                getString(R.string.profile_lang_en),
                getString(R.string.profile_lang_vi)
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_select_lang)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String targetLang = (which == 1) ? "vi" : "en";
                    if (!targetLang.equals(currentLang)) {
                        LocaleHelper.setLocale(requireContext(), targetLang);
                        dialog.dismiss();
                        
                        // Recreate activity to apply changes instantly
                        if (getActivity() != null) {
                            getActivity().recreate();
                        }
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.profile_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
