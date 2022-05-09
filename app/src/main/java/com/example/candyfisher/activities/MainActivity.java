package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.example.candyfisher.R;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        MediaPlayer myMedia = new MediaPlayer();
//        try {
//            myMedia.setDataSource(String.valueOf(R.raw.waltz));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        myMedia.prepareAsync();
//        myMedia.start();

//        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.waltz);
//
//        mediaPlayer.setLooping(true);
//        mediaPlayer.start(); // no need to call prepare(); create() does that for you


    }
    public void toFishingActivity(View view) {
        Intent intent = new Intent(this, FishingActivity.class);
        startActivity(intent);
    }

    public void toCollection(View view){
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

}