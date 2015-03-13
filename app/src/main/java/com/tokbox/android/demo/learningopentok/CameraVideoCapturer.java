package com.tokbox.android.demo.learningopentok;

import android.content.Context;

import com.opentok.android.BaseVideoCapturer;

public class CameraVideoCapturer extends BaseVideoCapturer
        implements AndroidCameraHelper.HelperListener {
    
    private boolean mCapturerHasStarted;
    private boolean mCapturerIsPaused;
    private CaptureSettings mCapturerSettings;
    private int mWidth;
    private int mHeight;
    private int mDesiredFps;
    private AndroidCameraHelper mHelper;

    public CameraVideoCapturer(Context context, int width, int height, int fps) {
        mWidth = width;
        mHeight = height;
        mDesiredFps = fps;

        mHelper = new AndroidCameraHelper(context, mWidth, mHeight, mDesiredFps);
        mHelper.setHelperListener(this);
    }
    @Override
    public void init() {
        mCapturerHasStarted = false;
        mCapturerIsPaused = false;

        mCapturerSettings = new CaptureSettings();
        mCapturerSettings.height = mHeight;
        mCapturerSettings.width = mWidth;
        mCapturerSettings.expectedDelay = 0;
        mCapturerSettings.format = BaseVideoCapturer.NV21;

        mHelper.init();
    }

    @Override
    public int startCapture() {
        mCapturerHasStarted = true;
        mHelper.startCapture();
        return 0;
    }

    @Override
    public int stopCapture() {
        mCapturerHasStarted = false;
        mHelper.stopCapture();
        return 0;
    }

    @Override
    public void destroy() {
        mHelper.destroy();
    }

    @Override
    public boolean isCaptureStarted() {
        return mCapturerHasStarted;
    }

    @Override
    public CaptureSettings getCaptureSettings() {
        return mCapturerSettings;
    }

    @Override
    public void onPause() {
        mCapturerIsPaused = false;
    }

    @Override
    public void onResume() {
        mCapturerIsPaused = true;
    }

    @Override
    public void cameraFrameReady(byte[] data, int width, int height, int rotation, boolean mirror) {
        provideByteArrayFrame(data, NV21, width,
                height, rotation, mirror);
    }
}
