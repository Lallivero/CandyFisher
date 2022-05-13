package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.candyfisher.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MediaPlayer mediaPlayer;
    private boolean playing;
    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = MediaPlayer.create(this, R.raw.waltz);
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
        playing = true;
        imageButton = findViewById(R.id.mute_button);
    }

    public void toFishingActivity(View view) {

        Intent intent = new Intent(this, FishingActivity.class);
        startActivity(intent);
    }

    public void toCollection(View view) {
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

    public void pauseMusic(View view) {
        if (playing) {
            mediaPlayer.pause();
            imageButton.setImageResource(R.drawable.ic_round_volume_up_24);
        } else {
            mediaPlayer.start();
            imageButton.setImageResource(R.drawable.ic_round_volume_off_24);
        }
        playing = !playing;

    }

    @Override
    protected void onStop() {
        super.onStop();
//        mediaPlayer.pause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        mediaPlayer.start();
    }
}