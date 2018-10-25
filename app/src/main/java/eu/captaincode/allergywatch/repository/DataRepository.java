package eu.captaincode.allergywatch.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    private List<Product> mProducts = new ArrayList<>();
    private MutableLiveData<List<Product>> mutableProducts = new MutableLiveData<>();
    public LiveData<List<Product>> products = mutableProducts;

    private DataRepository(MyDatabase database, AppExecutors executors) {
        this.mExecutors = executors;
        this.mProductDao = database.productDao();
        Retrofit mRetrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .baseUrl(BASE_URL)
                .build();
        this.mOffWebService = mRetrofit.create(OffWebService.class);
        mutableProducts.postValue(mProducts);
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


    public LiveData<List<Product>> getProducts() {
        refreshProducts();
        return mProductDao.findAll();
    }

    public void refreshProducts() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Product> products = mProductDao.findAllProducts();
                for (Product product : products) {
                    refreshProduct(product.getCode());
                }
            }
        });
    }

    public List<Product> getAllProducts() {
        return mProductDao.findAllProducts();
    }

    public void update(final Product product) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mProductDao.update(product);

            }
        });
    }

    public LiveData<Product> getProduct(Long code) {
        refreshProduct(code);
        return mProductDao.load(code);
    }

    private void refreshProduct(final Long code) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final Product product = mProductDao.hasProduct(
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
