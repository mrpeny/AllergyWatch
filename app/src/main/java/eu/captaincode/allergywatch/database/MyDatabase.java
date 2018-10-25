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
import eu.captaincode.allergywatch.database.dao.ProductDao;
import eu.captaincode.allergywatch.database.entity.Product;

@Database(entities = {Product.class}, version = 1)
@TypeConverters({DateConverter.class, AllergenListConverter.class})
public abstract class MyDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "allergy_watch_db.db";
    // TODO: Remove from release version
    private static final Long CODE_PRODUCT = 3017620429484L;
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
                        executors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(4000);
                                } catch (InterruptedException ignored) {
                                }
                                insertInitialData(context, executors);
                            }
                        });
                    }
                }).build();
    }

    private static void insertInitialData(Context context, AppExecutors executors) {
        final MyDatabase database = MyDatabase.getInstance(context.getApplicationContext(), executors);
        database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                Product product = new Product();
                product.setProductName("Fake Nutella");
                product.setLastRefresh(new Date());
                product.setUserRating(Product.UserRating.DANGEROUS);
                product.setCode(CODE_PRODUCT);

                Product product2 = new Product();
                product2.setProductName("Fake Banana");
                product2.setLastRefresh(new Date());
                product2.setCode(123456789L);
                database.productDao().saveBoth(product, product2);
            }
        });
    }

    public abstract ProductDao productDao();

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    private void updateDatabaseCreated(Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }
}
