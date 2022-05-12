package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
        myDataCollection = Objects.requireNonNull(collectionViewModel.getNonZeroListData().getValue());
        if(myDataCollection.size() < itemIndex + 1){
            itemIndex = myDataCollection.size()-1;
            onBackPressed();
        }else {
            imageView.setImageResource(myDataCollection.get(itemIndex).getImageId());
            nameText.setText(myDataCollection.get(itemIndex).getDescription());
            numCollected.setText(String.valueOf(myDataCollection.get(itemIndex).getNumCollected()));
//        imageView.setImageResource(collectionViewModel.getImageId(itemIndex));
//        nameText.setText(collectionViewModel.getItemDescription(itemIndex));
//        numCollected.setText(String.valueOf(collectionViewModel.getNumCollected(itemIndex)));
            button.setOnClickListener(view -> setWriteMode());
        }

    }

    public void setWriteMode() {
        if (myDataCollection.get(itemIndex).getNumCollected() != 0 && myDataCollection.size() > 1) {
            writing = !writing;
            showPopup();
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
            boolean sent = Utils.writeNFC(tag, String.valueOf(myDataCollection.get(itemIndex).getRealIndex()));
            if (sent) {
                runOnUiThread(() -> {
                    Log.i(TAG, "onTagDiscovered: tag discovered");

                    collectionViewModel.decrementCollected(myDataCollection.get(itemIndex).getRealIndex());
                });
                writing = false;
            }
        }
    }

    private void showPopup(){

        View alertCustomDialog = LayoutInflater.from(this).inflate(R.layout.dialog_layout_nfc, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertCustomDialog);
        final AlertDialog dialog = alert.create();
        LinearLayout layout = alertCustomDialog.findViewById(R.id.dialog_nfc_layout);
        TextView textView = alertCustomDialog.findViewById(R.id.dialog_nfc_text);
//        textView.setTextColor(getResources().getColor(R.color.gul));
//        layout.setBackground(getResources().getDrawable(R.drawable.dialog_background_blue));
        Button myButton = alertCustomDialog.findViewById(R.id.cancel_button);

        myButton.setOnClickListener(view -> {
            writing = false;
            dialog.dismiss();

        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}