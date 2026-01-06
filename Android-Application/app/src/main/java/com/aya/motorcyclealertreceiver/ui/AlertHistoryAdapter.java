package com.aya.motorcyclealertreceiver.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aya.motorcyclealertreceiver.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class AlertHistoryAdapter
        extends RecyclerView.Adapter<AlertHistoryAdapter.ViewHolder> {

    private final List<AlertItem> alertList;

    public AlertHistoryAdapter(List<AlertItem> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        AlertItem alert = alertList.get(position);

        // Title
        holder.alertTitle.setText(
                "ACCIDENT (" + alert.severity + ")"
        );

        // Description
        holder.alertDescription.setText(
                "Source: " + alert.source + " | Status: " + alert.status
        );

        // Time
        holder.alertTime.setText(
                DateFormat.getDateTimeInstance()
                        .format(new Date(alert.timestamp))
        );

        // Badge (optional)
        holder.alertType.setText(alert.status);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView alertTitle, alertDescription, alertTime, alertType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            alertTitle = itemView.findViewById(R.id.alertTitle);
            alertDescription = itemView.findViewById(R.id.alertDescription);
            alertTime = itemView.findViewById(R.id.alertTime);
            alertType = itemView.findViewById(R.id.alertType);
        }
    }
}
