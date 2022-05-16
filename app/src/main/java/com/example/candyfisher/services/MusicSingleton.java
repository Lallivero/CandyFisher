package com.example.candyfisher.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.candyfisher.R;
import com.example.candyfisher.activities.SettingsActivity;

import java.util.HashMap;

public class MusicSingleton {
    private static MediaPlayer mediaPlayer;
    private static MusicSingleton musicSingleton;
    private final Context context;
    private boolean currentlyPlaying;
    private boolean muted;
    private static final String TAG = "MusicSingleton";

    private MusicSingleton(Context context) {
        this.context = context.getApplicationContext();
        mediaPlayer = MediaPlayer.create(this.context, R.raw.waltz);
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.setLooping(true);
        currentlyPlaying = false;





    }

    public static MusicSingleton getInstance(Context context) {
        if (musicSingleton == null) {
            synchronized (MusicSingleton.class) {
                musicSingleton = new MusicSingleton(context);
            }
        }
        return musicSingleton;
    }

    public void playMusic() {
        if (!currentlyPlaying && !muted) {
            currentlyPlaying = true;
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (currentlyPlaying) {
            currentlyPlaying = false;
            mediaPlayer.pause();
        }
    }

    public boolean isMuted(){
        return muted;
    }

    public void swapMuted(){
        muted = !muted;
    }

    public void setMuted(boolean m){
        muted = m;
    }

    public void lowerVolume(){
        mediaPlayer.setVolume(0.1f, 0.1f);
    }

}
