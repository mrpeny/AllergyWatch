package eu.captaincode.allergywatch;

import android.app.Application;

import eu.captaincode.allergywatch.database.MyDatabase;
import eu.captaincode.allergywatch.repository.DataRepository;

public class AllergyWatchApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = AppExecutors.getInstance();
    }

    public MyDatabase getDatabase() {
        return MyDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase(), mAppExecutors);
    }
}
