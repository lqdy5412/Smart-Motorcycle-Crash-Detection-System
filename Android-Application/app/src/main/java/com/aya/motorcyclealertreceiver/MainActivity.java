package com.aya.motorcyclealertreceiver;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FIREBASE_ALERT";
    private static final String CHANNEL_ID = "accident_alerts";
    private static final int REQ_NOTIF = 1001;

    private long lastNotifiedTimestamp = -1;
    private boolean isFirstLoad = true; // 🔴 VERY IMPORTANT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ---------------- NAVIGATION ----------------
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // ---------------- DARK MODE ----------------
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        // ---------------- NOTIFICATION PERMISSION ----------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF
                );
            }
        }

        // ---------------- CHANNEL ----------------
        createNotificationChannel();

        // ---------------- FIREBASE ----------------
        DatabaseReference accidentRef =
                FirebaseDatabase.getInstance().getReference("accidents/latest");

        accidentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                // 🚫 IGNORE FIRST CALLBACK
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }

                String status = snapshot.child("status").getValue(String.class);
                String severity = snapshot.child("severity").getValue(String.class);
                String source = snapshot.child("source").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                if (status == null || timestamp == null) return;
                if (timestamp == lastNotifiedTimestamp) return;

                boolean enabled =
                        prefs.getBoolean("notifications_enabled", true);

                if (!enabled) return;

                lastNotifiedTimestamp = timestamp;
                showAccidentNotification(status, severity, source, timestamp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error", error.toException());
            }
        });
    }

    // ================= NOTIFICATION =================
    private void showAccidentNotification(
            String status,
            String severity,
            String source,
            long timestamp
    ) {

        String time =
                DateFormat.getDateTimeInstance().format(new Date(timestamp));

        String message =
                "Severity: " + severity +
                        "\nSource: " + source +
                        "\nTime: " + time;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alert)
                        .setContentTitle("🚨 Accident Detected")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this)
                .notify((int) (timestamp % Integer.MAX_VALUE), builder.build());
    }

    // ================= CHANNEL =================
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Accident Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );
            channel.setDescription("Motorcycle accident notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
