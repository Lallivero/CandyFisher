package com.example.candyfisher.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.candyfisher.utils.Candies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SharedPreferenceAccess {
    private static SharedPreferences sharedPreferences;
    private static final String PREFERENCES_FILE = "com.example.candyfisher";
    private static ArrayList<Candies> candies;
    private static HashMap<String, Boolean> settings;

    public static void initialise(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        }

        candies = new ArrayList<>();
//        System.arraycopy(Candies.values(), 0, candies, 0, Candies.values().length);
        candies.addAll(Arrays.asList(Candies.values()));
        settings = new HashMap<>();
        settings.put("mute_music", false);
        settings.put("mute_effects", false);
        settings.put("help_text", false);
    }

    @Deprecated
    public static boolean isCandyCollected(int index) {
        return sharedPreferences.getBoolean(String.valueOf(candies.get(index)), false);
    }

    @Deprecated
    public static void swapCollected(int index) {
        String key = String.valueOf(candies.get(index));
        sharedPreferences.edit().putBoolean(key, (!sharedPreferences.getBoolean(key, false))).apply();
    }

    public static int getNumCollected(int index) {
        String key = String.valueOf(candies.get(index));
        return sharedPreferences.getInt(key, 0);
    }

    public static void incrementCollected(int index) {
        String key = String.valueOf(candies.get(index));
        int currentValue = sharedPreferences.getInt(key, 0);
        sharedPreferences.edit().putInt(key, currentValue + 1).apply();
    }

    public static void decrementCollected(int index) {
        String key = String.valueOf(candies.get(index));
        int currentValue = sharedPreferences.getInt(key, 0);
        if (currentValue > 0)
            sharedPreferences.edit().putInt(key, currentValue - 1).apply();
    }

    public static HashMap<String, Boolean> getSettings() {
        for (String s : settings.keySet()) {
            settings.put(s, getSetting(s));
        }
        return settings;
    }

    public static ArrayList<Candies> getCandies() {
        return candies;
    }

    private static final String TAG = "SharedPreferenceAccess";
    private static Boolean getSetting(String setting) {
        Log.i(TAG, "getSetting: called with " + setting);
        return sharedPreferences.getBoolean(setting, true);
    }

    public static void swapSetting(String setting) {
        boolean currentValue = sharedPreferences.getBoolean(setting, false);
        Log.i(TAG, "swapSetting: called with" + setting + currentValue);
        sharedPreferences.edit().putBoolean(setting, !currentValue).apply();
    }
}
