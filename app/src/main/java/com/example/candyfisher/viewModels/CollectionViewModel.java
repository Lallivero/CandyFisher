package com.example.candyfisher.viewModels;

import android.app.Application;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.candyfisher.R;
import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.models.SharedPreferenceAccess;
import com.example.candyfisher.utils.Candies;

import java.util.ArrayList;
import java.util.Objects;

public class CollectionViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<CollectionListData>> collectionListData;

    public CollectionViewModel(Application application) {
        super(application);
        SharedPreferenceAccess.initialise(getApplication());
        if (collectionListData == null) {
            collectionListData = new MutableLiveData<>();
            loadData();
        }
    }

    public LiveData<ArrayList<CollectionListData>> getCollectionListData() {

        return collectionListData;
    }

    private void loadData() {
        collectionListData.setValue(candyToCollectionListData(SharedPreferenceAccess.getCandies()));
    }

    private ArrayList<CollectionListData> candyToCollectionListData(ArrayList<Candies> candies) {
        ArrayList<CollectionListData> collection = new ArrayList<>();
        for (int i = 0; i < candies.size(); i++) {
            String capitalisedCandyName = Candies.values()[i].toString();
            String formattedCandyName = capitalisedCandyName.charAt(0) + capitalisedCandyName.substring(1).toLowerCase();
            String imageName = "candy" + (i+1);
            collection.add(new CollectionListData(formattedCandyName, getImageFromString(imageName), SharedPreferenceAccess.isCandyCollected(i)));
        }
        return collection;
    }

    private int getImageFromString(String imageName) {
        return getApplication().getResources().getIdentifier(imageName, "drawable", getApplication().getPackageName());
    }

    public int getImageId(int index) {
        return Objects.requireNonNull(collectionListData.getValue()).get(index).getImageId();
    }


    public int getCollectionSize() {
        return Objects.requireNonNull(collectionListData.getValue()).size();
    }


    public String getItemDescription(int index) {
        return Objects.requireNonNull(collectionListData.getValue()).get(index).getDescription();
    }


    public Boolean getCollectedStatus(int index) {
        return Objects.requireNonNull(collectionListData.getValue()).get(index).getCollected();
    }

    //Set database values
    public void swapCollected(int index) {
        SharedPreferenceAccess.swapCollected(index);
        loadData();
    }

}
