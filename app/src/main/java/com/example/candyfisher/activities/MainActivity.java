package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.candyfisher.R;
import com.example.candyfisher.interfaces.CollectionAccessContract;
import com.example.candyfisher.models.SharedPreferenceAccess;

public class MainActivity extends AppCompatActivity implements CollectionAccessContract.CollectionView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseView();
    }
    public void toFishingActivity(View view) {
        Intent intent = new Intent(this, FishingActivity.class);
        startActivity(intent);
    }

    public void toCollection(View view){
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

    @Override
    public void initialiseView() {

    }
}