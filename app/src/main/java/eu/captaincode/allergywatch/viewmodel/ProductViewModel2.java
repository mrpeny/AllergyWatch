package eu.captaincode.allergywatch.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import eu.captaincode.allergywatch.database.MyDatabase;
import eu.captaincode.allergywatch.database.entity.Product;

public class ProductViewModel2 extends ViewModel {

    private LiveData<Product> mProductLiveData;

    public ProductViewModel2(MyDatabase database, String code) {

        mProductLiveData = database.productDao().load(code);
    }

    public LiveData<Product> getProductLiveData() {
        return mProductLiveData;
    }
}
