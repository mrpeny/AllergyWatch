package eu.captaincode.allergywatch.ui.frament;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.FragmentProductBinding;
import eu.captaincode.allergywatch.viewmodel.ProductViewModel;

public class ProductFragment extends Fragment {
    public static final String KEY_PRODUCT_CODE = "product_code";

    private ViewModelProvider.Factory mViewModelFactory;
    private ProductViewModel mViewModel;
    private FragmentProductBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*ViewModelProvider.Factory viewModelFactory = new ProductViewModelFactory(
                getActivity().getApplication(), getArguments().getString(KEY_PRODUCT_CODE));

        ProductViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(ProductViewModel.class);
        subscribeUi(viewModel);*/
    }

    private void subscribeUi(final ProductViewModel viewModel) {
        mBinding.setProductViewModel(viewModel);
        String code = getArguments().getString(KEY_PRODUCT_CODE);
        viewModel.init(code);
        viewModel.getProductLiveData().observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                if (product != null)
                mBinding.tvProductName.setText(product.getCode());
            }
        });


    }
}
