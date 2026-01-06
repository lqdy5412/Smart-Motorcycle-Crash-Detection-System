package com.aya.motorcyclealertreceiver.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aya.motorcyclealertreceiver.R;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        NavController navController = NavHostFragment.findNavController(this);

        MaterialCardView aboutCard = view.findViewById(R.id.cardAboutMotorcycle);
        MaterialCardView emergencyCard = view.findViewById(R.id.cardEmergencyContacts);
        MaterialCardView systemStatusCard = view.findViewById(R.id.cardSystemStatus);
        MaterialCardView settingsCard = view.findViewById(R.id.cardSettings);
        MaterialCardView alertHistoryCard = view.findViewById(R.id.cardAlertHistory);
        MaterialCardView testAlertCard = view.findViewById(R.id.cardTestAlert);

        if (testAlertCard != null) {
            testAlertCard.setOnClickListener(v ->
                    navController.navigate(R.id.testAlertFragment)
            );
        }

        if (aboutCard != null) {
            aboutCard.setOnClickListener(v ->
                    navController.navigate(R.id.aboutMotorcycleFragment)
            );
        }

        if (emergencyCard != null) {
            emergencyCard.setOnClickListener(v ->
                    navController.navigate(R.id.emergencyContactsFragment)
            );
        }

        if (systemStatusCard != null) {
            systemStatusCard.setOnClickListener(v ->
                    navController.navigate(R.id.systemStatusFragment)
            );
        }

        if (settingsCard != null) {
            settingsCard.setOnClickListener(v ->
                    navController.navigate(R.id.settingsFragment)
            );
        }

        if (alertHistoryCard != null) {
            alertHistoryCard.setOnClickListener(v ->
                    navController.navigate(R.id.alertHistoryFragment)
            );
        }


        return view;
    }
}
