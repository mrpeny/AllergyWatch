package eu.captaincode.allergywatch.ui.widget;

import android.app.Application;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import eu.captaincode.allergywatch.AllergyWatchApp;
import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;
import eu.captaincode.allergywatch.ui.ProductActivity;

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private AllergyWatchApp mAllergyWatchApp;
    private List<Product> mProductList = new ArrayList<>();

    ListRemoteViewsFactory(Application application) {
        this.mAllergyWatchApp = (AllergyWatchApp) application;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        mProductList = mAllergyWatchApp.getDatabase().productDao().findAllProducts();

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mProductList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(mAllergyWatchApp.getPackageName(),
                R.layout.widget_food_item);
        Product product = mProductList.get(position);

        populateTextViews(product, remoteViews);
        setOnClickIntent(product, remoteViews);

        return remoteViews;
    }

    private void populateTextViews(Product product, RemoteViews remoteViews) {
        remoteViews.setTextViewText(R.id.tv_food_item_name, product.getProductName());
        remoteViews.setTextViewText(R.id.tv_food_item_quantity, product.getQuantity());
    }

    private void setOnClickIntent(Product product, RemoteViews remoteViews) {
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(ProductActivity.KEY_PRODUCT_CODE, product.getCode());
        remoteViews.setOnClickFillInIntent(R.id.ll_food_item_container, fillInIntent);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
