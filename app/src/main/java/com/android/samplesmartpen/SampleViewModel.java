package com.android.samplesmartpen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SampleViewModel extends ViewModel {

    private final MutableLiveData<String> data = new MutableLiveData<>();

    public void updatedata(String address) {
        data.setValue(address);
    }

    public LiveData<String> getAddress() {
        return data;
    }
}
