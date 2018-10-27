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
import eu.captaincode.allergywatch.databinding.FragmentProductBinding;
import eu.captaincode.allergywatch.viewmodel.ProductViewModel;
import eu.captaincode.allergywatch.viewmodel.ProductViewModelFactory;

public class ProductFragment extends Fragment {
    public static final String KEY_PRODUCT_CODE = "product_code";
    public static final String TAG_PRODUCT_FRAGMENT = "product-fragment";

    private FragmentProductBinding mBinding;
    private ProductViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product, container, false);
        mBinding.setLifecycleOwner(this);

        Bundle bundle = getArguments();
        Long productCode = 0L;
        if (bundle != null && bundle.containsKey(KEY_PRODUCT_CODE)) {
            productCode = bundle.getLong(KEY_PRODUCT_CODE);
        }

        ProductViewModelFactory viewModelFactory = new ProductViewModelFactory(
                Objects.requireNonNull(getActivity()).getApplication(), productCode);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductViewModel.class);

        subscribeUi(viewModel);

        return mBinding.getRoot();
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
    }

}
