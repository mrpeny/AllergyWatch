package eu.captaincode.allergywatch.repository;

import android.arch.lifecycle.LiveData;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;

import eu.captaincode.allergywatch.AllergyWatchApp;
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

    private static int FRESH_TIMEOUT_IN_MINUTES = 3;
    private static String BASE_URL = "https://world.openfoodfacts.org/api/v0/";
    private static DataRepository sInstance;

    private final MyDatabase mDatabase;
    private final OffWebService mOffWebService;
    private final ProductDao mProductDao;
    private final Retrofit mRetrofit;
    private final Executor mExecutor;

    public DataRepository(MyDatabase database, Executor executor) {
        this.mDatabase = database;
        this.mExecutor = executor;
        this.mProductDao = database.productDao();
        this.mRetrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .baseUrl(BASE_URL)
                .build();
        this.mOffWebService = mRetrofit.create(OffWebService.class);
    }

    public static DataRepository getInstance(MyDatabase database, Executor executor) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database, executor);
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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean productExists = (mProductDao.hasProduct(
                        code, DataRepository.this.getMaxRefreshTime(new Date())) != null);

                if (!productExists) {
                    mOffWebService.getProduct(code).enqueue(new Callback<Product>() {
                        @Override
                        public void onResponse(Call<Product> call, final Response<Product> response) {
                            Toast.makeText(AllergyWatchApp.context, "Refreshed from network",
                                    Toast.LENGTH_SHORT).show();
                            mExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Product product = response.body();
                                    product.setLastRefresh(new Date());
                                    // Product is parsed successfully at this point as I see
                                    mProductDao.save(product);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Product> call, Throwable t) {
                            Log.e(TAG, "Failed to connect to OFF Web API");
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
