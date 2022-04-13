package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.candyfisher.R;
import com.example.candyfisher.fragments.FailedThrow;
import com.example.candyfisher.fragments.FailureFragment;
import com.example.candyfisher.interfaces.CollectionAccessContract;
import com.example.candyfisher.interfaces.GameLogicContract;
import com.example.candyfisher.presenter.MyGamePresenter;
import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;
import com.example.candyfisher.utils.Utils;


public class FishingActivity extends AppCompatActivity implements SensorEventListener, GameLogicContract.GameLogicView {
    private static final String TAG = "FishingActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private MyGamePresenter myGamePresenter;


    private ConstraintLayout mConstraintLayout;
    private boolean display = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mConstraintLayout = findViewById(R.id.fishing_layout);
        myGamePresenter = new MyGamePresenter();
        myGamePresenter.initPresenter();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        myGamePresenter.setValues(sensorEvent.values.clone());

        myGamePresenter.setTilt();

        if (myGamePresenter.checkThrow()) {
//            Log.i(TAG, "Accepted");
            startFishing();
        } else {
            stopFishing();
//            Log.i(TAG, "Not accepted");
        }

    }

    private void stopFishing() {
        mConstraintLayout.setBackground(getDrawable(R.drawable.fishing));
    }

    private void startFishing() {
        mConstraintLayout.setBackground(getDrawable(R.drawable.not_fishing));
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
}