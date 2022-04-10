package com.example.candyfisher.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candyfisher.R;
import com.example.candyfisher.interfaces.CollectionAccessContract;
import com.example.candyfisher.models.CollectionListAdapter;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.presenter.MyCollectionPresenter;

import android.os.Bundle;
import android.view.View;

public class CollectionActivity extends AppCompatActivity implements CollectionAccessContract.CollectionView {


    CollectionAccessContract.CollectionPresenter myCollectionPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        myCollectionPresenter = new MyCollectionPresenter(this, this);
        myCollectionPresenter.initialisePresenter();
    }

    public void increaseCandy(View view) {
    }

    @Override
    public void initialiseView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.collection_view);
        CollectionListAdapter collectionListAdapter = new CollectionListAdapter(myCollectionPresenter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(collectionListAdapter);

    }

}