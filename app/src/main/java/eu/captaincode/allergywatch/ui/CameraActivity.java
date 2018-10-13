package eu.captaincode.allergywatch.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import eu.captaincode.allergywatch.R;

public class CameraActivity extends AppCompatActivity implements
        CameraFragment.BarcodeDetectionListener {

    public static final String EXTRA_BARCODE = "barcode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (savedInstanceState == null) {
            showCameraFragment();
        }
    }

    private void showCameraFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance())
                .commit();
    }

    @Override
    public void onBarcodeDetected(String barcode) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_BARCODE, barcode);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
