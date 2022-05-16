package com.example.candyfisher.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.candyfisher.R;
import com.example.candyfisher.models.FishingGameModel;
import com.example.candyfisher.services.MusicSingleton;
import com.example.candyfisher.viewModels.CollectionViewModel;

import java.util.ArrayList;
import java.util.Random;
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

    private MusicSingleton myMediaPlayer;
    private boolean hasFocus;
    private boolean showingPopup = false;

    private int tutorialSteps;
    private boolean tutorial = false;

    private Animation scaleAnimation;
    private TextView scaleText;
    AnimationSet animationSet;
    Animation fadeOutAnimation;
    private boolean notAnimated = true;
    private boolean help = false;
    private boolean failPause = false;
    private AlertDialog loadingDialog;
    private boolean isLoading = true;
    private long createTime;

    Random random = new Random();

    private ArrayList<String> encouragements = new ArrayList<>();
    private ArrayList<String> tooSlow = new ArrayList<>();
    private ArrayList<String> tooFast = new ArrayList<>();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setup();
        loadSensors();
        loadSound();
        setupAnimations();
        createStringLists();
        showLoadingPopUp();
    }

    private void createStringLists() {
        encouragements.add("Nice Throw!");
        encouragements.add("Well Done!");
        encouragements.add("Good One!");

        tooSlow.add("It got Away!");
        tooSlow.add("Need to be Faster!");
        tooSlow.add("Too Slow!");

        tooFast.add("Too Soon!");
        tooFast.add("Slow Down!");
        tooFast.add("Hold Steady!");
    }

    private void setupAnimations() {
        scaleText = findViewById(R.id.scaleText);
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade);
        animationSet = new AnimationSet(true);
        animationSet.setDuration(1500);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(fadeOutAnimation);
    }

    private void setup() {
        //ViewModel
        myCollectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        myCollectionViewModel.getCollectionListData();

        //Game logic model
        model = new FishingGameModel(orientationMode);

        //Views
        myDialog = new Dialog(this);
        mImageView = findViewById(R.id.background_image);

        //Misc
        hasFocus = true;
        tutorialSteps = 0;
        createTime = System.currentTimeMillis();
        asyncTaskParameters = new AsyncTaskParameters(3, 250, 200, (Vibrator) getSystemService(VIBRATOR_SERVICE));
    }

    private void loadSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (orientationMode) {
            orientationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            if (orientationVector == null) {
                Toast.makeText(this, "Orientation Vector not found", Toast.LENGTH_SHORT).show();
            }
            sensorManager.registerListener(this, orientationVector, SensorManager.SENSOR_DELAY_GAME);

        } else {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void loadSound() {
        //Soundpool
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.
                        USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundLoaded = true);
        failSound = soundPool.load(this, R.raw.fail, 1);
        throwSound = soundPool.load(this, R.raw.throw_sound, 1);
        successSound = soundPool.load(this, R.raw.success, 1);
        timerSound = soundPool.load(this, R.raw.timer, 1);

        //Mediaplayer
        myMediaPlayer = MusicSingleton.getInstance(this);

        soundLoaded = true;
    }


    //The "clock" of the game. Every sensor event is a tick.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (tutorial) {
            tutorialSensorChange(sensorEvent);
        } else {
            normalSensorChange(sensorEvent);
        }
    }

    private void normalSensorChange(SensorEvent sensorEvent) {

        if(System.currentTimeMillis() - createTime > 2500){
            loadingDialog.dismiss();
        }
        if (!showingPopup) {
            //provides sensor data to model
            model.setValues(sensorEvent.values.clone());
            if ((scaleText.getAnimation() == null || notAnimated || scaleText.getAnimation().hasEnded()) && !model.getCurrentlyFishing() && help) {
                notAnimated = false;
                scaleText.setVisibility(View.VISIBLE);
                scaleText.setText(R.string.scale);
                scaleText.startAnimation(scaleAnimation);
            } else if (model.getCurrentlyFishing() && !model.getBite() && help && scaleAnimation.hasEnded()) {
                scaleText.setVisibility(View.VISIBLE);
                scaleText.setText("Hold Steady Now!");
                scaleText.startAnimation(scaleAnimation);
            }
            //text animation
            //If we detect a successful throw, start fishing.
            if (model.checkSuccessfulThrow()) {
                if (!help) {
                    scaleText.setText(encouragements.get(random.nextInt(encouragements.size())));
                    scaleText.startAnimation(animationSet);
                } else
                    scaleText.clearAnimation();
//                scaleText.startAnimation(animationSet);
                model.startFishing();
                playSound(throwSound);
                changeBackground(model.getCurrentlyFishing());
                //If we catch something successfully register that (Might be some redundancy here)
            } else if (model.checkSuccessfulCatch()) {
                model.stopFishing();
                onCatch();

                //If we fail to catch something after the allotted grace period stop fishing
            } else if (model.checkFailedCatch() && model.gracePeriod()) {
                model.stopFishing();
                onFailedCatch(0, false);

            }

            //Check if we are eligible for a bite
            if (model.biteEligible()) {
                model.bite();
                playSound(timerSound);
                //Needs to be asynchronous in order to vibrate three times without locking UI thread
                AsyncVibration asyncVibration = new AsyncVibration();
                asyncVibration.execute(asyncTaskParameters);

                scaleText.setText(help ? "Tilt Up!" : "Oh, a Bite!");
                scaleText.setVisibility(View.VISIBLE);
                scaleText.startAnimation(animationSet);

            } else if (model.pastBiteTime()) {
                model.stopFishing();
                onFailedCatch(1, true);
            }
        }
    }

    private void tutorialSensorChange(SensorEvent sensorEvent) {

        if (!showingPopup) {
            model.setValues(sensorEvent.values.clone());
            switch (tutorialSteps) {
                case 0:
                    showStepByStep("Tilt your phone forward to throw!");
                    break;
                case 1:
                    if (model.checkSuccessfulThrow()) {
                        playSound(throwSound);
                        changeBackground(!model.getCurrentlyFishing());
                        showStepByStep("Good job! Keep your phone face-up and wait for a bite!");
                    }
                    break;
                case 2:
                    if (model.checkSuccessfulCatch()) {
                        model.stopFishing();
                        onCatch();
                        tutorial = false;
                    } else if (model.checkFailedCatch() && model.gracePeriod()) {
                        model.stopFishing();
                        changeBackground(model.getCurrentlyFishing());
                        playSound(failSound);
                        tutorialSteps = 0;
                        showStepByStep("Too fast! Wait for the bite!");
                    } else if (model.biteEligible()) {
                        AsyncVibration asyncVibration = new AsyncVibration();
                        asyncVibration.execute(asyncTaskParameters);
                        showStepByStep("Oh! A bite! Pull up before it gets away!");
                        //Needs to be asynchronous in order to vibrate three times without locking UI thread
                    } else if (model.pastBiteTime()) {
                        model.stopFishing();
                        changeBackground(model.getCurrentlyFishing());
                        playSound(failSound);
                        tutorialSteps = 0;
                        showStepByStep("Too slow! Pull up before the candy gets away!");
                    }
                    break;
            }
        }
    }

    private void tutorialOnClick() {
        switch (tutorialSteps) {
            case 0:
                tutorialSteps += 1;
                break;
            case 1:
                model.startFishing();
                tutorialSteps += 1;
                break;
            case 2:
                model.bite();
                playSound(timerSound);
                break;
        }
    }

    /*
    Actions to be carried out when a catch fails.
    Change background, show a popup with value -1 for failure.
    The arguments were added very late as a means of distinction between several events. No time to refactor.
     */
    private void onFailedCatch(int i, boolean slow) {
        changeBackground(model.getCurrentlyFishing());
        scaleText.clearAnimation();

        playSound(failSound);

        if (failPause && slow) {
            int FAILED_CATCH_VALUE = -1;
            showPopUp(FAILED_CATCH_VALUE);
            scaleText.setVisibility(View.INVISIBLE);
        }else if(failPause){
            int FAILED_CATCH_VALUE = -2;
            showPopUp(FAILED_CATCH_VALUE);
            scaleText.setVisibility(View.INVISIBLE);
        } else {
            scaleText.setText(i == 0 ? tooFast.get(random.nextInt(tooFast.size())) : tooSlow.get(random.nextInt(tooSlow.size())));
            scaleText.startAnimation(animationSet);
        }
    }

    /*
    Actions to be carried out on successful catch.
    These include generating a random catch, setting that index to collected,
    displaying a popup and changing the background
    */
    private void onCatch() {
        changeBackground(model.getCurrentlyFishing());
        playSound(successSound);
        scaleText.clearAnimation();
        scaleText.setVisibility(View.INVISIBLE);
        int catchIndex = model.getCatch().ordinal();
        myCollectionViewModel.incrementCollected(catchIndex);
        showPopUp(catchIndex);
//        Toast.makeText(this, myCollectionViewModel.getItemDescription(catchIndex), Toast.LENGTH_SHORT).show();

    }

    //Shows a pop up, takes an int referring to the index of the caught candy. -1 if unsuccessful catch
    private void showPopUp(int catchIndex) {
        showingPopup = true;
//        sensorManager.unregisterListener(this);
        ImageView imageView;
        TextView textView;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        imageView = alertCustomDialog.findViewById(R.id.progressBar);
        textView = alertCustomDialog.findViewById(R.id.dialog_loading_text);
        //This is the failure state dialog
        if (catchIndex == -1) {
            textView.setText("That one got away!");
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.cross);
            //Success state dialog
        } else if (catchIndex == -2) {
            textView.setText("Hold it steady!");
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.cross);
        }else{
            imageView.setImageResource(myCollectionViewModel.getImageId(catchIndex));

            textView.setText(String.format("%s %s!", getString(R.string.catchDialogText), myCollectionViewModel.getItemDescription(catchIndex)));
        }

        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Button okButton = alertCustomDialog.findViewById(R.id.recieve_done_button);
        Button collectionButton = alertCustomDialog.findViewById(R.id.collection_button);
        collectionButton.setOnClickListener(view -> {
            hasFocus = false;
            sensorManager.unregisterListener(this);
            Intent intent = new Intent(this, CollectionActivity.class);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });
        okButton.setOnClickListener(view -> {
            showingPopup = false;
            dialog.dismiss();
        });
        dialog.show();

    }


    private void showOptionsPopUp() {
        showingPopup = true;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_options, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button pauseOnFail = alertCustomDialog.findViewById(R.id.check_fail_pause);
        Button helpText = alertCustomDialog.findViewById(R.id.check_help_text);
        Button tutorial_button = alertCustomDialog.findViewById(R.id.check_tutorial);
        if (failPause)
            pauseOnFail.performClick();
        if (help)
            helpText.performClick();
        if (tutorial)
            tutorial_button.performClick();

        pauseOnFail.setOnClickListener(view -> {
            failPause = !failPause;
        });
        helpText.setOnClickListener(view -> {
            help = !help;
            scaleText.clearAnimation();
            scaleText.setVisibility(View.INVISIBLE);
        });
        tutorial_button.setOnClickListener(view -> {
            tutorialSteps = 0;
            tutorial = !tutorial;
        });
        dialog.setOnDismissListener(dialogInterface -> {
            showingPopup = false;
        });
        dialog.show();
    }

    private void showStepByStep(String message) {
        showingPopup = true;
        TextView textView;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_tutorial, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        textView = alertCustomDialog.findViewById(R.id.dialog_tutorial_text);
        textView.setText(message);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Button okButton = alertCustomDialog.findViewById(R.id.tutorial_button);
        okButton.setOnClickListener(view -> {
            showingPopup = false;
            tutorialOnClick();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showLoadingPopUp() {
        showingPopup = true;
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_loading, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        loadingDialog = alert.create();
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnDismissListener(dialogInterface -> {
            showingPopup = false;
        });
        loadingDialog.show();
    }

    public void showTutorialOnClick(View v) {
        showOptionsPopUp();
    }

    private void playSound(int sound) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float currentVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //Normalise since soundPool.play() requires a value between 0.0 and 1.0 for volume
        float normalisedVolume = currentVolume / maxVolume;
        if (soundLoaded)
            soundPool.play(sound, normalisedVolume, normalisedVolume, 1, 0, 1f);

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
                    }else {
                        parameters[0].vibrator.vibrate(parameters[0].vibrationDuration);
                    }
                    previousTime = currentTime;
                    count++;
                }
            }
            return null;
        }
    }

    //Pauses the sensor when activity is not in focus
    @Override
    protected void onPause() {
        super.onPause();
        if (hasFocus)
            myMediaPlayer.pauseMusic();
        showingPopup = true;
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
        hasFocus = true;
        myMediaPlayer.playMusic();
        showingPopup = false;
        if (orientationMode)
            sensorManager.registerListener(this, orientationVector, SensorManager.SENSOR_DELAY_GAME);
        else
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onBackPressed() {
        hasFocus = false;
        super.onBackPressed();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}