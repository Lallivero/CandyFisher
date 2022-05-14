package com.example.candyfisher.services;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.candyfisher.R;

public class MusicSingleton {
    private static MediaPlayer mediaPlayer;
    private static MusicSingleton musicSingleton;
    private final Context context;

    private MusicSingleton(Context context) {
        this.context = context.getApplicationContext();
        mediaPlayer = MediaPlayer.create(this.context, R.raw.waltz);
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.setLooping(true);
    }

    public static MusicSingleton getInstance(Context context){
        if(musicSingleton == null) {
            synchronized (MusicSingleton.class) {
                musicSingleton = new MusicSingleton(context);
            }
        }
        return musicSingleton;
    }

    public void playMusic() {
        mediaPlayer.start();
    }
    public void pauseMusic() {
        mediaPlayer.pause();
    }
}
