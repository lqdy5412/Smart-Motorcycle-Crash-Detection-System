package com.aya.motorcyclealertreceiver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aya.motorcyclealertreceiver.R;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> contacts;
    private final SharedPreferences prefs;

    public ContactAdapter(Context context, ArrayList<String> contacts) {
        this.context = context;
        this.contacts = contacts;
        this.prefs = context.getSharedPreferences("EmergencyContacts", Context.MODE_PRIVATE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone;
        ImageView edit, delete;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtContactName);
            phone = itemView.findViewById(R.id.txtContactPhone);
            edit = itemView.findViewById(R.id.btnEdit);
            delete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    // Parses "Name - Phone" into name + phone
    private String[] parseContact(String text) {
        if (!text.contains("-")) return new String[]{"Unknown", "000000"};
        String[] parts = text.split("-", 2);
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        String contact = contacts.get(position);
        String[] parsed = parseContact(contact);

        holder.name.setText(parsed[0]);
        holder.phone.setText(parsed[1]);

        // DELETE CONTACT
        holder.delete.setOnClickListener(v -> {
            contacts.remove(position);
            saveContacts();
            notifyDataSetChanged();
        });

        // EDIT CONTACT
        holder.edit.setOnClickListener(v -> showEditDialog(position, parsed[0], parsed[1]));
    }

    private void showEditDialog(int position, String oldName, String oldPhone) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_contact, null);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);

        edtName.setText(oldName);
        edtPhone.setText(oldPhone);

        new AlertDialog.Builder(context)
                .setTitle("Edit Contact")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String contact = edtName.getText().toString() + " - " + edtPhone.getText().toString();
                    contacts.set(position, contact);
                    saveContacts();
                    notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveContacts() {
        StringBuilder builder = new StringBuilder();
        for (String c : contacts) {
            builder.append(c).append("\n");
        }
        prefs.edit().putString("contacts_list", builder.toString().trim()).apply();
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
