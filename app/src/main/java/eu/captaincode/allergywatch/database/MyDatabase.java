package eu.captaincode.allergywatch.database;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import eu.captaincode.allergywatch.AppExecutors;
import eu.captaincode.allergywatch.database.converter.DateConverter;
import eu.captaincode.allergywatch.database.dao.ProductDao;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.ui.MainActivity;

@Database(entities = {Product.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class MyDatabase extends RoomDatabase {

    private static MyDatabase sInstance;
    private static final Object LOCK = new Object();

    public static final String DATABASE_NAME = "allergy_watch_db.db";
    private MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static MyDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        MyDatabase.class, DATABASE_NAME)
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                executors.diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(4000);
                                        } catch (InterruptedException ignored) {
                                        }
                                        Product product = new Product();
                                        product.setStatusVerbose("Dump status");
                                        product.setCode(MainActivity.CODE_PRODUCT);
                                        MyDatabase.getInstance(context.getApplicationContext(),
                                                executors).productDao().save(product);
                                    }
                                });
                            }
                        }).build();
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
                                //List<Product> products = new ArrayList<>();

                                //insertData(database, products);

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

    public abstract ProductDao productDao();

}
