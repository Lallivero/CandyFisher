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
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.candyfisher.R;
import com.example.candyfisher.fragments.FailedThrow;
import com.example.candyfisher.fragments.FailureFragment;
import com.example.candyfisher.models.FishingGameModel;


public class FishingActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "FishingActivity";

    private float[] values;


    private FishingGameModel model;

    private boolean display = false;


    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        if (values == null) {
            values = new float[3];
        }


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mRelativeLayout = findViewById(R.id.fishing_layout);
        mImageView = findViewById(R.id.background_image);

        model = new FishingGameModel();

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

        model.setValues(sensorEvent.values.clone());

        if (model.checkSuccessfulThrow()) {
            model.startFishing();
            Toast.makeText(this, "Nice Throw!", Toast.LENGTH_SHORT).show();
            changeBackground(model.getCurrentlyFishing());
        } else if (model.checkSuccessfulCatch()) {
            model.setCaught(true);
        }else if(model.checkFailedCatch()){
            model.failedCatch();
            model.stopFishing();
            changeBackground(model.getCurrentlyFishing());
            Toast.makeText(this, "Failed Catch :(", Toast.LENGTH_SHORT).show();
        }


        if (model.biteEligible()) {
            model.bite();
            vibrate();
        } else if (model.getCaught()) {
            model.stopFishing();
            Toast.makeText(this, "Caught a Fish!", Toast.LENGTH_SHORT).show();
            changeBackground(model.getCurrentlyFishing());
        } else if (model.pastBiteTime()) {
            model.stopFishing();
            changeBackground(model.getCurrentlyFishing());
            Toast.makeText(this, "That one got away :(", Toast.LENGTH_SHORT).show();
        }

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


    public void changeBackground(boolean fishing) {
        if (fishing) {
            mImageView.setImageResource(R.drawable.background_test_large_cropped);
//            mRelativeLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.not_fishing));
        } else {
            mImageView.setImageResource(R.drawable.background_test_large_cropped);
//            mRelativeLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.fishing));
        }
    }

}