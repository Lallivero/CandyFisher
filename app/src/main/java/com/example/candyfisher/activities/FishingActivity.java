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
import androidx.lifecycle.ViewModelProvider;

import com.example.candyfisher.R;
import com.example.candyfisher.fragments.FailedThrow;
import com.example.candyfisher.fragments.FailureFragment;
import com.example.candyfisher.fragments.SuccessFragment;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.models.FishingGameModel;
import com.example.candyfisher.viewModels.CollectionViewModel;

import java.util.ArrayList;


public class FishingActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "FishingActivity";

    private float[] values;


    private FishingGameModel model;

    private boolean display = false;


    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;
    private CollectionViewModel myCollectionViewModel;
    private ArrayList<CollectionListData> myData;
    private ImageView testImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        myCollectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        myCollectionViewModel.getCollectionListData().observe(this, this::initializeData);

        if (values == null) {
            values = new float[3];
        }


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mRelativeLayout = findViewById(R.id.fishing_layout);
        mImageView = findViewById(R.id.background_image);
        testImage = findViewById(R.id.testImage);

        model = new FishingGameModel();

    }

    private void initializeData(ArrayList<CollectionListData> collectionListData) {
        myData = collectionListData;
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
        } else if (model.checkFailedCatch()) {
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
//            testImage.setImageResource(myData.get(model.getCatch().ordinal()).getImageId());
//            testImage.setVisibility(View.VISIBLE);
//            loadFragment(myData.get(model.getCatch().ordinal()).getImageId());
            Toast.makeText(this, model.getCatch().toString(), Toast.LENGTH_SHORT).show();
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


    private void loadFragment(int imageId) {
//        SuccessFragment successFragment = SuccessFragment.newInstance(String.valueOf(imageId));
        FragmentManager fragmentManager = getSupportFragmentManager();
//        successFragment.show(fragmentManager, "success_fragment");
        Bundle bundle = new Bundle();
        bundle.putInt("imageId" ,imageId);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, SuccessFragment.newInstance(String.valueOf(imageId))).addToBackStack(null).commit();
//        display = true;
    }


    public void changeBackground(boolean fishing) {
        if (fishing) {
            mImageView.setImageResource(R.drawable.fishing);
//            mRelativeLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.not_fishing));
        } else {
            mImageView.setImageResource(R.drawable.not_fishing);
//            mRelativeLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.fishing));
        }
    }

}