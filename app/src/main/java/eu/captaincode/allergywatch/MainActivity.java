package eu.captaincode.allergywatch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST_CODE = 101;

    private CameraManager cameraManager;
    private int neededCameraFacing = CameraMetadata.LENS_FACING_BACK;
    private TextureView textureView;
    private String cameraId;
    private ImageReader mImageReader;
    private Size mImageReaderOutputSize;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private CameraDevice cameraDevice;
    private Size outputSize;

    TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    Log.i(LOG_TAG, "SurfaceTexture opened with size: " + width + "x" + height);
                    setupCamera();
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    Log.i(LOG_TAG, "SurfaceTexture size changed: " + width + "x" + height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    Log.i(LOG_TAG, "SurfaceTexture destroyed");

                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    Log.i(LOG_TAG, "SurfaceTexture updated");
                }
            };

    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest captureRequest;

    public static final long LOCK_FOCUS_DELAY_ON_FOCUSED = 5000;

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(LOG_TAG, "Image available with format: " + reader.getImageFormat());
            Image image = reader.acquireLatestImage();
            detectBarcode(image);
            if (image != null) {
                image.close();
            }
        }
    };
    private int mCameraRotation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        textureView = findViewById(R.id.texture_view);


    }

    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();
        if (textureView.isAvailable()) {
            Log.i(LOG_TAG, "TextureView is available");
            setupCamera();
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    public static final long LOCK_FOCUS_DELAY_ON_UNFOCUSED = 1000;

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.e(LOG_TAG, "Camera permission granted");
                cameraManager.openCamera(this.cameraId, stateCallback, backgroundHandler);
            } else {
                Log.e(LOG_TAG, "Camera permission is not granted");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Integer mLastAfState;

    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            Log.i(LOG_TAG, "closeCamera: closing camera");
            cameraDevice.close();
            MainActivity.this.cameraDevice = null;
            Log.i(LOG_TAG, "closeCamera: Camera closed:");
        }
    }

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    private Integer mLastAutoFocusState;
    private Handler mUiHandler = new Handler();
    private Runnable mLockAutoFocusRunnable = new Runnable() {
        @Override
        public void run() {
            lockAutoFocus();
        }
    };
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {


        private void process(CaptureResult result) {
            // We have nothing to do when the camera preview is working normally.
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (afState != null && !afState.equals(mLastAfState)) {
                switch (afState) {
                    case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_INACTIVE");
                        lockAutoFocus();
                        break;
                    case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN");
                        break;
                    case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED");
                        mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                        mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_FOCUSED);
                        break;
                    case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                        mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                        mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_UNFOCUSED);
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED");
                        break;
                    case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                        mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                        //mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_UNFOCUSED);
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED");
                        break;
                    case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN");
                        break;
                    case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                        mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                        //mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_FOCUSED);
                        Log.d(LOG_TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED");
                        break;
                }
            }
            mLastAfState = afState;


        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(LOG_TAG, "Camera opened successfully, id=" + camera.getId());
            MainActivity.this.cameraDevice = camera;
            createPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            MainActivity.this.cameraDevice = null;
            Log.i(LOG_TAG, "Camera disconnected: id=" + camera.getId());
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.i(LOG_TAG, "onError: closing camera");
            camera.close();
            MainActivity.this.cameraDevice = null;
            Activity activity = MainActivity.this;
            finish();
            Log.i(LOG_TAG, "onError: Camera closed:");
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            Log.i(LOG_TAG, "onError: closing camera");
            camera.close();
            MainActivity.this.cameraDevice = null;
            Log.i(LOG_TAG, "onError: Camera closed:");
        }
    };

    private void setupCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                Log.d(LOG_TAG, "CameraId: " + cameraId);
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                Integer cameraFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraFacing != null && cameraFacing == neededCameraFacing) {
                    Log.d(LOG_TAG, "Camera with id=" + cameraId + " is facing back");
                    mCameraRotation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    Log.d(LOG_TAG, "Camera rotation=" + mCameraRotation);

                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert streamConfigurationMap != null;
                    outputSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

                    mImageReaderOutputSize = streamConfigurationMap.getOutputSizes(ImageReader.class)[0];
                    mImageReader = ImageReader.newInstance(
                            mImageReaderOutputSize.getWidth(),
                            mImageReaderOutputSize.getHeight(),
                            ImageFormat.YUV_420_888,
                            2);
                    mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler);

                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createPreviewSession() {
        Log.i(LOG_TAG, "Creating preview session");
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(outputSize.getWidth(), outputSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            Surface mImageSurface = mImageReader.getSurface();

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(mImageSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            Log.i(LOG_TAG, "CaptureSession.onConfigured");
                            if (cameraDevice == null) {
                                Log.e(LOG_TAG, "Cameradevice was null");
                                return;
                            }

                            try {
                                if (isAutoFocusSupported())
                                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                            CaptureRequest.CONTROL_AF_MODE_AUTO);
                                else
                                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                captureRequest = captureRequestBuilder.build();
                                cameraCaptureSession = session;
                                cameraCaptureSession.setRepeatingRequest(captureRequest, mCaptureCallback, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }


                    },
                    backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void detectBarcode(Image image) {
        if (image == null) {
            return;
        }
        Log.i(LOG_TAG, "Detecting barcode");
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_EAN_13
                ).build();

        FirebaseVisionImage firebaseImageBarcode = FirebaseVisionImage.fromMediaImage(image, 0);

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseImageBarcode)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        Log.d(LOG_TAG, "Imagedetection.onSuccess()");
                        for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {
                            String rawValue = barcode.getRawValue();
                            Log.d(LOG_TAG, "Detected barcode: " + rawValue);
                            Toast.makeText(MainActivity.this, rawValue, Toast.LENGTH_LONG).show();
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

    private void lockAutoFocus() {
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            CaptureRequest captureRequest = captureRequestBuilder.build();
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
            if (cameraCaptureSession != null)
                cameraCaptureSession.capture(captureRequest, mCaptureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private float getMinimumFocusDistance() {
        if (cameraId == null) {
            return 0;
        }
        Float minimumLens = null;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics c = manager.getCameraCharacteristics(cameraId);
            minimumLens = c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        } catch (CameraAccessException e) {
            Log.d(LOG_TAG, "Cannot access camera");
            e.printStackTrace();
        }
        if (minimumLens != null) {
            return minimumLens;
        }
        return 0;
    }

    private boolean isAutoFocusSupported() {
        return isHardwareLevelSupported(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) ||
                getMinimumFocusDistance() > 0;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isHardwareLevelSupported(int requiredLevel) {
        boolean res = false;
        if (cameraId == null)
            return res;
        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

            int deviceLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (deviceLevel) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(LOG_TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_3");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(LOG_TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(LOG_TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(LOG_TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED");
                    break;
                default:
                    Log.d(LOG_TAG, "Unknown INFO_SUPPORTED_HARDWARE_LEVEL: " + deviceLevel);
                    break;
            }


            if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                res = requiredLevel == deviceLevel;
            } else {
                // deviceLevel is not LEGACY, can use numerical sort
                res = requiredLevel <= deviceLevel;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "isHardwareLevelSupported Error", e);
        }
        return res;
    }

}
