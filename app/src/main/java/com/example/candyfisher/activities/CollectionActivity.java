package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candyfisher.R;

import com.example.candyfisher.models.CollectionListAdapter;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.services.MusicSingleton;
import com.example.candyfisher.utils.Candies;
import com.example.candyfisher.utils.Utils;
import com.example.candyfisher.viewModels.CollectionViewModel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class CollectionActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private CollectionViewModel collectionViewModel;
    private boolean reading = false;
    private NfcAdapter nfcAdapter;
    private static final String TAG = "CollectionActivity";
    private AlertDialog dialog;
    private MusicSingleton myMediaPlayer;
    //    private ProgressBar spinner;
    private boolean hasFocus;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_collection);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        myMediaPlayer = MusicSingleton.getInstance(this);
        refreshUI();
        hasFocus = true;
    }

    private void refreshUI() {
        collectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        collectionViewModel.getCollectionListData().observe(this, this::initialiseView);
    }

    private void initialiseView(ArrayList<CollectionListData> collectionListData) {


        RecyclerView recyclerView = findViewById(R.id.collection_view);
        ArrayList<CollectionListData> nonZeroValueList = collectionViewModel.getNonZeroListData().getValue();
        CollectionListAdapter collectionListAdapter = new CollectionListAdapter(nonZeroValueList,
                this::toDetailsActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(collectionListAdapter);
    }

    public void toDetailsActivity(int index) {
        hasFocus = false;
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra("Item_index", index);
        startActivity(intent);
    }

    public void readClick(View view) {
        reading = !reading;
        showPopup();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasFocus = true;
        myMediaPlayer.playMusic();
        refreshUI();
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
    protected void onPause() {
        super.onPause();
        if (hasFocus)
            myMediaPlayer.pauseMusic();
        reading = false;
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
        getViewModelStore().clear();
    }

    @Override
    public void onBackPressed() {
        hasFocus = false;
        super.onBackPressed();

    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (reading) {
            String message = Utils.readNFC(tag);
            assert message != null;
            int itemIndex = Integer.parseInt(message);
            if (itemIndex < Candies.values().length) {
                runOnUiThread(() -> collectionViewModel.incrementCollected(itemIndex));
                Utils.writeNFC(tag, "");
            }
            dialog.dismiss();
            reading = false;
        }
    }

    private void showPopup() {
        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_nfc, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        dialog = alert.create();
        Button myButton = alertCustomDialog.findViewById(R.id.cancel_button);
        myButton.setOnClickListener(view -> {
            reading = false;
            dialog.dismiss();
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}
