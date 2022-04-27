package com.example.candyfisher.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.candyfisher.R;
import com.example.candyfisher.fragments.FailedThrow;
import com.example.candyfisher.fragments.FailureFragment;
import com.example.candyfisher.fragments.SuccessFragment;
import com.example.candyfisher.interfaces.GameLogicContract;
import com.example.candyfisher.presenter.MyGamePresenter;
import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;


public class FishingActivity extends AppCompatActivity implements SensorEventListener, GameLogicContract.GameLogicView {
    private static final String TAG = "FishingActivity";

    private float[] values;
    private Tilt previousTilt;
    private Tilt tilt;

    private Fifo fifo;
    private boolean fishing = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;

//    private MyGamePresenter myGamePresenter;


    private ConstraintLayout mConstraintLayout;
    private boolean display = false;
    private long fishingStartTime;
    private boolean bite;
    private long biteTime;
    private boolean cought;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        if (values == null) {
            values = new float[3];
        }

        fifo = new Fifo();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mConstraintLayout = findViewById(R.id.fishing_layout);
//        myGamePresenter = new MyGamePresenter();
//        myGamePresenter.initPresenter(this, this);
    }

    public void setTilt() {
        float tiltValue = 3f;
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

    public boolean checkThrow() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.UPRIGHT);
        key1.push(Tilt.FACEUP);
        Fifo key2 = new Fifo();
        key2.push(Tilt.UPSIDEDOWN);
        key2.push(Tilt.FACEUP);
//        Log.d(TAG, String.valueOf(fifo));
        return (fifo.equals(key2) || fifo.equals(key1));
    }


    private void vibrate() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).
                    vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

//        myGamePresenter.setValues(sensorEvent.values.clone());
        values = sensorEvent.values.clone();
        setTilt();

        if (checkThrow()) {
            fishing = true;
            changeBackground();
            fifo.clear();
            tilt = null;
            previousTilt = null;
            fishingStartTime = System.currentTimeMillis();
        }else if(checkCatch() && bite){
            Log.i(TAG, "onSensorChanged: in here now");
            cought = true;
        }

        if (!bite && fishing && System.currentTimeMillis() - fishingStartTime > 2000) {
            biteTime = System.currentTimeMillis();
            vibrate();
            bite = true;
        }else if(cought){
            Log.i(TAG, "onSensorChanged: Caught a fish!");
            fishing = false;
            bite = false;
            cought = false;
            loadFragment(new SuccessFragment());
            changeBackground();
        }else if(bite && System.currentTimeMillis() - biteTime > 2000){
            fishing = false;
            bite = false;
            changeBackground();
        }

    }

    private boolean checkCatch() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.FACEUP);
        key1.push(Tilt.UPRIGHT);
//        Fifo key2 = new Fifo();
//        key2.push(Tilt.UPSIDEDOWN);
//        key2.push(Tilt.FACEUP);
//        Log.d(TAG, String.valueOf(fifo));
        return (fifo.equals(key1));
    }

//    private void stopFishing() {
//        myGamePresenter.stopFishing();
//    }

//    private void startFishing() {
//        myGamePresenter.startFishing();
//    }

    private void stopFishing() {
        fishing = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onClick(View view) {
        loadFragment(new FailureFragment());
    }

    private void loadFragment(Fragment fragment) {
        FailedThrow failureFragment = new FailedThrow().newInstance("a", "b");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, failureFragment).addToBackStack(null).commit();
        display = true;
    }

    @Override
    public void initView() {

    }

    @Override
    public void changeBackground(boolean fishing) {

    }


    public void changeBackground() {

        if (!fishing) {
            Log.d(TAG, "changeBackground: " + String.valueOf(fishing));
//            mConstraintLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            mConstraintLayout.setBackground(getDrawable(R.drawable.fishing));
        } else {
            Log.d(TAG, "changeBackground: " + String.valueOf(fishing));
//            mConstraintLayout.setBackgroundColor(Color.parseColor("#FF000000"));
            mConstraintLayout.setBackground(getDrawable(R.drawable.not_fishing));
        }
    }
}