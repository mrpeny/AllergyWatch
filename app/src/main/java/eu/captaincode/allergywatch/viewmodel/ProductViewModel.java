package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.database.entity.ProductRating;
import eu.captaincode.allergywatch.repository.DataRepository;
import eu.captaincode.allergywatch.service.WidgetUpdateService;
import eu.captaincode.allergywatch.ui.events.SingleLiveEvent;

public class ProductViewModel extends AndroidViewModel {

    private final LiveData<Product> mObservableProduct;
    private final LiveData<ProductRating> mObservableProductRating;
    public MutableLiveData<Product> product = new MutableLiveData<>();
    public ObservableField<Boolean> productFound = new ObservableField<>();
    private final SingleLiveEvent<ProductRating.Rating> mProductRatingChanged = new SingleLiveEvent<>();

    private DataRepository repository;
    private long code;

    ProductViewModel(Application application, DataRepository repository, final Long code) {
        super(application);
        this.code = code;
        this.mObservableProduct = repository.getProduct(code);
        this.mObservableProductRating = repository.getProductRating(code);
        this.repository = repository;
        this.productFound.set(true);
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.hazelnuts)
                .into(view);
    }

    public void onSafeButtonClicked() {
        Product product = this.product.getValue();
        if (product == null) {
            return;
        }
        ProductRating.Rating newRating = ProductRating.Rating.SAFE;
        repository.saveProductRating(product.getCode(), newRating);
        startWidgetUpdateService();
        mProductRatingChanged.setValue(newRating);
    }

    public void onDangerousButtonClicked() {
        Product product = this.product.getValue();
        if (product == null) {
            return;
        }
        ProductRating.Rating newRating = ProductRating.Rating.DANGEROUS;
        repository.saveProductRating(product.getCode(), newRating);
        startWidgetUpdateService();
        mProductRatingChanged.setValue(newRating);
    }

    private void startWidgetUpdateService() {
        Intent widgetUpdateIntent = new Intent(getApplication(), WidgetUpdateService.class);
        widgetUpdateIntent.setAction(WidgetUpdateService.ACTION_SAFE_FOOD_LIST_CHANGED);
        getApplication().startService(widgetUpdateIntent);
    }

    public LiveData<String> getAllergens() {
        return Transformations.map(product, productValue -> {
            if (TextUtils.isEmpty(productValue.getAllergensFromIngredients())) {
                return getApplication().getResources().getString(R.string.no_allergens_found);
            } else {
                return productValue.getAllergensFromIngredients();
            }
        });
    }

    public LiveData<String> getIngredients() {
        return Transformations.map(product, productValue -> {
            if (TextUtils.isEmpty(productValue.getIngredientsText())) {
                return getApplication().getResources().getString(R.string.no_ingredients_found);
            } else {
                return productValue.getIngredientsText();
            }
        });
    }

    public LiveData<Product> getObservableProduct() {
        return this.mObservableProduct;
    }

    public LiveData<ProductRating> getObservableProductRating() {
        return this.mObservableProductRating;
    }



    public void setProduct(Product product) {
        this.product.setValue(product);
    }

    public void setProductFound(Boolean found) {
        this.productFound.set(found);
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public SingleLiveEvent<ProductRating.Rating> getProductRatingChanged() {
        return mProductRatingChanged;
    }

    public void deleteSelectedProduct() {
        repository.deleteProduct(this.code);
    }

}
