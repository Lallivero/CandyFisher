package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;

import com.example.candyfisher.R;
import com.example.candyfisher.services.MusicSingleton;
import com.example.candyfisher.viewModels.SettingsViewModel;
import com.google.android.material.chip.Chip;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private HashMap<String, Boolean> mySettings;
    private SettingsViewModel settingsViewModel;

    private Chip music;
    private Chip effects;
    private Chip help;

    private MusicSingleton musicSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(this, this::initialise);

        music = findViewById(R.id.chip4);
        effects = findViewById(R.id.chip5);
        help = findViewById(R.id.chip6);

        music.setOnClickListener(view -> swapMusic());
        effects.setOnClickListener(view -> swapEffects());
        help.setOnClickListener(view -> swapHelp());

        musicSingleton = MusicSingleton.getInstance(this);
        musicSingleton.setMuted(mySettings.get("mute_music"));
    }

    private void initialise(HashMap<String, Boolean> map) {
        mySettings = map;
    }

    private static final String TAG = "SettingsActivity";

    public void swapMusic() {

        settingsViewModel.swapSetting("mute_music");
        musicSingleton.swapMuted();
        if (mySettings.get("mute_music")) {
            Log.i(TAG, "swapMusic: Paused Music");
            musicSingleton.pauseMusic();
        } else {
            Log.i(TAG, "swapMusic: Play Music");
            musicSingleton.playMusic();
        }

    }

    public void swapEffects() {
        settingsViewModel.swapSetting("mute_effects");
    }

    public void swapHelp() {
        settingsViewModel.swapSetting("help_text");
    }
}