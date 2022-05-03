package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candyfisher.R;

import com.example.candyfisher.models.CollectionListAdapter;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.viewModels.CollectionViewModel;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class CollectionActivity extends AppCompatActivity {


    private CollectionViewModel myCollectionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        myCollectionViewModel = new ViewModelProvider(this).get(CollectionViewModel.class);
        myCollectionViewModel.getCollectionListData().observe(this, this::initialiseView);

    }

    public void initialiseView(ArrayList<CollectionListData> collectionListData) {

        RecyclerView recyclerView = findViewById(R.id.collection_view);
        CollectionListAdapter collectionListAdapter = new CollectionListAdapter(collectionListData,
                index -> myCollectionViewModel.decrementCollected(index));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(collectionListAdapter);

    }
}