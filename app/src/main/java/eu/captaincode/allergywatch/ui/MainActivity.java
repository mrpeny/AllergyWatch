package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.MyDatabase;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;
import eu.captaincode.allergywatch.viewmodel.ProductViewModel;
import eu.captaincode.allergywatch.viewmodel.ProductViewModelFactory;

public class MainActivity extends FragmentActivity {
    public static final String CODE_PRODUCT = "737628064502";

    private MyDatabase mDatabase;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ProductViewModelFactory viewModelFactory = new ProductViewModelFactory(getApplication(), CODE_PRODUCT);
        final ProductViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductViewModel.class);

        mBinding.setProductViewModel(viewModel);

        subscribeToModel(viewModel);

    }

    private void subscribeToModel(final ProductViewModel viewModel) {
        viewModel.getObservableProduct().observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                // Here I ALWAYS get null product
                viewModel.setProduct(product);
            }
        });
    }

    private void showFragment(Bundle savedInstanceState) {
        ProductFragment fragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ProductFragment.KEY_PRODUCT_CODE, CODE_PRODUCT);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, null)
                .commit();

    }

}
