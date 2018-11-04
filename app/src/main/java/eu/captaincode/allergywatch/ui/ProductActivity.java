package eu.captaincode.allergywatch.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;

import java.util.Objects;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.databinding.ActivityProductBinding;
import eu.captaincode.allergywatch.ui.fragment.ProductFragment;

public class ProductActivity extends AppCompatActivity {
    public static final String KEY_PRODUCT_CODE = "product_code";

    private ActivityProductBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setAllowEnterTransitionOverlap(true);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_product);

        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.product_details_title);

        Bundle bundle = getIntent().getExtras();
        //setupTransitions();
        Long productCode = 0L;
        if (bundle != null && bundle.containsKey(KEY_PRODUCT_CODE)) {
            productCode = bundle.getLong(KEY_PRODUCT_CODE);
        }
        if (savedInstanceState == null) {
            showProductFragment(productCode);
        }
    }

    private void showProductFragment(Long barcode) {
        ProductFragment fragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ProductFragment.KEY_PRODUCT_CODE, barcode);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.product_detail_container, fragment, null)
                .commitAllowingStateLoss();
    }

    private void setupTransitions() {
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.END);
        getWindow().setEnterTransition(slide);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
