package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import eu.captaincode.allergywatch.AllergyWatchApp;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.repository.DataRepository;

public class MainViewModel extends AndroidViewModel {

    private DataRepository mRepository;

    MainViewModel(Application application) {
        super(application);
        this.mRepository = ((AllergyWatchApp) application).getRepository();

    }

    public LiveData<List<Product>> getAllProducts() {
        return mRepository.getProducts();
    }

    public LiveData<List<Product>> getSafeProducts() {
        return mRepository.getSafeProducts();
    }

    public LiveData<List<Product>> getDangerousProducts() {
        return mRepository.getDangerousProducts();
    }

    public void refreshProducts() {
        mRepository.refreshProducts();
    }

}
