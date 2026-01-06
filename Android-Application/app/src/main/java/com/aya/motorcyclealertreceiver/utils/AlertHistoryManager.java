package com.aya.motorcyclealertreceiver.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlertHistoryManager {

    private static final String PREF = "alerts";
    private static final String ALERT_SET = "alert_set";

    public static void saveAlert(Context context, String title, String message, long time) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Set<String> alerts = new HashSet<>(sp.getStringSet(ALERT_SET, new HashSet<>()));

        alerts.add(title + "|" + message + "|" + time);

        sp.edit().putStringSet(ALERT_SET, alerts).apply();
    }

    public static List<Alert> getAlerts(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Set<String> raw = sp.getStringSet(ALERT_SET, new HashSet<>());

        List<Alert> list = new ArrayList<>();
        for (String s : raw) {
            String[] p = s.split("\\|");
            if (p.length == 3) {
                list.add(new Alert(p[0], p[1], Long.parseLong(p[2])));
            }
        }
        return list;
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    public static class Alert {
        public String title, message;
        public long time;

        public Alert(String title, String message, long time) {
            this.title = title;
            this.message = message;
            this.time = time;
        }

        public String getFormattedTime() {
            return new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date(time));
        }
    }
}
