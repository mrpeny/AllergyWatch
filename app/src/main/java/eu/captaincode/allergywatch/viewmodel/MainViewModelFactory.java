package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class MainViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    private final Application mApplication;

    public MainViewModelFactory(@NonNull Application application) {
        this.mApplication = application;

    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mApplication);
    }
}
