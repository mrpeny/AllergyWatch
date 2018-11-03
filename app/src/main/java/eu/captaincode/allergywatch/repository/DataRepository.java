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
import eu.captaincode.allergywatch.database.dao.ProductRatingDao;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.database.entity.ProductRating;
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
    private final ProductRatingDao mProductRatingDao;
    private final AppExecutors mExecutors;

    private List<Product> mProducts = new ArrayList<>();
    private MutableLiveData<List<Product>> mutableProducts = new MutableLiveData<>();
    public LiveData<List<Product>> products = mutableProducts;

    private DataRepository(MyDatabase database, AppExecutors executors) {
        this.mExecutors = executors;
        this.mProductDao = database.productDao();
        this.mProductRatingDao = database.productRatingDao();
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

    public LiveData<List<Product>> getSafeProducts() {
        refreshProducts();
        return mProductDao.findAllObservableByRating(ProductRating.Rating.SAFE);
    }

    public LiveData<List<Product>> getDangerousProducts() {
        refreshProducts();
        return mProductDao.findAllObservableByRating(ProductRating.Rating.DANGEROUS);
    }

    public void refreshProducts() {
        mExecutors.diskIO().execute(() -> {
            List<Product> products = mProductDao.findAllProducts();
            for (Product product : products) {
                refreshProduct(product.getCode());
            }
        });
    }

    public List<Product> getAllProducts() {
        return mProductDao.findAllProducts();
    }

    public void update(final Product product) {
        mExecutors.diskIO().execute(() -> mProductDao.update(product));
    }

    public LiveData<Product> getProduct(Long code) {
        refreshProduct(code);
        return mProductDao.load(code);
    }

    private void refreshProduct(final Long code) {
        mExecutors.diskIO().execute(() -> {
            final Product oldProduct = mProductDao.loadByCode(code);

            final Product outdatedProduct = mProductDao.hasProduct(
                    code, DataRepository.this.getMaxRefreshTime(new Date()));
            boolean productExists = (outdatedProduct != null);

            if (!productExists) {
                mOffWebService.getProduct(code).enqueue(new Callback<ProductSearchResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProductSearchResponse> call,
                                           @NonNull final Response<ProductSearchResponse> response) {
                        Log.i(TAG, "Data fetched from the network");
                        mExecutors.networkIO().execute(() -> {
                            ProductSearchResponse productSearchResponse = response.body();
                            if (productSearchResponse != null) {
                                if (productSearchResponse.getStatus() == CODE_PRODUCT_FOUND) {
                                    Product refreshedProduct = productSearchResponse.getProduct();
                                    refreshedProduct.setLastRefresh(new Date());
                                    if (oldProduct != null) {
                                        refreshedProduct.setCreateDate(oldProduct.getCreateDate());
                                    }
                                    Log.i(TAG, "Saving product with code: " + refreshedProduct.getCode());
                                    mProductDao.save(refreshedProduct);
                                } else {
                                    Log.d(TAG, "Search result returned with status code: " +
                                            productSearchResponse.getStatus() + " and status message: " +
                                            productSearchResponse.getStatusVerbose());
                                }
                            } else {
                                Log.d(TAG, "Search result was null. No product saved.");
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
        });
    }

    public void saveProductRating(Long barcode, ProductRating.Rating rating) {
        final ProductRating productRating = new ProductRating();
        productRating.setBarcode(barcode);
        productRating.setRating(rating);
        mExecutors.diskIO().execute(() -> mProductRatingDao.save(productRating));
    }

    /* Helper methods */
    private Date getMaxRefreshTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, -FRESH_TIMEOUT_IN_MINUTES);
        return calendar.getTime();
    }

    public LiveData<ProductRating> getProductRating(Long code) {
        return mProductRatingDao.findBy(code);
    }
}
