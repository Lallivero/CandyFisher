package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.nfc.NfcAdapter;
import android.nfc.Tag;

import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.candyfisher.R;

import com.example.candyfisher.models.CollectionListData;
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

    private int itemIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            itemIndex = extras.getInt("Item_index");

        collectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        collectionViewModel.getCollectionListData().observe(this, this::initialiseViews);
        Log.i(TAG, "onCreate: " + String.valueOf(collectionViewModel.getImageId(itemIndex)));
        imageView = findViewById(R.id.details_candy_image);
        nameText = findViewById(R.id.details_candy_name);
        numCollected = findViewById(R.id.details_num_collected);
        button = findViewById(R.id.share_button);

//        initialiseViews();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initialiseViews(ArrayList<CollectionListData> collectionListData) {
        CollectionListData myData = Objects.requireNonNull(collectionViewModel.getNonZeroListData().getValue()).get(itemIndex);
        imageView.setImageResource(myData.getImageId());
        nameText.setText(myData.getDescription());
        numCollected.setText(String.valueOf(myData.getNumCollected()));
//        imageView.setImageResource(collectionViewModel.getImageId(itemIndex));
//        nameText.setText(collectionViewModel.getItemDescription(itemIndex));
//        numCollected.setText(String.valueOf(collectionViewModel.getNumCollected(itemIndex)));
        button.setOnClickListener(view -> setWriteMode());
    }

    public void setWriteMode() {
        if (collectionViewModel.getNumCollected(itemIndex) != 0) {
            writing = !writing;
            Toast.makeText(this, writing ? "Sharing!" : "Not Sharing!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "You can't share what you don't have!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
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
            boolean sent = Utils.writeNFC(tag, String.valueOf(itemIndex));
            if (sent) {
                runOnUiThread(() -> {
                    Log.i(TAG, "onTagDiscovered: here i am");
                    collectionViewModel.decrementCollected(itemIndex);
                });
                writing = false;
            }
        }
    }
}