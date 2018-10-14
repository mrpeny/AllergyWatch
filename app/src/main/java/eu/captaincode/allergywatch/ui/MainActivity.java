package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.adapter.ProductListAdapter;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;
import eu.captaincode.allergywatch.viewmodel.MainViewModel;
import eu.captaincode.allergywatch.viewmodel.MainViewModelFactory;

public class MainActivity extends AppCompatActivity implements
        ProductListAdapter.ProductClickListener {

    public static final int REQUEST_CODE_BARCODE_DETECTION = 100;
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;
    private boolean mTwoPane;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);

        mTwoPane = getResources().getBoolean(R.bool.isTablet);

        MainViewModelFactory viewModelFactory = new MainViewModelFactory(getApplication());
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        setupRecyclerView();

        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraActivityForResult();
            }
        });

    }

    private void setupRecyclerView() {
        final ProductListAdapter productListAdapter = new ProductListAdapter(this, this, mTwoPane);
        mBinding.rvProductList.setAdapter(productListAdapter);

        mViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(@Nullable List<Product> products) {
                productListAdapter.swapData(products);
            }
        });
    }

    private void startCameraActivityForResult() {
        Intent barcodeDetectionIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(barcodeDetectionIntent, REQUEST_CODE_BARCODE_DETECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BARCODE_DETECTION) {
            if (resultCode == RESULT_OK) {
                String detectedBarcode = data.getStringExtra(CameraActivity.EXTRA_BARCODE);
                launchProductActivity(detectedBarcode);
            }
        }
    }

    @Override
    public void onProductClicked(Long code) {
        if (mTwoPane) {
            showProductFragment(String.valueOf(code));

        } else {
            launchProductActivity(String.valueOf(code));
        }
    }

    private void launchProductActivity(String barcode) {
        Intent launchProductActivityIntent = new Intent(this, ProductActivity.class);
        launchProductActivityIntent.putExtra(ProductActivity.KEY_PRODUCT_CODE,
                Long.valueOf(barcode));
        startActivity(launchProductActivityIntent);
    }

    private void showProductFragment(String barcode) {
        ProductFragment fragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ProductFragment.KEY_PRODUCT_CODE, barcode);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, null)
                .commitAllowingStateLoss();
    }


}

class RefreshProductsTask extends AsyncTask<Void, Void, Void> {
    private MainViewModel mViewModel;

    public RefreshProductsTask(MainViewModel viewModel) {
        this.mViewModel = viewModel;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // TODO: Show swipe refresh
        mViewModel.refreshProducts();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // TODO: Hide swipe refresh
    }

}