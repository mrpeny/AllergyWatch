package eu.captaincode.allergywatch.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;

import eu.captaincode.allergywatch.AppExecutors;
import eu.captaincode.allergywatch.api.OffWebService;
import eu.captaincode.allergywatch.database.MyDatabase;
import eu.captaincode.allergywatch.database.dao.ProductDao;
import eu.captaincode.allergywatch.database.entity.Product;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataRepository {
    private static final String TAG = DataRepository.class.getSimpleName();

    private static final int FRESH_TIMEOUT_IN_MINUTES = 2;
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v0/";
    private static DataRepository sInstance;

    private final OffWebService mOffWebService;
    private final ProductDao mProductDao;
    private final AppExecutors mExecutors;

    private DataRepository(MyDatabase database, AppExecutors executors) {
        this.mExecutors = executors;
        this.mProductDao = database.productDao();
        Retrofit mRetrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .baseUrl(BASE_URL)
                .build();
        this.mOffWebService = mRetrofit.create(OffWebService.class);
    }

    public static DataRepository getInstance(MyDatabase database, AppExecutors executors) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database, executors);
                }
            }
        }
        return sInstance;
    }

    public LiveData<Product> getProduct(String code) {
        refreshProduct(code);
        return mProductDao.load(code);
    }

    private void refreshProduct(final String code) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Product product = mProductDao.hasProduct(
                        code, DataRepository.this.getMaxRefreshTime(new Date()));
                boolean productExists = (product != null);

                if (!productExists) {
                    mOffWebService.getProduct(code).enqueue(new Callback<Product>() {
                        @Override
                        public void onResponse(@NonNull Call<Product> call,
                                               @NonNull final Response<Product> response) {
                            Log.i(TAG, "Data refreshed from the network");
                            mExecutors.networkIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Product product = response.body();
                                    if (product != null) {
                                        Log.e(TAG, "Response body deserialization failed");
                                        product.setLastRefresh(new Date());
                                    }
                                    mProductDao.save(product);
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                            Log.e(TAG, "Failed to connect to OFF Web API:" + t.getMessage());
                            t.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private Date getMaxRefreshTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, -FRESH_TIMEOUT_IN_MINUTES);
        return calendar.getTime();
    }
}
