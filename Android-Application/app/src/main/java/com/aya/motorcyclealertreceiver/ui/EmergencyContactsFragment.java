package com.aya.motorcyclealertreceiver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.aya.motorcyclealertreceiver.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;

public class EmergencyContactsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private ArrayList<String> contacts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false);

        recyclerView = view.findViewById(R.id.recyclerContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadContacts();

        adapter = new ContactAdapter(requireContext(), contacts);
        recyclerView.setAdapter(adapter);

        MaterialButton btnAdd = view.findViewById(R.id.btnAddContact);
        btnAdd.setOnClickListener(v -> showAddContactDialog());

        return view;
    }

    private void loadContacts() {
        SharedPreferences prefs = requireContext().getSharedPreferences("EmergencyContacts", Context.MODE_PRIVATE);
        String saved = prefs.getString("contacts_list", "");

        if (saved.trim().isEmpty()) {
            contacts = new ArrayList<>();
        } else {
            contacts = new ArrayList<>(Arrays.asList(saved.split("\n")));
        }
    }

    private void showAddContactDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_contact, null);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Contact")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String entry = edtName.getText().toString() + " - " + edtPhone.getText().toString();
                    contacts.add(entry);
                    saveContacts();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveContacts() {
        SharedPreferences prefs = requireContext().getSharedPreferences("EmergencyContacts", Context.MODE_PRIVATE);
        StringBuilder builder = new StringBuilder();

        for (String c : contacts) {
            builder.append(c).append("\n");
        }

        prefs.edit().putString("contacts_list", builder.toString().trim()).apply();
    }
}
