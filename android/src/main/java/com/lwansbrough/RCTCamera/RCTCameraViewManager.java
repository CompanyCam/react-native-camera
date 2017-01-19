package com.lwansbrough.RCTCamera;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.*;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.List;
import java.util.ArrayList;

public class RCTCameraViewManager extends ViewGroupManager<RCTCameraView> {
    private static final String REACT_CLASS = "RCTCamera";
    private RCTCameraView currentView;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RCTCameraView createViewInstance(ThemedReactContext context) {

        // If there's a current CCCameraView already instantiated, remove its lifecycleListener from the context
        if (currentView != null && currentView.lifecycleListener != null) {
            context.removeLifecycleEventListener(currentView.lifecycleListener);
        }

        currentView = new RCTCameraView(context);

        // After instantiating a new view, add its liefcycleListener to the context
        if (currentView != null && currentView.lifecycleListener != null) {
            context.addLifecycleEventListener(currentView.lifecycleListener);
        }

        return currentView;
    }

    @ReactProp(name = "aspect")
    public void setAspect(RCTCameraView view, int aspect) {
        view.setAspect(aspect);
    }

    @ReactProp(name = "captureMode")
    public void setCaptureMode(RCTCameraView view, int captureMode) {
        // TODO - implement video mode
    }

    @ReactProp(name = "captureTarget")
    public void setCaptureTarget(RCTCameraView view, int captureTarget) {
        // No reason to handle this props value here since it's passed again to the RCTCameraModule capture method
    }

    @ReactProp(name = "type")
    public void setType(RCTCameraView view, int type) {
        view.setCameraType(type);
    }

    @ReactProp(name = "captureQuality")
    public void setCaptureQuality(RCTCameraView view, String captureQuality) {
        view.setCaptureQuality(captureQuality);
    }

    @ReactProp(name = "torchMode")
    public void setTorchMode(RCTCameraView view, int torchMode) {
        view.setTorchMode(torchMode);
    }

    @ReactProp(name = "flashMode")
    public void setFlashMode(RCTCameraView view, int flashMode) {
        view.setFlashMode(flashMode);
    }

    @ReactProp(name = "orientation")
    public void setOrientation(RCTCameraView view, int orientation) {
        view.setOrientation(orientation);
    }

    @ReactProp(name = "captureAudio")
    public void setCaptureAudio(RCTCameraView view, boolean captureAudio) {
        // TODO - implement video mode
    }

    @ReactProp(name = "barcodeScannerEnabled")
    public void setBarcodeScannerEnabled(RCTCameraView view, boolean barcodeScannerEnabled) {
        view.setBarcodeScannerEnabled(barcodeScannerEnabled);
    }

    @ReactProp(name = "barCodeTypes")
    public void setBarCodeTypes(RCTCameraView view, ReadableArray barCodeTypes) {
        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
        view.setBarCodeTypes(result);
    }
}
