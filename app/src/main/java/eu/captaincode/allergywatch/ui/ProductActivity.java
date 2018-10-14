package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.ActivityProductBinding;
import eu.captaincode.allergywatch.viewmodel.ProductViewModel;
import eu.captaincode.allergywatch.viewmodel.ProductViewModelFactory;

public class ProductActivity extends AppCompatActivity {
    public static final String KEY_PRODUCT_CODE = "product_code";

    private ActivityProductBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_product);

        Bundle bundle = getIntent().getExtras();
        Long productCode = 0L;
        if (bundle != null && bundle.containsKey(KEY_PRODUCT_CODE)) {
            productCode = bundle.getLong(KEY_PRODUCT_CODE);
        }

        ProductViewModelFactory viewModelFactory = new ProductViewModelFactory(getApplication(),
                productCode);
        final ProductViewModel viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(ProductViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(final ProductViewModel viewModel) {
        mBinding.setProductViewModel(viewModel);

        viewModel.getObservableProduct().observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                if (product != null) {
                    viewModel.setProduct(product);
                    viewModel.setProductFound(true);
                } else {
                    viewModel.setProductFound(false);
                }
            }
        });
    }

}
