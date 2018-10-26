package eu.captaincode.allergywatch.ui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.databinding.FragmentMasterBinding;
import eu.captaincode.allergywatch.ui.adapter.ProductListAdapter;
import eu.captaincode.allergywatch.viewmodel.MainViewModel;
import eu.captaincode.allergywatch.viewmodel.MainViewModelFactory;

public class MasterFragment extends Fragment {
    public static final int LIST_TYPE_HISTORY = 0;
    public static final int LIST_TYPE_SAFE = 1;
    public static final int LIST_TYPE_DANGEROUS = 2;
    private static final String KEY_LIST_TYPE = "list-type";
    private FragmentMasterBinding mBinding;
    private MainViewModel mViewModel;

    private int selectedListType;

    public MasterFragment() {
        // Required empty public constructor
    }


    public static MasterFragment newInstance(int listType) {
        MasterFragment fragment = new MasterFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedListType = getArguments().getInt(KEY_LIST_TYPE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_master, container, false);
        mBinding.setLifecycleOwner(this);

        setupViewModel();
        setupRecyclerView();

        return mBinding.getRoot();
    }

    private void setupViewModel() {
        MainViewModelFactory viewModelFactory =
                new MainViewModelFactory(Objects.requireNonNull(getActivity()).getApplication());
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
    }

    private void setupRecyclerView() {
        final ProductListAdapter productListAdapter = new ProductListAdapter(getContext(),
                (ProductListAdapter.ProductClickListener) getActivity());
        mBinding.rvProductList.setAdapter(productListAdapter);
        mBinding.rvProductList.setLayoutManager(new LinearLayoutManager(getContext()));

        loadProducts(productListAdapter);
    }

    private void loadProducts(final ProductListAdapter productListAdapter) {
        mViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(@Nullable List<Product> products) {
                productListAdapter.swapData(products);
            }
        });
    }

}
