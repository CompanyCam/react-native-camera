/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View;

import com.facebook.react.bridge.LifecycleEventListener;

import java.util.List;

public class RCTCameraView extends ViewGroup {
    private final OrientationEventListener _orientationListener;
    private final Context _context;
    private RCTCameraViewFinder _viewFinder = null;
    private int _actualDeviceOrientation = -1;
    private int _aspect = RCTCameraModule.RCT_CAMERA_ASPECT_FIT;
    private String _captureQuality = "high";
    private int _torchMode = -1;
    private int _flashMode = -1;

    private boolean shouldReacquireCamera = false;

    // Each instance will create a lifecycleListener to properly release and reacquire the camera when the app transitions from background to foreground
    public LifecycleEventListener lifecycleListener;

    public RCTCameraView(Context context) {
        super(context);
        this._context = context;
        RCTCamera.createInstance(getDeviceOrientation(context));

        _orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (setActualDeviceOrientation(_context)) {
                    layoutViewFinder();
                }
            }
        };

        if (_orientationListener.canDetectOrientation()) {
            _orientationListener.enable();
        } else {
            _orientationListener.disable();
        }

        lifecycleListener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                System.out.println("onHostResume called in RCTCameraView");

                // Check if the view should try to reacquire the camera
                if (shouldReacquireCamera) {

                    // Acquire the rear camera
                    RCTCamera.getInstance().acquireCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_BACK);

                    // Start the preview
                    _viewFinder.startPreview();

                    // Enable the orientation listener
                    if (_orientationListener != null) {
                        _orientationListener.enable();
                    }

                    // Reset the shouldReacquireCamera flag
                    shouldReacquireCamera = false;
                }
            }

            @Override
            public void onHostPause() {
                System.out.println("onHostPause called in RCTCameraView");

                // Check if this view currently has a reference to the back camera
                if (RCTCamera.getInstance().hasCameraInstanceOfType(RCTCameraModule.RCT_CAMERA_TYPE_BACK)) {

                    // Stop the preview
                    _viewFinder.stopPreview();

                    // Release the back camera
                    RCTCamera.getInstance().releaseCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_BACK);

                    // Disable the orientation listener
                    if (_orientationListener != null) {
                        _orientationListener.disable();
                    }

                    // Set the shouldReacquireCamera flag so that the view attempts to reacquire the camera after the activity resumes
                    shouldReacquireCamera = true;
                }
            }

            @Override
            public void onHostDestroy() {
                System.out.println("onHostDestroy called in RCTCameraView");

                // Check if this view currently has a reference to the back camera
                if (RCTCamera.getInstance().hasCameraInstanceOfType(RCTCameraModule.RCT_CAMERA_TYPE_BACK)) {

                    // Stop the preview
                    _viewFinder.stopPreview();

                    // Release the back camera
                    RCTCamera.getInstance().releaseCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_BACK);

                    // Disable the orientation listener
                    if (_orientationListener != null) {
                        _orientationListener.disable();
                    }

                    // Set the shouldReacquireCamera flag so that the view attempts to reacquire the camera after the activity resumes
                    shouldReacquireCamera = true;
                }
            }
        };
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutViewFinder(left, top, right, bottom);
    }

    @Override
    public void onViewAdded(View child) {
        if (this._viewFinder == child) return;
        // remove and readd view to make sure it is in the back.
        // @TODO figure out why there was a z order issue in the first place and fix accordingly.
        this.removeView(this._viewFinder);
        this.addView(this._viewFinder, 0);
    }

    public void setAspect(int aspect) {
        this._aspect = aspect;
        layoutViewFinder();
    }

    public void setCameraType(final int type) {
        if (null != this._viewFinder) {
            this._viewFinder.setCameraType(type);
            RCTCamera.getInstance().adjustPreviewLayout(type);
        } else {
            _viewFinder = new RCTCameraViewFinder(_context, type);
            if (-1 != this._flashMode) {
                _viewFinder.setFlashMode(this._flashMode);
            }
            if (-1 != this._torchMode) {
                _viewFinder.setFlashMode(this._torchMode);
            }
            addView(_viewFinder);
        }
    }

    public void setCaptureQuality(String captureQuality) {
        this._captureQuality = captureQuality;
        if (this._viewFinder != null) {
            this._viewFinder.setCaptureQuality(captureQuality);
        }
    }

    public void setTorchMode(int torchMode) {
        this._torchMode = torchMode;
        if (this._viewFinder != null) {
            this._viewFinder.setTorchMode(torchMode);
        }
    }

    public void setFlashMode(int flashMode) {
        this._flashMode = flashMode;
        if (this._viewFinder != null) {
            this._viewFinder.setFlashMode(flashMode);
        }
    }

    public void setOrientation(int orientation) {
        RCTCamera.getInstance().setOrientation(orientation);
        if (this._viewFinder != null) {
            layoutViewFinder();
        }
    }

    public void setBarcodeScannerEnabled(boolean barcodeScannerEnabled) {
        RCTCamera.getInstance().setBarcodeScannerEnabled(barcodeScannerEnabled);
    }

    public void setBarCodeTypes(List<String> types) {
        RCTCamera.getInstance().setBarCodeTypes(types);
    }

    private boolean setActualDeviceOrientation(Context context) {
        int actualDeviceOrientation = getDeviceOrientation(context);
        if (_actualDeviceOrientation != actualDeviceOrientation) {
            _actualDeviceOrientation = actualDeviceOrientation;
            RCTCamera.getInstance().setActualDeviceOrientation(_actualDeviceOrientation);
            return true;
        } else {
            return false;
        }
    }

    private int getDeviceOrientation(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
    }

    private void layoutViewFinder() {
        layoutViewFinder(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
    }

    private void layoutViewFinder(int left, int top, int right, int bottom) {
        if (null == _viewFinder) {
            return;
        }
        float width = right - left;
        float height = bottom - top;
        int viewfinderWidth;
        int viewfinderHeight;
        double ratio;
        switch (this._aspect) {
            case RCTCameraModule.RCT_CAMERA_ASPECT_FIT:
                ratio = this._viewFinder.getRatio();
                if (ratio * height > width) {
                    viewfinderHeight = (int) (width / ratio);
                    viewfinderWidth = (int) width;
                } else {
                    viewfinderWidth = (int) (ratio * height);
                    viewfinderHeight = (int) height;
                }
                break;
            case RCTCameraModule.RCT_CAMERA_ASPECT_FILL:
                ratio = this._viewFinder.getRatio();
                if (ratio * height < width) {
                    viewfinderHeight = (int) (width / ratio);
                    viewfinderWidth = (int) width;
                } else {
                    viewfinderWidth = (int) (ratio * height);
                    viewfinderHeight = (int) height;
                }
                break;
            default:
                viewfinderWidth = (int) width;
                viewfinderHeight = (int) height;
        }

        int viewFinderPaddingX = (int) ((width - viewfinderWidth) / 2);
        int viewFinderPaddingY = (int) ((height - viewfinderHeight) / 2);

        this._viewFinder.layout(viewFinderPaddingX, viewFinderPaddingY, viewFinderPaddingX + viewfinderWidth, viewFinderPaddingY + viewfinderHeight);
        this.postInvalidate(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
    }
}
