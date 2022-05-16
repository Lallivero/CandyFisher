package com.example.candyfisher.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.candyfisher.models.CollectionListData;
import com.example.candyfisher.models.SharedPreferenceAccess;
import com.example.candyfisher.utils.Candies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SettingsViewModel extends AndroidViewModel {

    private MutableLiveData<HashMap<String, Boolean>> settings;

    //Populates MutableLiveData with data from sharedPreferences
    public SettingsViewModel(Application application) {
        super(application);
        SharedPreferenceAccess.initialise(getApplication());
    }

    //Returns an immutable version of the data
    public LiveData<HashMap<String, Boolean>> getSettings() {
        if (settings == null) {
            settings = new MutableLiveData<>();
            loadData();
        }

        return settings;
    }

    //Loads data from sharedPreferences
    private void loadData() {
        settings.setValue(SharedPreferenceAccess.getSettings());
    }

    public void swapSetting(String setting){
        SharedPreferenceAccess.swapSetting(setting);
        loadData();
    }
}
