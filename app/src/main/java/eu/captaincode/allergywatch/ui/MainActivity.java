package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;
import eu.captaincode.allergywatch.viewmodel.MainActivityViewModel;

public class MainActivity extends FragmentActivity {

    public static final String CODE_PRODUCT = "30176204294844";

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MainActivityViewModel mainActivityViewModel = ViewModelProviders.of(this)
                .get(MainActivityViewModel.class);
        mainActivityViewModel.getBarcode().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String newBarcode) {
                if (!TextUtils.isEmpty(newBarcode)) {
                    showProductFragment(newBarcode);
                }
            }
        });

        if (savedInstanceState == null) {
            showCameraFragment();
        }
    }

    private void showCameraFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance())
                .commit();
    }

    private void showProductFragment(String barcode) {
            ProductFragment fragment = new ProductFragment();
            Bundle bundle = new Bundle();
        bundle.putString(ProductFragment.KEY_PRODUCT_CODE, barcode);
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, fragment, null)
                    .commit();
    }

}
