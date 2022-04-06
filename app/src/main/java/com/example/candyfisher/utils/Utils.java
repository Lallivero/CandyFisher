package com.example.candyfisher.utils;

public class Utils {
    private static final float ALPHA = 0.09f;

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null)
            return input;
        for (int i = 0; i < input.length; i++)
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        return output;
    }
}
