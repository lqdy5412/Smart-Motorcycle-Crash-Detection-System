package com.aya.motorcyclealertreceiver.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aya.motorcyclealertreceiver.R;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_DARK_MODE = "dark_mode";

    private Switch switchNotifications;
    private Switch switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        NavController navController = NavHostFragment.findNavController(this);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        SharedPreferences prefs =
                requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // ===== Notifications (KEEP) =====
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        switchNotifications.setChecked(notificationsEnabled);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply();
            Toast.makeText(
                    requireContext(),
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // ===== Dark Mode (NEW) =====
        boolean darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(darkModeEnabled);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // ===== Alert Sound Navigation (KEEP) =====
        View cardAlertSound = view.findViewById(R.id.cardAlertSound);
        if (cardAlertSound != null) {
            cardAlertSound.setOnClickListener(v ->
                    navController.navigate(R.id.alertSoundFragment)
            );
        }

        return view;
    }
}
