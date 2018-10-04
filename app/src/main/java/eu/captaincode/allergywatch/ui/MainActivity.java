package eu.captaincode.allergywatch.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import eu.captaincode.allergywatch.R;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }
    }

}
