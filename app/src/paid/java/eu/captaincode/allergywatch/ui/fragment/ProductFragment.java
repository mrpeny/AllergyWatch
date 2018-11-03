package eu.captaincode.allergywatch.ui.fragment;


import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.ProductRating;
import eu.captaincode.allergywatch.databinding.FragmentProductBinding;
import eu.captaincode.allergywatch.util.SnackbarUtils;
import eu.captaincode.allergywatch.viewmodel.ProductViewModel;
import eu.captaincode.allergywatch.viewmodel.ProductViewModelFactory;

public class ProductFragment extends Fragment {
    public static final String KEY_PRODUCT_CODE = "product_code";
    public static final String TAG_PRODUCT_FRAGMENT = "product-fragment";

    private FragmentProductBinding mBinding;
    private ProductViewModel mViewModel;
    private boolean mTwoPane;
    private int mSelectedListType;
    private Long mProductCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product, container, false);
        mBinding.setLifecycleOwner(this);

        getExtras();
        setupViewModel();
        subscribeUi(mViewModel);
        mTwoPane = getResources().getBoolean(R.bool.isTablet);

        subscribeUi(mViewModel);

        return mBinding.getRoot();
    }

    private void getExtras() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(KEY_PRODUCT_CODE)) {
            mProductCode = bundle.getLong(KEY_PRODUCT_CODE);
        }
        if (bundle != null && bundle.containsKey(MasterFragment.KEY_LIST_TYPE)) {
            mSelectedListType = bundle.getInt(MasterFragment.KEY_LIST_TYPE);
        }
    }

    private void setupViewModel() {
        ProductViewModelFactory viewModelFactory = new ProductViewModelFactory(
                Objects.requireNonNull(getActivity()).getApplication(), mProductCode);
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductViewModel.class);
    }

    private void subscribeUi(final ProductViewModel viewModel) {
        mBinding.setProductViewModel(viewModel);

        viewModel.getObservableProduct().observe(this, product -> {
            if (product != null) {
                viewModel.setProduct(product);
                viewModel.setProductFound(true);
            } else {
                viewModel.setProductFound(false);
            }
        });

        viewModel.getObservableProductRating().observe(this, productRating -> {
            if (productRating == null) {
                return;
            }
            ProductRating.Rating rating = productRating.getRating();
            if (rating == ProductRating.Rating.SAFE) {
                updateButtons(true);
            } else if (rating == ProductRating.Rating.DANGEROUS) {
                updateButtons(false);
            }
        });

        viewModel.getProductRatingChanged().observe(this, rating -> {
            if (mTwoPane && mSelectedListType != MasterFragment.LIST_TYPE_HISTORY) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .remove(this).commit();
                return;
            }
            if (rating == ProductRating.Rating.SAFE) {
                SnackbarUtils.showSnackbar(mBinding.clFragmentProduct,
                        getString(R.string.product_marked_safe));
                updateButtons(true);
            } else if (rating == ProductRating.Rating.DANGEROUS) {
                SnackbarUtils.showSnackbar(mBinding.clFragmentProduct,
                        getString(R.string.product_marked_dangerous));
                updateButtons(false);
            }
        });
    }

    private void updateButtons(boolean safe) {
        mBinding.btnSafe.setEnabled(!safe);
        mBinding.btnDangerous.setEnabled(safe);
    }

}
