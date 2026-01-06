package com.aya.motorcyclealertreceiver.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aya.motorcyclealertreceiver.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AlertFragment extends Fragment {

    private TextView txtStatus, txtDetails, txtTime, txtSeverity, txtSource, txtAlertId;
    private TextView txtGoogleMaps, txtGaodeMaps;
    private Button btnCallEmergency;
    private MaterialCardView cardAlertStatus;
    private ImageView iconExpand;
    private LinearLayout expandableDetails;

    private boolean isExpanded = false;
    private static final String EMERGENCY_NUMBER = "112";

    private DatabaseReference latestRef, historyRef;
    private long lastSavedTimestamp = -1;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        txtStatus = view.findViewById(R.id.alertTitle);
        txtDetails = view.findViewById(R.id.alertDescription);
        txtTime = view.findViewById(R.id.alertTime);
        txtSeverity = view.findViewById(R.id.alertSeverity);
        txtSource = view.findViewById(R.id.alertSource);
        txtAlertId = view.findViewById(R.id.alertId);
        txtGoogleMaps = view.findViewById(R.id.txtGoogleMaps);
        txtGaodeMaps = view.findViewById(R.id.txtGaodeMaps);

        btnCallEmergency = view.findViewById(R.id.btnCallEmergency);
        cardAlertStatus = view.findViewById(R.id.cardAlertStatus);
        iconExpand = view.findViewById(R.id.iconExpand);
        expandableDetails = view.findViewById(R.id.expandableDetails);

        btnCallEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + EMERGENCY_NUMBER));
            startActivity(intent);
        });

        cardAlertStatus.setOnClickListener(v -> toggleExpand());

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        latestRef = db.getReference("accidents/latest");
        historyRef = db.getReference("accidents/history");

        listenToLatestAlert();
        return view;
    }

    private void listenToLatestAlert() {
        latestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    showNoAlert();
                    return;
                }

                String status = snapshot.child("status").getValue(String.class);
                String severity = snapshot.child("severity").getValue(String.class);
                String source = snapshot.child("source").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                String googleMaps = snapshot.child("google_maps").getValue(String.class);
                String gaodeMaps  = snapshot.child("gaode_maps").getValue(String.class);

                if (status == null || timestamp == null) {
                    showNoAlert();
                    return;
                }

                txtStatus.setText("🚨 " + status);
                txtDetails.setText("Tap to view alert details");

                txtTime.setText(formatTime(timestamp));
                txtSeverity.setText("⚠️ Severity: " + (severity != null ? severity : "Unknown"));
                txtSource.setText("📍 Source: " + (source != null ? source : "Unknown"));
                txtAlertId.setText("🆔 Alert ID: " + timestamp);

                txtGoogleMaps.setText("🌍 Google Maps: " + (googleMaps != null ? googleMaps : "--"));
                txtGaodeMaps.setText("📍 Gaode Maps: " + (gaodeMaps != null ? gaodeMaps : "--"));

                btnCallEmergency.setVisibility(View.VISIBLE);

                saveToHistoryOnce(status, severity, source, timestamp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showNoAlert();
            }
        });
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        expandableDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        iconExpand.setImageResource(isExpanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
    }

    private void showNoAlert() {
        txtStatus.setText("No Active Alert");
        txtDetails.setText("No accident detected");
        expandableDetails.setVisibility(View.GONE);
        btnCallEmergency.setVisibility(View.GONE);
    }

    private void saveToHistoryOnce(String status, String severity, String source, long timestamp) {
        if (timestamp == lastSavedTimestamp) return;
        lastSavedTimestamp = timestamp;

        Map<String, Object> item = new HashMap<>();
        item.put("status", status);
        item.put("severity", severity);
        item.put("source", source);
        item.put("timestamp", timestamp);

        historyRef.push().setValue(item);
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy • HH:mm:ss", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
