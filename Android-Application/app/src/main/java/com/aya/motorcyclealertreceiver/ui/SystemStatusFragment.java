package com.aya.motorcyclealertreceiver.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aya.motorcyclealertreceiver.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemStatusFragment extends Fragment {

    private TextView txtFirebase;
    private TextView txtLastAlert;
    private TextView txtMotorcycle;
    private TextView txtContacts;
    private TextView txtOverall;

    private DatabaseReference latestAlertRef;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_system_status, container, false);

        txtFirebase = view.findViewById(R.id.txtFirebaseStatus);
        txtLastAlert = view.findViewById(R.id.txtLastAlert);
        txtMotorcycle = view.findViewById(R.id.txtMotorcycleStatus);
        txtContacts = view.findViewById(R.id.txtContactsStatus);
        txtOverall = view.findViewById(R.id.txtOverallStatus);

        latestAlertRef = FirebaseDatabase
                .getInstance()
                .getReference("accidents/latest");

        checkMotorcycleInfo();
        checkEmergencyContacts();
        listenToFirebase();

        return view;
    }

    private void listenToFirebase() {
        txtFirebase.setText("Firebase: Connecting…");

        latestAlertRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtFirebase.setText("Firebase: Connected ✅");

                if (!snapshot.exists()) {
                    txtLastAlert.setText("Last alert: None yet");
                    updateOverallStatus();
                    return;
                }

                Long ts = snapshot.child("timestamp").getValue(Long.class);
                if (ts != null) {
                    String time = new SimpleDateFormat(
                            "dd MMM yyyy, HH:mm",
                            Locale.getDefault()
                    ).format(new Date(ts));

                    txtLastAlert.setText("Last alert: " + time);
                } else {
                    txtLastAlert.setText("Last alert: Unknown time");
                }

                updateOverallStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                txtFirebase.setText("Firebase: Error ❌");
                updateOverallStatus();
            }
        });
    }

    private void checkMotorcycleInfo() {
        SharedPreferences prefs =
                requireContext().getSharedPreferences("MotorcycleInfo", Context.MODE_PRIVATE);

        String model = prefs.getString("model", "");
        String plate = prefs.getString("plate", "");

        if (!TextUtils.isEmpty(model) && !TextUtils.isEmpty(plate)) {
            txtMotorcycle.setText("Motorcycle info: Complete ✅");
        } else {
            txtMotorcycle.setText("Motorcycle info: Missing ❌");
        }
    }

    private void checkEmergencyContacts() {
        SharedPreferences prefs =
                requireContext().getSharedPreferences("EmergencyContacts", Context.MODE_PRIVATE);

        String list = prefs.getString("contacts_list", "");

        if (!TextUtils.isEmpty(list.trim())) {
            txtContacts.setText("Emergency contacts: Added ✅");
        } else {
            txtContacts.setText("Emergency contacts: None ❌");
        }
    }

    private void updateOverallStatus() {
        boolean motorcycleOk = txtMotorcycle.getText().toString().contains("✅");
        boolean contactsOk = txtContacts.getText().toString().contains("✅");
        boolean firebaseOk = txtFirebase.getText().toString().contains("Connected");

        if (motorcycleOk && contactsOk && firebaseOk) {
            txtOverall.setText("System status: READY ✅");
        } else {
            txtOverall.setText("System status: NOT READY ❌");
        }
    }
}
