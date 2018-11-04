package eu.captaincode.allergywatch.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
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
    private boolean mStartedFromWidget;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupTransitions();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);

        setupNavigationDrawer();
        mTwoPane = getResources().getBoolean(R.bool.isTablet);
        showMasterFragment(MasterFragment.LIST_TYPE_HISTORY);
        mBinding.fab.setOnClickListener(v -> startCameraActivityForResult());
        showProductIfStartedFromWidget();
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, mBinding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mBinding.navView.getMenu().findItem(R.id.nav_history).setChecked(true);
        mBinding.navView.setNavigationItemSelectedListener(this);
    }

    private void showMasterFragment(int listType) {
        MasterFragment fragment = MasterFragment.newInstance(listType);
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.inclProductList.frameLayout.getId(), fragment, null)
                .commit();
    }

    private void setupTransitions() {
        /*getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);*/
        Explode transition = new Explode();
        transition.setDuration(500);
        getWindow().setExitTransition(transition);
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
                showProductDetails(Long.valueOf(detectedBarcode));
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
        showProductDetails(code);
    }

    private void showProductDetails(Long code) {
        if (mTwoPane) {
            showProductFragment(code);
        } else {
            if (mStartedFromWidget) {
                launchProductActivity(code, null);
            } else {
                launchProductActivityWithTransition(code);
            }
        }
    }

    private void launchProductActivity(Long barcode, Bundle bundle) {
        Intent launchProductActivityIntent = new Intent(this, ProductActivity.class);
        launchProductActivityIntent.putExtra(ProductActivity.KEY_PRODUCT_CODE, barcode);
        if (bundle != null) {
            startActivity(launchProductActivityIntent, bundle);
        } else {
            startActivity(launchProductActivityIntent);
        }
    }

    private void launchProductActivityWithTransition(Long barcode) {
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        launchProductActivity(barcode, bundle);
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

/*    private void showProductIfStartedFromWidget() {
        Bundle bundle = getIntent().getExtras();
        Long productCode;
        if (bundle != null && bundle.containsKey(ProductActivity.KEY_PRODUCT_CODE)) {
            productCode = bundle.getLong(ProductActivity.KEY_PRODUCT_CODE);
            // Wait of transitions to be configured to avoiding NPE when started from widget
            new Handler().postDelayed(() -> showProductDetails(productCode), 500);

        }
    }*/

    private void showProductIfStartedFromWidget() {
        Bundle bundle = getIntent().getExtras();
        Long productCode;
        if (bundle != null && bundle.containsKey(ProductActivity.KEY_PRODUCT_CODE)) {
            productCode = bundle.getLong(ProductActivity.KEY_PRODUCT_CODE);
            mStartedFromWidget = true;
            showProductDetails(productCode);

        }
    }

}
