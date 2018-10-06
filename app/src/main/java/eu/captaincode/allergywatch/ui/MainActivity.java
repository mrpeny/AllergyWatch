package eu.captaincode.allergywatch.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import eu.captaincode.allergywatch.AllergyWatchApp;
import eu.captaincode.allergywatch.R;
import eu.captaincode.allergywatch.database.entity.Product;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }*/

        LiveData<Product> productLiveData = ((AllergyWatchApp) getApplication()).getRepository().getProduct("737628064502");
        Product product = productLiveData.getValue();
        productLiveData.observe(this, new Observer<Product>() {
            @Override
            public void onChanged(@Nullable Product product) {
                String name = "No name";
                if (product != null) {
                    name = product.getProductName();
                }

                Toast.makeText(MainActivity.this, "Product: "+ name, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
