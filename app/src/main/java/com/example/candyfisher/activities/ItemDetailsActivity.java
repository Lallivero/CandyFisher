package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.candyfisher.R;

import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.services.MusicSingleton;
import com.example.candyfisher.utils.Utils;
import com.example.candyfisher.viewModels.CollectionViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class ItemDetailsActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private static final String TAG = "ItemDetailsActivity";

    NfcAdapter nfcAdapter;
    boolean writing = false;

    CollectionViewModel collectionViewModel;

    private ImageView imageView;
    private TextView nameText;
    private TextView numCollected;
    private Button button;

    private ArrayList<CollectionListData> myDataCollection;
    private int itemIndex;

    private AlertDialog dialog;

    private MusicSingleton myMediaPlayer;
    private boolean hasFocus;

    private SoundPool soundPool;
    private boolean soundLoaded;

    private int shareSound;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            itemIndex = extras.getInt("Item_index");
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        collectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        collectionViewModel.getCollectionListData().observe(this, this::initialiseViews);
        Log.i(TAG, "onCreate: " + collectionViewModel.getImageId(itemIndex));
        imageView = findViewById(R.id.details_candy_image);
        nameText = findViewById(R.id.details_candy_name);
        numCollected = findViewById(R.id.details_num_collected);
        button = findViewById(R.id.share_button);
        myMediaPlayer = MusicSingleton.getInstance(this);
//        myMediaPlayer.playMusic();
//        initialiseViews();

        loadSound();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initialiseViews(ArrayList<CollectionListData> collectionListData) {
        myDataCollection = Objects.requireNonNull(collectionViewModel.getNonZeroListData().getValue());
        if (myDataCollection.size() < itemIndex + 1) {
            itemIndex = myDataCollection.size() - 1;
            onBackPressed();
        } else {
            imageView.setImageResource(myDataCollection.get(itemIndex).getImageId());
            nameText.setText(myDataCollection.get(itemIndex).getDescription());
            numCollected.setText(String.valueOf(myDataCollection.get(itemIndex).getNumCollected()));
            button.setOnClickListener(view -> setWriteMode());
        }

    }

    private void loadSound() {
        //Soundpool
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.
                        USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundLoaded = true);
        shareSound = soundPool.load(this, R.raw.send, 1);

        soundLoaded = true;
    }

    public void setWriteMode() {
        if (myDataCollection.get(itemIndex).getNumCollected() != 0 && myDataCollection.size() > 1) {
            writing = !writing;
            showPopup();
//            Toast.makeText(this, writing ? "Sharing!" : "Not Sharing!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You can't share what you don't have!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasFocus)
            myMediaPlayer.pauseMusic();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }

    }

    @Override
    public void onBackPressed() {
        hasFocus = false;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasFocus = true;
        myMediaPlayer.playMusic();
        if (nfcAdapter != null) {
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }

    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (writing) {
            boolean sent = Utils.writeNFC(tag, String.valueOf(myDataCollection.get(itemIndex).getRealIndex()));
            if (sent) {
                runOnUiThread(() -> {
                    Log.i(TAG, "onTagDiscovered: tag discovered");
                    dialog.dismiss();
                    showPopupDone();
                    collectionViewModel.decrementCollected(myDataCollection.get(itemIndex).getRealIndex());
                    playSound(shareSound);
                });

                writing = false;
            }
        }
    }

    private void showPopup() {

        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_nfc, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        dialog = alert.create();
        LinearLayout layout = alertCustomDialog.findViewById(R.id.dialog_nfc_layout);
        TextView textView = alertCustomDialog.findViewById(R.id.dialog_nfc_text);
        Button myButton = alertCustomDialog.findViewById(R.id.recieve_done_button);

        myButton.setOnClickListener(view -> {
            writing = false;
            dialog.dismiss();

        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showPopupDone() {

        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_nfc_done, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        dialog = alert.create();
        LinearLayout layout = alertCustomDialog.findViewById(R.id.dialog_nfc_layout);
        TextView textView = alertCustomDialog.findViewById(R.id.dialog_nfc_text_done);
        ImageView imageView = alertCustomDialog.findViewById(R.id.receive_image);
        Button myButton = alertCustomDialog.findViewById(R.id.receive_done_button_ok);
        imageView.setImageResource(R.drawable.ic_round_check_24);
        textView.setText(String.format("You successfully sent a\n%s", collectionViewModel.getItemDescription(myDataCollection.get(itemIndex).getRealIndex())));

        myButton.setOnClickListener(view -> {
            writing = false;
            dialog.dismiss();

        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
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
}