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

    //Populates MutableLiveData with data from sharedPreferences
    public CollectionViewModel(Application application) {
        super(application);
        SharedPreferenceAccess.initialise(getApplication());
//        if (collectionListData == null) {
//            collectionListData = new MutableLiveData<>();
//            loadData();
//        }
    }

    //Returns an immutable version of the data
    public LiveData<ArrayList<CollectionListData>> getCollectionListData() {
        if (collectionListData == null) {
            collectionListData = new MutableLiveData<>();
            loadData();
        }

        return collectionListData;
    }

    public LiveData<ArrayList<CollectionListData>> getNonZeroListData(){
        MutableLiveData<ArrayList<CollectionListData>> tempData = new MutableLiveData<>();
        tempData.setValue(new ArrayList<>());
        for (CollectionListData data : Objects.requireNonNull(getCollectionListData().getValue())) {
            if (data.getNumCollected() != 0) {
                Objects.requireNonNull(tempData.getValue()).add(data);
            }
        }
        return tempData;
    }

    //Loads data from sharedPreferences
    private void loadData() {
        collectionListData.setValue(candyToCollectionListData(SharedPreferenceAccess.getCandies()));
    }

    //Creates CollectionListData out of enum Candies
    private ArrayList<CollectionListData> candyToCollectionListData(ArrayList<Candies> candies) {
        ArrayList<CollectionListData> collection = new ArrayList<>();
        for (int i = 0; i < candies.size(); i++) {
            String capitalisedCandyName = Candies.values()[i].toString();
            String[] nameParts = capitalisedCandyName.split("_");
            StringBuilder finishedCandyName = new StringBuilder();
            for (int j = 0; j < nameParts.length; j++) {
                if (nameParts[j].equals("AND"))
                    finishedCandyName.append("&");
                else
                    finishedCandyName.append(nameParts[j].charAt(0)).append(nameParts[j].substring(1).toLowerCase());
                if (j != nameParts.length - 1)
                    finishedCandyName.append(" ");
            }
            String formattedCandyName = capitalisedCandyName.charAt(0) + capitalisedCandyName.substring(1).toLowerCase();
            String imageName = "candy" + (i + 1);
            collection.add(new CollectionListData(finishedCandyName.toString(), getImageFromString(imageName), SharedPreferenceAccess.getNumCollected(i)));
        }
        return collection;
    }

    //Generates an imageId from a string consisting of the image resources name
    private int getImageFromString(String imageName) {
        return getApplication().getResources().getIdentifier(imageName, "drawable", getApplication().getPackageName());
    }

    //Returns the imageId of a Candy
    public int getImageId(int index) {
        return Objects.requireNonNull(collectionListData.getValue()).get(index).getImageId();
    }

    //Returns the number of unique Candies
    public int getCollectionSize() {
        return Objects.requireNonNull(collectionListData.getValue()).size();
    }

    //Returns the description of a Candy
    public String getItemDescription(int index) {
        return Objects.requireNonNull(collectionListData.getValue()).get(index).getDescription();
    }

    public int getNumCollected(int index) {
        return Objects.requireNonNull(collectionListData.getValue()).get(index).getNumCollected();
    }

    public void incrementCollected(int index) {
        SharedPreferenceAccess.incrementCollected(index);
        loadData();

    }

    public void decrementCollected(int index) {
        SharedPreferenceAccess.decrementCollected(index);
        loadData();

    }


//    //Returns weather or not a candy has been collected
//    public Boolean getCollectedStatus(int index) {
//        return Objects.requireNonNull(collectionListData.getValue()).get(index).getCollected();
//    }

    //Set database values
    public void swapCollected(int index) {
        SharedPreferenceAccess.swapCollected(index);
        loadData();
    }

}
