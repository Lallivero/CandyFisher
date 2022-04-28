package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.candyfisher.R;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    public void toFishingActivity(View view) {
        Intent intent = new Intent(this, FishingActivity.class);
        startActivity(intent);
    }

    public void toCollection(View view){
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

}