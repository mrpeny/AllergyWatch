package eu.captaincode.allergywatch.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private final MutableLiveData<String> barcode = new MutableLiveData<>();

    public LiveData<String> getBarcode() {
        return this.barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue(barcode);
    }
}
