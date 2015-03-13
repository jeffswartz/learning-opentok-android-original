package com.tokbox.android.demo.learningopentok;

public class NoiseVideoCapturer extends com.opentok.android.BaseVideoCapturer {
    private boolean mCapturerHasStarted;
    private boolean mCapturerIsPaused;
    private CaptureSettings mCapturerSettings;
    private int mWidth;
    private int mHeight;

    public NoiseVideoCapturer(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void init() {
        mCapturerHasStarted = false;
        mCapturerIsPaused = false;

        mCapturerSettings = new CaptureSettings();
        mCapturerSettings.height = mHeight;
        mCapturerSettings.width = mWidth;
    }

    @Override
    public int startCapture() {
        mCapturerHasStarted = true;
        return 0;
    }

    @Override
    public int stopCapture() {
        mCapturerHasStarted = false;
        return 0;
    }

    @Override
    public void destroy() {

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
        mCapturerIsPaused = true;
    }

    @Override
    public void onResume() {
        mCapturerIsPaused = false;
    }
}
