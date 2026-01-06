package com.aya.motorcyclealertreceiver.ui;

public class AlertItem {

    public String status;
    public String severity;
    public String source;
    public long timestamp;

    public AlertItem() {
        // Required empty constructor for Firebase
    }

    public AlertItem(String status, String severity, String source, long timestamp) {
        this.status = status;
        this.severity = severity;
        this.source = source;
        this.timestamp = timestamp;
    }
}
