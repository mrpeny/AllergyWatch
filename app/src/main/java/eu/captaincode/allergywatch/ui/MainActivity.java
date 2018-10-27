package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.adapter.ProductListAdapter;
import eu.captaincode.allergywatch.ui.fragment.MasterFragment;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;
import eu.captaincode.allergywatch.viewmodel.MainViewModel;
import eu.captaincode.allergywatch.viewmodel.MainViewModelFactory;

public class MainActivity extends AppCompatActivity implements
        ProductListAdapter.ProductClickListener, NavigationView.OnNavigationItemSelectedListener {

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

        setupNavigationDrawer();

        mTwoPane = getResources().getBoolean(R.bool.isTablet);

        MainViewModelFactory viewModelFactory = new MainViewModelFactory(getApplication());
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        showMasterFragment(MasterFragment.LIST_TYPE_HISTORY);

        mBinding.fab.setOnClickListener(v -> startCameraActivityForResult());
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, mBinding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mBinding.navView.getMenu().findItem(R.id.nav_history).setChecked(true);
        mBinding.navView.setNavigationItemSelectedListener(this);
        // TODO: Implement Always open NavDrawer
        // https://stackoverflow.com/questions/17133541/navigation-drawer-set-as-always-opened-on-tablets/50646711#50646711
    }

    private void showMasterFragment(int listType) {
        MasterFragment fragment = MasterFragment.newInstance(listType);
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.inclProductList.frameLayout.getId(), fragment, null)
                .commit();
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
                launchProductActivity(Long.valueOf(detectedBarcode));
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            startCameraActivityForResult();
        } else if (id == R.id.nav_safe_foods) {
            showMasterFragment(MasterFragment.LIST_TYPE_SAFE);
            removeProductFragment();
        } else if (id == R.id.nav_dangerous_foods) {
            showMasterFragment(MasterFragment.LIST_TYPE_DANGEROUS);
            removeProductFragment();
        } else if (id == R.id.nav_history) {
            showMasterFragment(MasterFragment.LIST_TYPE_HISTORY);
            removeProductFragment();
        }

        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void removeProductFragment() {
        if (mTwoPane) {
            Fragment productFragment = getSupportFragmentManager()
                    .findFragmentByTag(ProductFragment.TAG_PRODUCT_FRAGMENT);

            if (productFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(productFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // TODO: Change to subscription model
    // https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
    @Override
    public void onProductClicked(Long code) {
        if (mTwoPane) {
            showProductFragment(code);
        } else {
            launchProductActivity(code);
        }
    }

    private void launchProductActivity(Long barcode) {
        Intent launchProductActivityIntent = new Intent(this, ProductActivity.class);
        launchProductActivityIntent.putExtra(ProductActivity.KEY_PRODUCT_CODE, barcode);
        startActivity(launchProductActivityIntent);
    }

    private void showProductFragment(Long barcode) {
        ProductFragment fragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ProductFragment.KEY_PRODUCT_CODE, barcode);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.product_detail_container,
                        fragment,
                        ProductFragment.TAG_PRODUCT_FRAGMENT)
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