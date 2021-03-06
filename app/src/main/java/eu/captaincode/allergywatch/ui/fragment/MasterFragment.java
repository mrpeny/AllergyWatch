package eu.captaincode.allergywatch.ui.fragment;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
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
    public static final String KEY_LIST_TYPE = "list-type";
    public static final int LIST_TYPE_HISTORY = 0;
    public static final int LIST_TYPE_SAFE = 1;
    public static final int LIST_TYPE_DANGEROUS = 2;

    private static final String TAG = MasterFragment.class.getSimpleName();
    private FragmentMasterBinding mBinding;
    private MainViewModel mViewModel;

    private int selectedListType;

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

        setTitle();
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();

        return mBinding.getRoot();
    }

    private void setTitle() {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            switch (selectedListType) {
                case LIST_TYPE_HISTORY:
                    parentActivity.setTitle(R.string.history);
                    break;
                case LIST_TYPE_SAFE:
                    parentActivity.setTitle(R.string.safe_foods);
                    break;
                case LIST_TYPE_DANGEROUS:
                    parentActivity.setTitle(R.string.dangerous_foods);
                    break;
            }
        }
    }

    private void setupViewModel() {
        MainViewModelFactory viewModelFactory =
                new MainViewModelFactory(Objects.requireNonNull(getActivity()).getApplication());
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        mBinding.setMainViewModel(mViewModel);
    }

    private void setupRecyclerView() {
        Context context = getContext();
        assert context != null;
        final ProductListAdapter productListAdapter = new ProductListAdapter(context,
                (ProductListAdapter.ProductClickListener) getActivity());
        mBinding.rvProductList.setAdapter(productListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
                layoutManager.getOrientation());
        mBinding.rvProductList.setLayoutManager(layoutManager);
        mBinding.rvProductList.addItemDecoration(dividerItemDecoration);

        loadProducts(productListAdapter);
    }

    private void loadProducts(final ProductListAdapter productListAdapter) {
        LiveData<List<Product>> productList = null;
        if (selectedListType == LIST_TYPE_HISTORY) {
            productList = mViewModel.getAllProducts();
        } else if (selectedListType == LIST_TYPE_SAFE) {
            productList = mViewModel.getSafeProducts();
        } else if (selectedListType == LIST_TYPE_DANGEROUS) {
            productList = mViewModel.getDangerousProducts();
        }

        if (productList != null) {
            productList.observe(this, newProducts -> {
                if (newProducts == null || newProducts.isEmpty()) {
                    mViewModel.isListEmpty.setValue(true);
                } else {
                    mViewModel.isListEmpty.setValue(false);
                }
                productListAdapter.swapData(newProducts);
            });
        }
    }

    private void setupSwipeRefresh() {
        mBinding.swiperefresh.setOnRefreshListener(() -> new RefreshProductsTask().execute());
    }

    class RefreshProductsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "Refreshing database from the network...");
            mBinding.swiperefresh.setRefreshing(true);
            mViewModel.refreshProducts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "Database network refresh finished");
            mBinding.swiperefresh.setRefreshing(false);
        }

    }

}
