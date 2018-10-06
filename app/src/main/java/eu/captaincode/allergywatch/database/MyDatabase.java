package eu.captaincode.allergywatch.database;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.captaincode.allergywatch.AppExecutors;
import eu.captaincode.allergywatch.database.converter.DateConverter;
import eu.captaincode.allergywatch.database.dao.ProductDao;
import eu.captaincode.allergywatch.database.entity.Product;

@Database(entities = {Product.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class MyDatabase extends RoomDatabase {

    private static MyDatabase sInstance;

    public static final String DATABASE_NAME = "allergy_watch_db.db";
    private MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public abstract ProductDao productDao();

    public static MyDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (MyDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context);
                }
            }
        }
        return sInstance;
    }

    private static MyDatabase buildDatabase(final Context applicationContext, final AppExecutors executors) {

        return Room.databaseBuilder(applicationContext, MyDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                MyDatabase database = MyDatabase.getInstance(applicationContext,
                                        executors);
                                // TODO: Initialize pre-defined data
                                List<Product> products = new ArrayList<>();

                                insertData(database, products);

                                database.setDatabaseCreated();
                            }
                        });

                    }
                }).build();
    }

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    private void updateDatabaseCreated(Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private static void insertData(final MyDatabase database, final List<Product> products) {
        database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                database.productDao().insertAll(products);
            }
        });
    }
}
