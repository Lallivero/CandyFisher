package com.example.candyfisher.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.candyfisher.utils.Candies;

public class SharedPreferenceAccess {
    private static SharedPreferences sharedPreferences;
    private static final String PREFERENCES_FILE = "com.example.candyfisher";
    private static Candies[] candies;

    public static void initialise(Context context) {
        if(sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        }

        candies = new Candies[Candies.values().length];
        System.arraycopy(Candies.values(), 0, candies, 0, Candies.values().length);
    }

    public static boolean getCandy(int index) {
        return sharedPreferences.getBoolean(String.valueOf(candies[index]), false);
    }

    public static void swapCollected(int index) {
        String key = String.valueOf(candies[index]);
        sharedPreferences.edit().putBoolean(key, (!sharedPreferences.getBoolean(key, false))).apply();
    }

}
