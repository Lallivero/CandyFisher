package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.candyfisher.R;
import com.example.candyfisher.services.MusicSingleton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    MusicSingleton myMediaPlayer;
    private boolean playing;
    private ImageButton imageButton;
    private boolean hasFocus;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        myMediaPlayer = MusicSingleton.getInstance(this);
        myMediaPlayer.playMusic();
        playing = true;
        imageButton = findViewById(R.id.mute_button);
        imageButton.setImageResource(myMediaPlayer.isMuted() ? R.drawable.ic_round_volume_off_24 : R.drawable.ic_round_volume_up_24);
        hasFocus = true;
    }

    public void toFishingActivity(View view) {
        hasFocus = false;
        Intent intent = new Intent(this, FishingActivity.class);
        startActivity(intent);
    }

    public void toCollection(View view) {
        hasFocus = false;
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

//    public void toSettings(View view){
//        hasFocus = false;
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
//    }

    public void muteOnClick(View view) {
        if (playing) {
            myMediaPlayer.swapMuted();
            myMediaPlayer.pauseMusic();
            imageButton.setImageResource(R.drawable.ic_round_volume_off_24);
        } else {
            myMediaPlayer.swapMuted();
            myMediaPlayer.playMusic();
            imageButton.setImageResource(R.drawable.ic_round_volume_up_24);
        }
        playing = !playing;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasFocus)
            myMediaPlayer.pauseMusic();

    }

    @Override
    protected void onResume() {
        super.onResume();
        hasFocus = true;
        myMediaPlayer.playMusic();

    }

}