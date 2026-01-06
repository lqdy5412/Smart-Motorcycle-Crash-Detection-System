package com.aya.motorcyclealertreceiver.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aya.motorcyclealertreceiver.R;

public class AlertSoundFragment extends Fragment {

    private RadioGroup soundGroup;
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alert_sound, container, false);

        soundGroup = view.findViewById(R.id.soundGroup);

        soundGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            // CHANGE: Replace switch with if-else statements
            if (checkedId == R.id.radioSound1) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound1);
            } else if (checkedId == R.id.radioSound2) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound2);
            } else if (checkedId == R.id.radioSound3) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound3);
            } else if (checkedId == R.id.radioSound4) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound4);
            } else if (checkedId == R.id.radioSound5) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound5);
            }

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}