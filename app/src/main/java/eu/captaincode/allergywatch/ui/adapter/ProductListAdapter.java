package eu.captaincode.allergywatch.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;


/**
 * Populates AdapterViews or RecyclerViews with a list of {@link Product}s.
 */
public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
    private Context mContext;
    private List<Product> mProductList = new ArrayList<>();
    private ProductClickListener mProductClickListener;
    private boolean mTwoPane;

    public ProductListAdapter(Context mContext, ProductClickListener listener, boolean twoPane) {
        this.mContext = mContext;
        this.mProductClickListener = listener;
        this.mTwoPane = twoPane;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.product_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = mProductList.get(position);
        holder.nameTextView.setText(product.getProductName());
        holder.barcodeTextView.setText(String.valueOf(product.getCode()));
    }

    @Override
    public int getItemCount() {
        return mProductList.size();
    }

    public void swapData(List<Product> newProducts) {
        mProductList.clear();
        mProductList.addAll(newProducts);
        notifyDataSetChanged();
    }

    public interface ProductClickListener {
        void onProductClicked(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView;
        private TextView barcodeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_product_item_name);
            barcodeTextView = itemView.findViewById(R.id.tv_product_item_barcode);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mProductClickListener.onProductClicked(position);
        }
    }
}
