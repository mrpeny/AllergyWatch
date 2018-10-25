package eu.captaincode.allergywatch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.repository.DataRepository;

public class ProductViewModel extends AndroidViewModel {

    private final LiveData<Product> mObservableProduct;
    public ObservableField<Product> product = new ObservableField<>();
    private ObservableField<Boolean> productFound = new ObservableField<>();

    private DataRepository repository;

    ProductViewModel(Application application, DataRepository repository, final Long code) {
        super(application);
        this.mObservableProduct = repository.getProduct(code);
        this.repository = repository;
        this.productFound.set(true);
    }

    public LiveData<Product> getObservableProduct() {
        return this.mObservableProduct;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    public void setProductFound(Boolean found) {
        this.productFound.set(found);
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.hazelnuts)
                .into(view);
    }

    public void onSafeButtonClicked() {
        Product product = this.product.get();
        if (product != null) {
            product.setUserRating(Product.UserRating.SAFE);
            repository.update(product);
        }

    }

    public void onDangerousButtonClicked() {
        Product product = this.product.get();
        if (product != null) {
            product.setUserRating(Product.UserRating.DANGEROUS);
            repository.update(product);
        }
    }
}
