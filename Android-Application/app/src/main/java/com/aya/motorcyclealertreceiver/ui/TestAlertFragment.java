package com.aya.motorcyclealertreceiver.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aya.motorcyclealertreceiver.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TestAlertFragment extends Fragment {

    private DatabaseReference latestRef;
    private DatabaseReference historyRef;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_alert, container, false);

        Button btnSendTest = view.findViewById(R.id.btnSendTestAlert);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        latestRef = db.getReference("accidents/latest");
        historyRef = db.getReference("accidents/history");

        btnSendTest.setOnClickListener(v -> sendTestAlert());

        return view;
    }

    private void sendTestAlert() {

        // ✅ CORRECT TIMESTAMP (UTC, auto-converted on display)
        long timestamp = System.currentTimeMillis();

        Map<String, Object> alert = new HashMap<>();
        alert.put("status", "ACCIDENT");
        alert.put("severity", "MEDIUM");
        alert.put("source", "TEST_BUTTON");
        alert.put("timestamp", timestamp);

        latestRef.setValue(alert);
        historyRef.push().setValue(alert);

        Toast.makeText(
                requireContext(),
                "Test alert sent",
                Toast.LENGTH_SHORT
        ).show();
    }
}
