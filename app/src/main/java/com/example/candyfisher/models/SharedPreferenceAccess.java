package com.example.candyfisher.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.candyfisher.utils.Candies;

import java.util.ArrayList;
import java.util.Arrays;

public class SharedPreferenceAccess {
    private static SharedPreferences sharedPreferences;
    private static final String PREFERENCES_FILE = "com.example.candyfisher";
    private static ArrayList<Candies> candies;

    public static void initialise(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        }

        candies = new ArrayList<>();
//        System.arraycopy(Candies.values(), 0, candies, 0, Candies.values().length);
        candies.addAll(Arrays.asList(Candies.values()));
    }

    public static boolean isCandyCollected(int index) {
        return sharedPreferences.getBoolean(String.valueOf(candies.get(index)), false);
    }

    public static void swapCollected(int index) {
        String key = String.valueOf(candies.get(index));
        sharedPreferences.edit().putBoolean(key, (!sharedPreferences.getBoolean(key, false))).apply();
    }

    public static ArrayList<Candies> getCandies() {
        return candies;
    }

}
