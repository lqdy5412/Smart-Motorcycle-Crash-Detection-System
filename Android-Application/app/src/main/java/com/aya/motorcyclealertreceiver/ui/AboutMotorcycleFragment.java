package com.aya.motorcyclealertreceiver.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.aya.motorcyclealertreceiver.R;
import com.google.android.material.textfield.TextInputEditText;

public class AboutMotorcycleFragment extends Fragment {

    private TextInputEditText inputModel, inputPlate, inputEngine, inputYear;
    private Button btnSave, btnReset;

    private static final String PREFS = "MotorcycleInfo";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about_motorcycle, container, false);

        inputModel = view.findViewById(R.id.inputModel);
        inputPlate = view.findViewById(R.id.inputPlate);
        inputEngine = view.findViewById(R.id.inputEngine);
        inputYear = view.findViewById(R.id.inputYear);

        btnSave = view.findViewById(R.id.btnSaveMotorcycle);
        btnReset = view.findViewById(R.id.btnResetMotorcycle);

        loadSavedData();

        btnSave.setOnClickListener(v -> saveMotorcycleInfo());
        btnReset.setOnClickListener(v -> resetInfo());

        return view;
    }

    private void loadSavedData() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        inputModel.setText(prefs.getString("model", ""));
        inputPlate.setText(prefs.getString("plate", ""));
        inputEngine.setText(prefs.getString("engine", ""));
        inputYear.setText(prefs.getString("year", ""));
    }

    private void saveMotorcycleInfo() {
        SharedPreferences.Editor editor =
                requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();

        editor.putString("model", inputModel.getText().toString());
        editor.putString("plate", inputPlate.getText().toString());
        editor.putString("engine", inputEngine.getText().toString());
        editor.putString("year", inputYear.getText().toString());
        editor.apply();

        Toast.makeText(getContext(), "Motorcycle info saved ✔", Toast.LENGTH_SHORT).show();
    }

    private void resetInfo() {
        SharedPreferences.Editor editor =
                requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();

        editor.clear();
        editor.apply();

        inputModel.setText("");
        inputPlate.setText("");
        inputEngine.setText("");
        inputYear.setText("");

        Toast.makeText(getContext(), "Information reset", Toast.LENGTH_SHORT).show();
    }
}
