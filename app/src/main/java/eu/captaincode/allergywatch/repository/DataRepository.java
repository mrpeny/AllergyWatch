package eu.captaincode.allergywatch.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;

import eu.captaincode.allergywatch.AppExecutors;
import eu.captaincode.allergywatch.api.OffWebService;
import eu.captaincode.allergywatch.api.ProductSearchResponse;
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

    private static final int FRESH_TIMEOUT_IN_MINUTES = 1;
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v0/";
    private static final int CODE_PRODUCT_FOUND = 1;
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
                    mOffWebService.getProduct(code).enqueue(new Callback<ProductSearchResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<ProductSearchResponse> call,
                                               @NonNull final Response<ProductSearchResponse> response) {
                            Log.i(TAG, "Data refreshed from the network");
                            mExecutors.networkIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    ProductSearchResponse productSearchResponse = response.body();
                                    if (productSearchResponse != null &&
                                            productSearchResponse.getStatus() == CODE_PRODUCT_FOUND) {

                                        Product refreshedProduct = productSearchResponse.getProduct();
                                        refreshedProduct.setLastRefresh(new Date());
                                        Log.i(TAG, "Saving product with code: " +
                                                refreshedProduct.getCode());
                                        mProductDao.save(refreshedProduct);
                                    } else {
                                        Log.d(TAG, "Search result was null or returned with error");
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Call<ProductSearchResponse> call,
                                              @NonNull Throwable t) {
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
