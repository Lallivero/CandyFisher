package com.example.candyfisher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.candyfisher.R;
import com.example.candyfisher.models.FishingGameModel;
import com.example.candyfisher.services.MusicSingleton;
import com.example.candyfisher.viewModels.CollectionViewModel;
//import com.tomer.fadingtextview.FadingTextView;


public class FishingActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "FishingActivity";


    private FishingGameModel model;

    Dialog myDialog;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor orientationVector;


    private ImageView mImageView;
    private CollectionViewModel myCollectionViewModel;

    private AsyncTaskParameters asyncTaskParameters;

    private final boolean orientationMode = true;

    private int failSound;
    private int throwSound;
    private int successSound;
    private int timerSound;

    private SoundPool soundPool;
    private boolean soundLoaded;

//    private MusicSingleton myMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);
        myDialog = new Dialog(this);


        myCollectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        myCollectionViewModel.getCollectionListData();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (orientationMode) {
            orientationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            if(orientationVector == null) {

            }
            sensorManager.registerListener(this, orientationVector, SensorManager.SENSOR_DELAY_GAME);


        } else {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.
                USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundLoaded = true);

        failSound = soundPool.load(this, R.raw.fail, 1);
        throwSound = soundPool.load(this, R.raw.throw_sound, 1);
        successSound = soundPool.load(this, R.raw.success, 1);
        timerSound = soundPool.load(this, R.raw.timer, 1);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundLoaded = true;
        mImageView = findViewById(R.id.background_image);
        model = new FishingGameModel(orientationMode);
//        myMediaPlayer = MusicSingleton.getInstance(this);
//        myMediaPlayer.playMusic();
        asyncTaskParameters = new AsyncTaskParameters(3, 250, 200, (Vibrator) getSystemService(VIBRATOR_SERVICE));



    }

    public void ShowPopUp(View v){
        sensorManager.unregisterListener(this);
        TextView txtClose;
        myDialog.setContentView(R.layout.popup);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.setCancelable(false);
        myDialog.setCanceledOnTouchOutside(false);
        txtClose = (TextView) myDialog.findViewById(R.id.close);
        txtClose.setOnClickListener(v1 -> {
            sensorManager.registerListener(this, orientationVector, SensorManager.SENSOR_DELAY_GAME);
            myDialog.dismiss();
        });
        myDialog.show();
    }

    //Vibrate the phone, use AsyncVibration for multiple vibrations
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
            playSound(throwSound);
            //Sound effect here when throwing
//            Toast.makeText(this, "Nice Throw!", Toast.LENGTH_SHORT).show();
            changeBackground(model.getCurrentlyFishing());
            //If we catch something successfully register that (Might be some redundancy here)
        } else if (model.checkSuccessfulCatch()) {
            model.stopFishing();
            onCatch();
            //If we fail to catch something after the allotted grace period stop fishing
        } else if (model.checkFailedCatch() && model.gracePeriod()) {
            //Sound effect here when you fail to catch
            model.stopFishing();
            onFailedCatch();
        }

        //Check if we are eligible for a bite
        if (model.biteEligible()) {
            //Sound effect here when you get a bite
            model.bite();
            playSound(timerSound);
            //Needs to be asynchronous in order to vibrate three times without locking UI thread
            AsyncVibration asyncVibration = new AsyncVibration();
            asyncVibration.execute(asyncTaskParameters);
//            vibrate();
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
        changeBackground(model.getCurrentlyFishing());
        playSound(failSound);
        int FAILED_CATCH_VALUE = -1;
        showPopUp(FAILED_CATCH_VALUE);
//        Toast.makeText(this, "That one got away :(", Toast.LENGTH_SHORT).show();
    }

    /*
    Actions to be carried out on successful catch.
    These include generating a random catch, setting that index to collected,
    displaying a popup and changing the background
    */
    private void onCatch() {
        changeBackground(model.getCurrentlyFishing());
        playSound(successSound);
        int catchIndex = model.getCatch().ordinal();
        myCollectionViewModel.incrementCollected(catchIndex);
        showPopUp(catchIndex);
//        Toast.makeText(this, myCollectionViewModel.getItemDescription(catchIndex), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Pauses the sensor when activity is not in focus
    @Override
    protected void onPause() {
        super.onPause();
//        myMediaPlayer.pauseMusic();
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
//        myMediaPlayer.playMusic();
        if (orientationMode)
            sensorManager.registerListener(this, orientationVector, SensorManager.SENSOR_DELAY_GAME);
        else
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    //Shows a pop up, takes an int referring to the index of the caught candy. -1 if unsuccessful catch
    private void showPopUp(int catchIndex) {
        sensorManager.unregisterListener(this);
        ImageView imageView;
        TextView textView;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        imageView = alertCustomDialog.findViewById(R.id.progressBar);
        textView = alertCustomDialog.findViewById(R.id.dialog_nfc_text);
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
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Button okButton = alertCustomDialog.findViewById(R.id.cancel_button);
        Button collectionButton = alertCustomDialog.findViewById(R.id.collection_button);
        collectionButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CollectionActivity.class);
            startActivity(intent);
            finish();

        });
        okButton.setOnClickListener(view -> {
            if (orientationMode)
                sensorManager.registerListener(this, orientationVector, SensorManager.SENSOR_DELAY_GAME);
            else
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

    private static class AsyncTaskParameters {
        final Vibrator vibrator;
        final int numVibes;
        final long vibrationDelay;
        final long vibrationDuration;

        AsyncTaskParameters(int numVibes, long vibrationDelay, long vibrationDuration, Vibrator vibrator) {
            this.numVibes = numVibes;
            this.vibrationDelay = vibrationDelay;
            this.vibrationDuration = vibrationDuration;
            this.vibrator = vibrator;
        }

    }

    private static class AsyncVibration extends AsyncTask<AsyncTaskParameters, Void, Void> {

        private long previousTime = 0;
        private int count;

        @Override
        protected Void doInBackground(AsyncTaskParameters... parameters) {
            while (count < parameters[0].numVibes) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - previousTime > parameters[0].vibrationDelay) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        parameters[0].vibrator.vibrate(VibrationEffect.createOneShot(parameters[0].vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    previousTime = currentTime;
                    count++;
                }
            }
            return null;
        }


    }

    private void playSound(int sound) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float currentVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //Normalise since soundPool.play() requires a value between 0.0 and 1.0 for volume
        float normalisedVolume = currentVolume / maxVolume;
        if(soundLoaded)
            soundPool.play(sound, normalisedVolume, normalisedVolume, 1, 0, 1f);


    }

}