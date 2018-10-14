package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.databinding.ObservableField;

import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.repository.DataRepository;

public class ProductViewModel extends AndroidViewModel {

    private final LiveData<Product> mObservableProduct;
    public ObservableField<Product> product = new ObservableField<>();
    public ObservableField<Boolean> productFound = new ObservableField<>();

    public Long mCode;

    ProductViewModel(Application application, DataRepository repository, final Long code) {
        super(application);
        this.mCode = code;
        this.mObservableProduct = repository.getProduct(mCode);
        productFound.set(true);
    }

    public LiveData<Product> getObservableProduct() {
        return this.mObservableProduct;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    public void setProductFound(Boolean found) {
        this.productFound.set(found);
    }
}
