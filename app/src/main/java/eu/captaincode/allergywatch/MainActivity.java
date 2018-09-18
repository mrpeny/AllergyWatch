package eu.captaincode.allergywatch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.images.internal.ImageUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    TextView barcodeResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barcodeResultTextView = findViewById(R.id.tv_barcode_result);
        Bitmap barcode1 = BitmapFactory.decodeResource(getResources(), R.drawable.barcode1);
        detectBarcode(barcode1);
    }

    private void detectBarcode(Bitmap barcode) {
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_EAN_13
                ).build();

        FirebaseVisionImage firebaseImageBarcode = FirebaseVisionImage.fromBitmap(barcode);

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseImageBarcode)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        Log.d(LOG_TAG, "Imagedetection.onSuccess()");
                        for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {
                            Log.d(LOG_TAG, "Detected barcode: " + barcode.getRawValue());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LOG_TAG, "Imagedetection.onFailure()");
                        e.printStackTrace();

                    }
                }).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<FirebaseVisionBarcode>> task) {
                        Log.d(LOG_TAG, "Imagedetection.onComplete()");

                    }
                });

    }
}
