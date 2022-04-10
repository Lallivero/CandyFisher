package com.example.candyfisher.presenter;

import android.content.Context;

import com.example.candyfisher.interfaces.CollectionAccessContract;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.models.SharedPreferenceAccess;
import com.example.candyfisher.utils.Candies;

import java.util.Locale;

public class MyCollectionPresenter implements CollectionAccessContract.CollectionPresenter {
    private final Context myContext;
    private final CollectionAccessContract.CollectionView myCollectionView;
    private CollectionListData[] collectionListData;



    public MyCollectionPresenter(Context context, CollectionAccessContract.CollectionView view) {
        myContext = context;
        myCollectionView = view;

    }

    @Override
    public void initialisePresenter() {

        SharedPreferenceAccess.initialise(myContext);
        int numberOfCandies = Candies.values().length;

        collectionListData = new CollectionListData[numberOfCandies];
        for(int i = 0; i < numberOfCandies; i++){
            String capitalisedCandyName = Candies.values()[i].toString();
            String formattedCandyName = capitalisedCandyName.charAt(0) + capitalisedCandyName.substring(1).toLowerCase(Locale.ROOT);

            collectionListData[i] = new CollectionListData(formattedCandyName, android.R.drawable.ic_dialog_email, SharedPreferenceAccess.getCandy(i));
        }
        myCollectionView.initialiseView();
    }

    @Override
    public void swapCollected(int index) {
        SharedPreferenceAccess.swapCollected(index);
    }

    @Override
    public int getImageId(int index) {
        return collectionListData[index].getImageId();
    }

    @Override
    public void onClick(int index) {
        collectionListData[index].swapCollected();
        SharedPreferenceAccess.swapCollected(index);
        myCollectionView.initialiseView();

    }

    public int getCollectionSize(){
        return collectionListData.length;
    }

    @Override
    public String getItemDescription(int index) {
        return collectionListData[index].getDescription();
    }

    @Override
    public Boolean getCollectedStatus(int index) {
        return collectionListData[index].getCollected();
    }
}
