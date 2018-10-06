package eu.captaincode.allergywatch.repository;

import android.arch.lifecycle.LiveData;

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
    private static int FRESH_TIMEOUT_IN_MINUTES = 3;
    private static String BASE_URL = "https://world.openfoodfacts.org/api/v0/";

    private static DataRepository sInstance;

    private final MyDatabase mDatabase;
    private final OffWebService mOffWebService;
    private final ProductDao mProductDao;
    private final Retrofit mRetrofit;
    private final AppExecutors mExecutors;

    public DataRepository(MyDatabase database, AppExecutors executors) {
        this.mDatabase = database;
        this.mExecutors = executors;
        this.mProductDao = database.productDao();

        mRetrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .baseUrl(BASE_URL)
                .build();
        mOffWebService = mRetrofit.create(OffWebService.class);

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
                boolean productExists =
                        (mProductDao.hasProduct(code, getMaxRefreshTime(new Date())) != null);

                if (!productExists) {
                    mOffWebService.getProduct(code).enqueue(new Callback<Product>() {
                        @Override
                        public void onResponse(Call<Product> call, final Response<Product> response) {
                            mExecutors.networkIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Product product = response.body();
                                    product.setLastRefresh(new Date());
                                    mProductDao.save(product);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Product> call, Throwable t) {

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
