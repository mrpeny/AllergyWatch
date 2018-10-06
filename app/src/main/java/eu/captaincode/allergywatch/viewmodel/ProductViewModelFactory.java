package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import eu.captaincode.allergywatch.AllergyWatchApp;
import eu.captaincode.allergywatch.repository.DataRepository;

public class ProductViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    private final Application mApplication;
    private final String mCode;
    private final DataRepository mRepository;

    public ProductViewModelFactory(@NonNull Application application, String code) {
        this.mApplication = application;
        this.mCode = code;
        this.mRepository = ((AllergyWatchApp) application).getRepository();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProductViewModel(mApplication, mRepository, mCode);
    }
}
