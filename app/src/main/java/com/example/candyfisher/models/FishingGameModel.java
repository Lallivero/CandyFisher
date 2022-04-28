package com.example.candyfisher.models;


import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;

public class FishingGameModel {
    private static final String TAG = "FishingGameModel";

    //State values
    private float[] values;
    private Tilt previousTilt;
    private Tilt tilt;
    private final Fifo fifo;

    //Flow booleans
    private boolean currentlyFishing;
    private boolean bite;
    private boolean caught;

    //Fishing timers
    private long fishingStartTime;
    private long biteTime;


    public FishingGameModel() {
        if (values == null) {
            values = new float[3];
        }

        fifo = new Fifo();
    }

    public void startFishing() {
        currentlyFishing = true;
        clearTilts();
        fishingStartTime = System.currentTimeMillis();
    }

    public void stopFishing() {
        currentlyFishing = false;
        bite = false;
        caught = false;
    }

    private void clearTilts() {
        fifo.clear();
        tilt = null;
        previousTilt = null;
    }

    public void setValues(float[] values) {
        this.values = values;
        setTilt();
    }

    public boolean biteEligible() {
        return !bite && currentlyFishing && System.currentTimeMillis() - fishingStartTime > 2000;
    }

    public boolean pastBiteTime() {
        return bite && System.currentTimeMillis() - biteTime > 2000;
    }

    public boolean getCurrentlyFishing() {
        return currentlyFishing;
    }

    public void bite() {
        biteTime = System.currentTimeMillis();
        bite = true;
        clearTilts();
    }

    public boolean getCaught() {
        return caught;
    }

    public boolean checkSuccessfulThrow() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.UPRIGHT);
        key1.push(Tilt.FACEUP);
        Fifo key2 = new Fifo();
        key2.push(Tilt.UPSIDEDOWN);
        key2.push(Tilt.FACEUP);
//        Log.d(TAG, String.valueOf(fifo));
        return (fifo.equals(key2) || fifo.equals(key1));
    }

    public boolean checkSuccessfulCatch() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.FACEUP);
        key1.push(Tilt.UPRIGHT);

        return (fifo.equals(key1) && bite);
    }

    public boolean checkFailedCatch() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.FACEUP);

        return !fifo.equals(key1) && currentlyFishing;
    }

    public void failedCatch() {
        clearTilts();
    }

    public void setCaught(boolean caught) {
        this.caught = caught;
    }

    public void setTilt() {
        float tiltValue = 5f;
        previousTilt = tilt;
        if (values[0] > tiltValue && values[0] > Math.abs(values[1])) {
            tilt = Tilt.LEFT;
//        }
//        else if(filteredValues[1] > tiltValue && filteredValues[2] > 2){
//            tilt = Tilt.LEANINGFORWARDS;
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

}
