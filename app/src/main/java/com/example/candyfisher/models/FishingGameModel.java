package com.example.candyfisher.models;


import android.hardware.SensorManager;
import android.util.Log;

import com.example.candyfisher.utils.Candies;
import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;

import java.util.Random;

public class FishingGameModel {
    private static final String TAG = "FishingGameModel";

    //State values
    private float[] values;
    private float[] rotationMatrix;
    private float[] orientationValues;
    private Tilt previousTilt;
    private Tilt tilt;
    private final Fifo fifo;

    //Flow booleans
    private boolean currentlyFishing;
    private boolean bite;

    //Fishing timers
    private long fishingStartTime = 0;
    private long biteTime;
    private long biteDelay;

    private final boolean orientationMode;


    public FishingGameModel(boolean orientation) {
        orientationMode = orientation;
        if (orientationMode && values == null) {
            values = new float[4];
            orientationValues = new float[3];
            rotationMatrix = new float[9];
        } else if (values == null) {
            values = new float[3];
        }
        fifo = new Fifo();
    }

    public void startFishing() {
        currentlyFishing = true;
        biteDelay = randomiseBiteDelay();
        fishingStartTime = System.currentTimeMillis();
    }

    public void stopFishing() {
        currentlyFishing = false;
        bite = false;
        clearTilts();
    }

    private void clearTilts() {
        fifo.clear();
        tilt = null;
        previousTilt = null;
    }

    public void setValues(float[] values) {
        this.values = values;
        if (orientationMode) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, values);
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            setTiltRotation();
        } else
            setTilt();
    }

    public boolean biteEligible() {
        return !bite && currentlyFishing && System.currentTimeMillis() - fishingStartTime > biteDelay;
    }

    public boolean pastBiteTime() {
        long biteDuration = 2000;
        return bite && System.currentTimeMillis() - biteTime > biteDuration;
    }

    public boolean getCurrentlyFishing() {
        return currentlyFishing;
    }

    //Register a bite
    public void bite() {
        biteTime = System.currentTimeMillis();
        bite = true;

    }

    //If the tilt-fifo contains either of the two "correct" patterns for a throw and we are not currently fishing, return true
    public boolean checkSuccessfulThrow() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.UPRIGHT);
        key1.push(Tilt.FACEUP);
        Fifo key2 = new Fifo();
        key2.push(Tilt.UPSIDEDOWN);
        key2.push(Tilt.FACEUP);
        return ((fifo.equals(key2) || fifo.equals(key1)) && !currentlyFishing);
    }

    //If the tilt-fifo contains the allowed catch pattern and we have a bite, return true
    public boolean checkSuccessfulCatch() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.FACEUP);
        key1.push(Tilt.UPRIGHT);
        return (fifo.equals(key1) && bite);
    }

    //If we are fishing and get any tilt combination other than the two allowed throws, return true
    public boolean checkFailedCatch() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.UPRIGHT);
        key1.push(Tilt.FACEUP);
        Fifo key2 = new Fifo();
        key2.push(Tilt.UPSIDEDOWN);
        key2.push(Tilt.FACEUP);
        return (!(fifo.equals(key2) || fifo.equals(key1)) && currentlyFishing);

    }

    //Generate a random candy
    public Candies getCatch() {
        Random rand = new Random();
        int index = rand.nextInt(Candies.values().length);
        return Candies.values()[index];
    }

    private void setTiltRotation() {
        float tiltSensitivity = 0.5f;
        previousTilt = tilt;
        if (Math.abs(Math.PI/2f - Math.abs(orientationValues[1])) < tiltSensitivity) {
            tilt = Tilt.UPRIGHT;
        } else if (Math.abs(orientationValues[1]) < tiltSensitivity) {
            tilt = Tilt.FACEUP;
        }
        if (tilt != previousTilt) {
            fifo.push(tilt);
        }
        Log.i(TAG, "setTiltRotation: " + orientationValues[1]);
    }

    //Sets the tilt to discrete values depending on the current sensor values.
    private void setTilt() {
        float tiltValue = 5f;
        previousTilt = tilt;
        if (values[0] > tiltValue && values[0] > Math.abs(values[1])) {
            tilt = Tilt.LEFT;
        } else if (values[0] < -tiltValue && Math.abs(values[0]) > Math.abs(values[1])) {
            tilt = Tilt.RIGHT;
        } else if (values[1] > tiltValue && values[1] > Math.abs(values[0])) {
            tilt = Tilt.UPRIGHT;
        } else if (values[1] < -tiltValue && Math.abs(values[1]) > Math.abs(values[0])) {
            tilt = Tilt.UPSIDEDOWN;
        } else {
            tilt = Tilt.FACEUP;
        }
        if (tilt != previousTilt) {
            fifo.push(tilt);
        }
    }

    //Sets an allotted grace period during which a throw can not fail to account for noise.
    public boolean gracePeriod() {
        long timeDiff = System.currentTimeMillis() - fishingStartTime;
        long GRACE_PERIOD = 1000;
        return timeDiff > GRACE_PERIOD;
    }

    //Sets a random value between 2 and 5 seconds before a bite event can register
    private long randomiseBiteDelay() {
        Random random = new Random();
        return random.nextInt(3000) + 2000;
    }
}
