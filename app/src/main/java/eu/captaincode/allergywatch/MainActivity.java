package eu.captaincode.allergywatch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
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

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST_CODE = 101;

    private CameraManager cameraManager;
    private int neededCameraFacing = CameraMetadata.LENS_FACING_BACK;
    private TextureView textureView;
    private String cameraId;

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
                    //textureView.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, );
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
            if (null != activity) {
                activity.finish();
            }
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

    private void setupCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                Log.d(LOG_TAG, "CameraId: " + cameraId);
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                Integer cameraFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraFacing != null && cameraFacing == neededCameraFacing) {
                    Log.d(LOG_TAG, "Camera with id=" + cameraId + " is facing back");
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert streamConfigurationMap != null;
                    outputSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

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

    private void createPreviewSession() {
        Log.i(LOG_TAG, "Create preview session");
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(outputSize.getWidth(), outputSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            Log.i(LOG_TAG, "CaptureSession.onConfigured");
                            if (cameraDevice == null) {
                                return;
                            }

                            try {
                                captureRequest = captureRequestBuilder.build();
                                cameraCaptureSession = session;
                                cameraCaptureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
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
}
