package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.example.candyfisher.R;
import com.example.candyfisher.interfaces.CollectionAccessContract;
import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;
import com.example.candyfisher.utils.Utils;


public class FishingActivity extends AppCompatActivity implements SensorEventListener, CollectionAccessContract.CollectionView {


    private SensorManager sensorManager;
    private Sensor accelerometer;


    private float[] filteredValues;
    private Tilt tilt;
    private Tilt previousTilt;

    private Fifo fifo = new Fifo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        filteredValues = Utils.lowPassFilter(sensorEvent.values.clone(), filteredValues);

        setTilt();
        if (previousTilt != tilt) {
            fifo.push(tilt);
        }
        Log.i("Tilt", fifo.toString());

    }

    private void setTilt() {
        float tiltValue = 3f;
        previousTilt = tilt;
        if (filteredValues[0] > tiltValue && filteredValues[0] > Math.abs(filteredValues[1])) {
            tilt = Tilt.LEFT;
//        }
//        else if(filteredValues[1] > tiltValue && filteredValues[2] > 2){
//            tilt = Tilt.LEANINGFORWARDS;
        } else if (filteredValues[0] < -tiltValue && Math.abs(filteredValues[0]) > Math.abs(filteredValues[1])) {
            tilt = Tilt.RIGHT;
        } else if (filteredValues[1] > tiltValue && filteredValues[1] > Math.abs(filteredValues[0])) {
            tilt = Tilt.UPRIGHT;
        } else if (filteredValues[1] < -tiltValue && Math.abs(filteredValues[1]) > Math.abs(filteredValues[0])) {
            tilt = Tilt.UPSIDEDOWN;
        } else {
            tilt = Tilt.FACEUP;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void initialiseView() {

    }
}