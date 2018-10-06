package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.databinding.ObservableField;

import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.repository.DataRepository;

public class ProductViewModel extends AndroidViewModel {

    private Application mApplication;
    private DataRepository mRepository;
    private String mCode;

    private LiveData<Product> mProductLiveData;

    public ObservableField<Product> observableProduct = new ObservableField<>();

    public ProductViewModel(Application application, DataRepository repository, String code) {
        super(application);
        this.mApplication = application;
        this.mRepository = repository;
        this.mCode = code;
        init(code);
    }

    public void init(String code) {
/*        if (this.mProductLiveData != null) {
            return;
        }*/

        mProductLiveData = mRepository.getProduct(mCode);
    }

    public LiveData<Product> getProductLiveData() {
        return this.mProductLiveData;
    }

    public void setObeservableProduct(Product product) {
        this.observableProduct.set(product);
    }
}
