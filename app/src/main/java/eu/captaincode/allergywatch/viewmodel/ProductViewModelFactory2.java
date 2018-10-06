package eu.captaincode.allergywatch.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import eu.captaincode.allergywatch.database.MyDatabase;

public class ProductViewModelFactory2 implements ViewModelProvider.Factory {


    private final String mCode;
    private final MyDatabase mDatabase;

    public ProductViewModelFactory2(MyDatabase database, String code) {
        this.mDatabase = database;
        this.mCode = code;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProductViewModel2(mDatabase, mCode);
    }
}
