package eu.captaincode.allergywatch.database;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Date;

import eu.captaincode.allergywatch.AppExecutors;
import eu.captaincode.allergywatch.database.converter.AllergenListConverter;
import eu.captaincode.allergywatch.database.converter.DateConverter;
import eu.captaincode.allergywatch.database.converter.RatingConverter;
import eu.captaincode.allergywatch.database.dao.ProductDao;
import eu.captaincode.allergywatch.database.dao.ProductRatingDao;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.database.entity.ProductRating;

@Database(entities = {Product.class, ProductRating.class}, version = 1, exportSchema = false)
@TypeConverters({AllergenListConverter.class, DateConverter.class, RatingConverter.class})
public abstract class MyDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "allergy_watch_db.db";
    private static final Object LOCK = new Object();
    private static MyDatabase sInstance;
    private MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static MyDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = buildDatabase(context, executors);
                sInstance.updateDatabaseCreated(context);
            }
        }
        return sInstance;
    }

    private static MyDatabase buildDatabase(final Context context, final AppExecutors executors) {

        return Room.databaseBuilder(context.getApplicationContext(),
                MyDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        executors.diskIO().execute(() -> {
                            // TODO: Comment this to prevent inserting initial data into the DB
                            insertInitialData(context, executors);
                        });
                    }
                }).build();
    }

    private static void insertInitialData(Context context, AppExecutors executors) {
        final MyDatabase database = MyDatabase.getInstance(context.getApplicationContext(), executors);
        database.runInTransaction(() -> {
            Product kitKat = new Product();
            kitKat.setProductName("Locally persisted Kitkat");
            kitKat.setLastRefresh(new Date());
            kitKat.setCode(7613034968340L);

            Product nutella = new Product();
            nutella.setProductName("Nutella to refresh");
            nutella.setLastRefresh(new Date());
            nutella.setCode(80177128L);
            database.productDao().saveBoth(kitKat, nutella);
        });
    }

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    private void updateDatabaseCreated(Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public abstract ProductDao productDao();

    public abstract ProductRatingDao productRatingDao();
}
