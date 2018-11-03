package eu.captaincode.allergywatch.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.adapter.ProductListAdapter;
import eu.captaincode.allergywatch.ui.fragment.MasterFragment;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;

public class MainActivity extends AppCompatActivity implements
        ProductListAdapter.ProductClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_CODE_BARCODE_DETECTION = 100;
    public static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;
    private boolean mTwoPane;
    private int mSelectedListType;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);

        setupNavigationDrawer();
        mTwoPane = getResources().getBoolean(R.bool.isTablet);
        showMasterFragment(MasterFragment.LIST_TYPE_HISTORY);
        mBinding.fab.setOnClickListener(v -> startCameraActivityForResult());
        showProductStartedFromWidget();
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
        Log.i(TAG, "Activity result received with resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE_BARCODE_DETECTION) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Barcode detection Activity result received successfully");
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
            mSelectedListType = MasterFragment.LIST_TYPE_SAFE;
            showMasterFragment(mSelectedListType);
            removeProductFragment();
        } else if (id == R.id.nav_dangerous_foods) {
            mSelectedListType = MasterFragment.LIST_TYPE_DANGEROUS;
            showMasterFragment(mSelectedListType);
            removeProductFragment();
        } else if (id == R.id.nav_history) {
            mSelectedListType = MasterFragment.LIST_TYPE_HISTORY;
            showMasterFragment(mSelectedListType);
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
        bundle.putInt(MasterFragment.KEY_LIST_TYPE, mSelectedListType);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.product_detail_container,
                        fragment,
                        ProductFragment.TAG_PRODUCT_FRAGMENT)
                .commitAllowingStateLoss();
    }

    private void showProductStartedFromWidget() {
        Bundle bundle = getIntent().getExtras();
        Long productCode;
        if (bundle != null && bundle.containsKey(ProductActivity.KEY_PRODUCT_CODE)) {
            productCode = bundle.getLong(ProductActivity.KEY_PRODUCT_CODE);
            onProductClicked(productCode);
        }
    }

}
