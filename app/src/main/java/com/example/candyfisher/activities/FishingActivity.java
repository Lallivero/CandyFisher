package com.example.candyfisher.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
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


    private SensorManager sensorManager;
    private Sensor accelerometer;


    private ImageView mImageView;
    private CollectionViewModel myCollectionViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);

        myCollectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);


        if (values == null) {
            values = new float[3];
        }


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        mImageView = findViewById(R.id.background_image);

        model = new FishingGameModel();

    }

    //Vibrate the phone
    private void vibrate() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).
                    vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        }
    }
    //The "clock" of the game. Every sensor event is a tick.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //provides sensor data to model
        model.setValues(sensorEvent.values.clone());
        //If we detect a successful throw, start fishing.
        if (model.checkSuccessfulThrow()) {
            model.startFishing();
            //Sound effect here when throwing
            Toast.makeText(this, "Nice Throw!", Toast.LENGTH_SHORT).show();
            changeBackground(model.getCurrentlyFishing());
        //If we catch something successfully register that (Might be some redundancy here)
        } else if (model.checkSuccessfulCatch()) {
            model.stopFishing();
            onCatch();
            //If we fail to catch something after the allotted grace period stop fishing
        } else if (model.checkFailedCatch() && model.gracePeriod()) {
            //Sound effect here when you fail to catch
            model.stopFishing();
            changeBackground(model.getCurrentlyFishing());
            Toast.makeText(this, "Failed Catch :(", Toast.LENGTH_SHORT).show();
        }

        //Check if we are eligible for a bite
        if (model.biteEligible()) {
            //Sound effect here when you get a bite
            model.bite();
            vibrate();
        } else if (model.pastBiteTime()) {
            //Same  sound effect as for failed catch
            model.stopFishing();
            onFailedCatch();

        }

    }

    /*
    Actions to be carried out when a catch fails.
    Change background, show a popup with value -1 for failure.
     */
    private void onFailedCatch() {
        int FAILED_CATCH_VALUE = -1;
        changeBackground(model.getCurrentlyFishing());
        showPopUp(FAILED_CATCH_VALUE);
        Toast.makeText(this, "That one got away :(", Toast.LENGTH_SHORT).show();
    }

    /*
    Actions to be carried out on successful catch.
    These include generating a random catch, setting that index to collected,
    displaying a popup and changing the background
    */
    private void onCatch() {
        int catchIndex = model.getCatch().ordinal();
        myCollectionViewModel.incrementCollected(catchIndex);
        showPopUp(catchIndex);
        Toast.makeText(this, myCollectionViewModel.getItemDescription(catchIndex), Toast.LENGTH_SHORT).show();
        changeBackground(model.getCurrentlyFishing());
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Pauses the sensor when activity is not in focus
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    //Re-registers the sensor when focus is restored
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    //Don't use this, use popup instead
    @Deprecated
    private void loadFragment(int imageId) {
//        SuccessFragment successFragment = SuccessFragment.newInstance(String.valueOf(imageId));
        FragmentManager fragmentManager = getSupportFragmentManager();
//        successFragment.show(fragmentManager, "success_fragment");
        Bundle bundle = new Bundle();
        bundle.putInt("imageId", imageId);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, SuccessFragment.newInstance(String.valueOf(imageId))).addToBackStack(null).commit();
//        display = true;
    }

    //Shows a pop up, takes an int referring to the index of the caught candy. -1 if unsuccessful catch
    private void showPopUp(int catchIndex) {
        sensorManager.unregisterListener(this);
        ImageView imageView;
        TextView textView;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        imageView = alertCustomDialog.findViewById(R.id.catch_image);
        textView = alertCustomDialog.findViewById(R.id.dialog_candy_text);
        //This is the failure state dialog
        if (catchIndex == -1) {
            textView.setText("Oof, that one got away!");
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.cross);
            //Success state dialog
        } else {
            imageView.setImageResource(myCollectionViewModel.getImageId(catchIndex));

            textView.setText(String.format("%s %s!", getString(R.string.catchDialogText), myCollectionViewModel.getItemDescription(catchIndex)));
        }

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
        } else {
            mImageView.setImageResource(R.drawable.not_fishing);
        }
    }

}