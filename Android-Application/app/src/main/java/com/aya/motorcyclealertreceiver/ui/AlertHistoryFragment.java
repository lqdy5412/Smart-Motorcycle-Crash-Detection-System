package com.aya.motorcyclealertreceiver.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aya.motorcyclealertreceiver.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.text.DateFormat;
import java.util.Date;

public class AlertHistoryFragment extends Fragment {

    private LinearLayout historyContainer;
    private TextView emptyHistoryText;
    private MaterialButton btnClearHistory;

    private DatabaseReference historyRef;
    private ValueEventListener historyListener;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alert_history, container, false);

        historyContainer = view.findViewById(R.id.alertHistoryContainer);
        emptyHistoryText = view.findViewById(R.id.emptyHistoryText);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);

        historyRef = FirebaseDatabase
                .getInstance()
                .getReference("accidents/history");

        btnClearHistory.setOnClickListener(v -> {
            historyRef.removeValue();   // ✅ CLEAR WORKS
        });

        startListening(inflater);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (historyListener != null) {
            historyRef.removeEventListener(historyListener); // ✅ PREVENT DUPLICATES
        }
    }

    private void startListening(LayoutInflater inflater) {

        historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                historyContainer.removeAllViews();

                if (!snapshot.exists()) {
                    emptyHistoryText.setVisibility(View.VISIBLE);
                    return;
                }

                emptyHistoryText.setVisibility(View.GONE);

                for (DataSnapshot item : snapshot.getChildren()) {

                    String status = item.child("status").getValue(String.class);
                    String severity = item.child("severity").getValue(String.class);
                    String source = item.child("source").getValue(String.class);
                    Long timestamp = item.child("timestamp").getValue(Long.class);

                    if (status == null || timestamp == null) continue;

                    View card = inflater.inflate(
                            R.layout.item_alert_history,
                            historyContainer,
                            false
                    );

                    ((TextView) card.findViewById(R.id.alertTitle))
                            .setText("ACCIDENT (" + severity + ")");

                    ((TextView) card.findViewById(R.id.alertDescription))
                            .setText("Source: " + source + " | Status: " + status);

                    ((TextView) card.findViewById(R.id.alertTime))
                            .setText(DateFormat.getDateTimeInstance()
                                    .format(new Date(timestamp)));

                    ((TextView) card.findViewById(R.id.alertType))
                            .setText(status);

                    // newest on top
                    historyContainer.addView(card, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // optional logging
            }
        };

        historyRef.addValueEventListener(historyListener);
    }
}
