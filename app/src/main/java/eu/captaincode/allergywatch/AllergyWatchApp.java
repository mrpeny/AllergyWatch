package eu.captaincode.allergywatch;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.Executors;

import eu.captaincode.allergywatch.database.MyDatabase;
import eu.captaincode.allergywatch.repository.DataRepository;

public class AllergyWatchApp extends Application {

    private AppExecutors mAppExecutors;

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mAppExecutors = AppExecutors.getInstance();
    }

    public MyDatabase getDatabase() {
        return MyDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase(), Executors.newSingleThreadExecutor());
    }
}
