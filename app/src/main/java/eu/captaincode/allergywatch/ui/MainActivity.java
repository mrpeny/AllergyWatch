package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import eu.captaincode.allergywatch.AppExecutors;
import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.MyDatabase;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.frament.ProductFragment;
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
/*        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }*/

/*        LiveData<Product> productLiveData = ((AllergyWatchApp) getApplication()).getRepository().getProduct(CODE_PRODUCT);
        Product product = productLiveData.getValue();
        productLiveData.observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                String name = "No name";
                if (product != null) {
                    name = product.getProductName();
                }

                Toast.makeText(MainActivity.this, "Product: "+ name, Toast.LENGTH_SHORT).show();
            }
        });*/

        final TextView textView = findViewById(R.id.tv_main);

        mDatabase = MyDatabase.getInstance(getApplicationContext(), AppExecutors.getInstance());

        ProductViewModelFactory viewModelFactory = new ProductViewModelFactory(getApplication(), CODE_PRODUCT);
        final ProductViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductViewModel.class);

        mBinding.setProductViewModel(viewModel);

        subscribeToModel(viewModel);


        /*ProductViewModelFactory2 factory2 = new ProductViewModelFactory2(mDatabase, CODE_PRODUCT);

        final ProductViewModel2 viewModel2 = ViewModelProviders.of(this, factory2)
                .get(ProductViewModel2.class);

        viewModel2.getProductLiveData().observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                if (product != null) {
                    textView.setText(product.getProductName());
                }
            }
        });*/

        //showFragment(savedInstanceState);
    }

    private void subscribeToModel(final ProductViewModel viewModel) {
        viewModel.getProductLiveData().observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                viewModel.setObeservableProduct(product);
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
