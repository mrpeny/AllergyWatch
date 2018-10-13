package eu.captaincode.allergywatch.ui.fragment;


import android.arch.lifecycle.Observer;
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
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.FragmentProductBinding;
import eu.captaincode.allergywatch.viewmodel.ProductViewModel;
import eu.captaincode.allergywatch.viewmodel.ProductViewModelFactory;

public class ProductFragment extends Fragment {
    public static final String KEY_PRODUCT_CODE = "product_code";

    private FragmentProductBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product, container, false);

        Bundle bundle = getArguments();
        String productCode = "";
        if (bundle != null && bundle.containsKey(KEY_PRODUCT_CODE)) {
            productCode = bundle.getString(KEY_PRODUCT_CODE);
        }

        ProductViewModelFactory viewModelFactory = new ProductViewModelFactory(
                Objects.requireNonNull(getActivity()).getApplication(), productCode);
        final ProductViewModel viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(ProductViewModel.class);

        subscribeUi(viewModel);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void subscribeUi(final ProductViewModel viewModel) {
        mBinding.setProductViewModel(viewModel);

        viewModel.getObservableProduct().observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                if (product != null) {
                    viewModel.setProduct(product);
                }
            }
        });
    }
}
