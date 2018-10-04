package eu.captaincode.allergywatch.barcode;

import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Log;

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

public class FirebaseBarcodeDetector {
    private static final String TAG = FirebaseBarcodeDetector.class.getSimpleName();

    private BarcodeDetectionListener detectionListener;
    private FirebaseVisionBarcodeDetector barcodeDetector;

    private OnSuccessListener<List<FirebaseVisionBarcode>> onSuccessListener =
            new OnSuccessListener<List<FirebaseVisionBarcode>>() {
        @Override
        public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
            Log.d(TAG, "Imagedetection.onSuccess()");
            for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {
                String rawValue = barcode.getRawValue();
                Log.d(TAG, "Detected barcode: " + rawValue);
                detectionListener.onBarcodeDetected(rawValue);
            }
        }
    };

    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(TAG, "Imagedetection.onFailure()");
            e.printStackTrace();

        }
    };

    private OnCompleteListener<List<FirebaseVisionBarcode>> onCompleteListener =
            new OnCompleteListener<List<FirebaseVisionBarcode>>() {
        @Override
        public void onComplete(@NonNull Task<List<FirebaseVisionBarcode>> task) {
            Log.d(TAG, "Imagedetection.onComplete()");

        }
    };

    public FirebaseBarcodeDetector(BarcodeDetectionListener detectionListener) {
        this.detectionListener = detectionListener;
        setupDetector();
    }

    private void setupDetector() {
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_EAN_13
                ).build();
        barcodeDetector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);
    }

    public void detectBarcode(Image image) {
        if (image == null) {
            return;
        }
        Log.i(TAG, "Detecting barcode");

        FirebaseVisionImage firebaseImageBarcode = FirebaseVisionImage.fromMediaImage(image, 0);
        barcodeDetector.detectInImage(firebaseImageBarcode)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener)
                .addOnCompleteListener(onCompleteListener);
    }

    public interface BarcodeDetectionListener {
        void onBarcodeDetected(String detectedBarcode);
    }
}
