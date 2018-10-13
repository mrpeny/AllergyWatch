package eu.captaincode.allergywatch.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.databinding.ActivityMainBinding;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;

public class MainActivity extends FragmentActivity {

    public static final String CODE_PRODUCT = "30176204294844";

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        showFragment(savedInstanceState);

    }

    private void showFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            ProductFragment fragment = new ProductFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ProductFragment.KEY_PRODUCT_CODE, CODE_PRODUCT);
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, null)
                    .commit();
        }
    }

}
