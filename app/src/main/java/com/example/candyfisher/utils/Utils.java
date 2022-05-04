package com.example.candyfisher.utils;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.io.IOException;

public class Utils {
    private static final float ALPHA = 0.09f;
    private static final String LANGUAGE_CODE = "en";

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null)
            return input;
        for (int i = 0; i < input.length; i++)
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        return output;
    }

    public static boolean writeNFC(Tag tag, String message) {
        Ndef mNdef = Ndef.get(tag);
        if(mNdef == null)
            return false;
        NdefRecord mNdefRecord = NdefRecord.createTextRecord(LANGUAGE_CODE, message);
        NdefMessage mNdefMessage = new NdefMessage(mNdefRecord);
        try {
            mNdef.connect();
            mNdef.writeNdefMessage(mNdefMessage);
        } catch (IOException | FormatException e) {
            e.printStackTrace();
        } finally {
            try {
                mNdef.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String readNFC(Tag tag){
        Ndef mNdef= Ndef.get(tag);
        if(mNdef == null){
            return null;
        }
        NdefMessage mNdefMessage = mNdef.getCachedNdefMessage();
        byte[] payload = mNdefMessage.getRecords()[0].getPayload();
        byte[] message = new byte[payload.length - LANGUAGE_CODE.length()];
        System.arraycopy(payload, LANGUAGE_CODE.length() + 1, message, 0, payload.length - LANGUAGE_CODE.length()-1);

        String oddMessage = new String(message);
        StringBuilder returnMessage = new StringBuilder();
        for(char c : oddMessage.toCharArray()){
            if (Character.isDigit(c)){
                returnMessage.append(c);
            }
        }
        return new String(returnMessage);
    }
}
