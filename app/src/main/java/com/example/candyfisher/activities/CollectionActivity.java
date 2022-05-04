package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candyfisher.R;

import com.example.candyfisher.models.CollectionListAdapter;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.utils.Candies;
import com.example.candyfisher.utils.Utils;
import com.example.candyfisher.viewModels.CollectionViewModel;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class CollectionActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private CollectionViewModel collectionViewModel;
    private boolean reading = false;
    private NfcAdapter nfcAdapter;
    private static final String TAG = "CollectionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        refreshUI();
    }

    private void refreshUI() {
        collectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        collectionViewModel.getCollectionListData().observe(this, this::initialiseView);
    }

    private void initialiseView(ArrayList<CollectionListData> collectionListData) {

        RecyclerView recyclerView = findViewById(R.id.collection_view);
//        CollectionListAdapter collectionListAdapter = new CollectionListAdapter(collectionListData,
//                index -> myCollectionViewModel.decrementCollected(index));
        CollectionListAdapter collectionListAdapter = new CollectionListAdapter(collectionListData,
                this::toDetailsActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(collectionListAdapter);
    }

    public void toDetailsActivity(int index) {
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra("Item_index", index);
        startActivity(intent);
    }

    public void readClick(View view) {
        reading = !reading;
        Toast.makeText(this, reading ? "Reading" : "Stopped Reading", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        reading = false;
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
        getViewModelStore().clear();
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (reading) {
            String message = Utils.readNFC(tag);
            assert message != null;
            int itemIndex = Integer.parseInt(message);
            if(itemIndex < Candies.values().length){
                runOnUiThread(() -> collectionViewModel.incrementCollected(itemIndex));
                Utils.writeNFC(tag, "");
            }
            reading = false;
        }
    }
}
