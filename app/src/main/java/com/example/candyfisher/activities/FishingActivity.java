package com.example.candyfisher.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

    private SensorManager sensorManager;
    private Sensor accelerometer;

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


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
            CollectionListData myCatch = myData.get(model.getCatch().ordinal());

            showPopUp(myCatch);

            Toast.makeText(this, myCatch.getDescription(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

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

    private void showPopUp(CollectionListData myCatch){
        sensorManager.unregisterListener(this);
        ImageView imageView;
        TextView textView;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        imageView = alertCustomDialog.findViewById(R.id.catch_image);
        imageView.setImageResource(myCatch.getImageId());
        textView = alertCustomDialog.findViewById(R.id.dialog_candy_text);
        textView.setText(myCatch.getDescription());
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button button = alertCustomDialog.findViewById(R.id.ok_button);
        button.setOnClickListener(view -> {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            dialog.dismiss();
        });
        dialog.show();

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