package com.tokbox.android.demo.learningopentok;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class AndroidCameraHelper implements Camera.PreviewCallback {
    public interface HelperListener {
        void cameraFrameReady(byte[] data, int width, int height, int rotation, boolean mirror);
    }

    private final static int PIXEL_FORMAT = ImageFormat.NV21;
    private final static int CAPTURE_BUFFERS_COUNT = 3;

    private final Display mCurrentDisplay;
    private final int mDesiredFps;
    private final int mPreferredCaptureWidth;
    private final int mPreferredCaptureHeight;

    private SurfaceTexture mSurfaceTexture;

    private int mCameraIndex;
    private int mCaptureActualWidth;
    private int mCaptureActualHeight;
    private int mCaptureActualFPS;
    private int mExpectedFrameSize;

    private Camera mCamera;
    private Camera.CameraInfo mCurrentDeviceInfo;

    private ReentrantLock mPreviewBufferLock = new ReentrantLock(); // sync

    private HelperListener mListener;

    public AndroidCameraHelper(Context context, int preferredWidth, int preferredHeight, int desiredFps) {
        mCameraIndex = getFrontCameraIndex();
        mPreferredCaptureWidth = preferredWidth;
        mPreferredCaptureHeight = preferredHeight;
        mDesiredFps = desiredFps;

        WindowManager windowManager = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);
        mCurrentDisplay = windowManager.getDefaultDisplay();
    }

    public void setHelperListener(HelperListener listener) {
        mListener = listener;
    }

    public void init() {
        mCamera = Camera.open(mCameraIndex);
        mCurrentDeviceInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraIndex, mCurrentDeviceInfo);
    }

    public void startCapture() {
        configureCaptureSize(mPreferredCaptureWidth, mPreferredCaptureHeight);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mCaptureActualWidth, mCaptureActualHeight);
        parameters.setPreviewFormat(PIXEL_FORMAT);
        parameters.setPreviewFrameRate(mCaptureActualFPS);

        mCamera.setParameters(parameters);

        // Create capture buffers
        PixelFormat pixelFormat = new PixelFormat();
        PixelFormat.getPixelFormatInfo(PIXEL_FORMAT, pixelFormat);
        int bufSize = mCaptureActualWidth * mCaptureActualHeight * pixelFormat.bitsPerPixel
                / 8;
        byte[] buffer = null;
        for (int i = 0; i < CAPTURE_BUFFERS_COUNT; i++) {
            buffer = new byte[bufSize];
            mCamera.addCallbackBuffer(buffer);
        }

        try {
            mSurfaceTexture = new SurfaceTexture(42);
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (Exception e) { e.printStackTrace(); }

        // Start preview
        mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.startPreview();

        mPreviewBufferLock.lock();
        mExpectedFrameSize = bufSize;

        mPreviewBufferLock.unlock();
    }

    public void stopCapture() {
        mPreviewBufferLock.lock();

        mCamera.stopPreview();
        mCamera.setPreviewCallbackWithBuffer(null);

        mPreviewBufferLock.unlock();
    }

    public void destroy() {
        mCamera.release();
    }

    private void configureCaptureSize(int preferredWidth, int preferredHeight) {
        Camera.Parameters parameters = mCamera.getParameters();

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        List<Integer> frameRates = parameters.getSupportedPreviewFrameRates();
        int maxFPS = 0;
        if (frameRates != null) {
            for (Integer frameRate : frameRates) {
                if (frameRate > maxFPS) {
                    maxFPS = frameRate;
                }
            }
        }
        mCaptureActualFPS = maxFPS;

        int maxw = 0;
        int maxh = 0;
        for (int i = 0; i < sizes.size(); ++i) {
            Camera.Size s = sizes.get(i);
            if (s.width >= maxw && s.height >= maxh) {
                if (s.width <= preferredWidth && s.height <= preferredHeight) {
                    maxw = s.width;
                    maxh = s.height;
                }
            }
        }
        if (maxw == 0 || maxh == 0) {
            Camera.Size s = sizes.get(0);
            maxw = s.width;
            maxh = s.height;
        }

        mCaptureActualWidth = maxw;
        mCaptureActualHeight = maxh;
    }

    private static int getFrontCameraIndex() {
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return 0;
    }

    private int compensateCameraRotation(int uiRotation) {
        int cameraRotation = 0;
        switch (uiRotation) {
            case (Surface.ROTATION_0):
                cameraRotation = 0;
                break;
            case (Surface.ROTATION_90):
                cameraRotation = 270;
                break;
            case (Surface.ROTATION_180):
                cameraRotation = 180;
                break;
            case (Surface.ROTATION_270):
                cameraRotation = 90;
                break;
            default:
                break;
        }

        int cameraOrientation = getNaturalCameraOrientation();

        int totalCameraRotation = 0;
        boolean usingFrontCamera = isFrontCamera();
        if (usingFrontCamera) {
            // The front camera rotates in the opposite direction of the
            // device.
            int inverseCameraRotation = (360 - cameraRotation) % 360;
            totalCameraRotation = (inverseCameraRotation + cameraOrientation) % 360;
        } else {
            totalCameraRotation = (cameraRotation + cameraOrientation) % 360;
        }

        return totalCameraRotation;
    }

    public boolean isFrontCamera() {
        return (mCurrentDeviceInfo != null && mCurrentDeviceInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    private int getNaturalCameraOrientation() {
        if (mCurrentDeviceInfo != null) {
            return mCurrentDeviceInfo.orientation;
        } else {
            return 0;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mPreviewBufferLock.lock();
        if (data.length == mExpectedFrameSize) {
            // Get the rotation of the camera
            int currentRotation = compensateCameraRotation(mCurrentDisplay
                    .getRotation());

            if (mListener != null) {
                mListener.cameraFrameReady(data, mCaptureActualWidth, mCaptureActualHeight,
                        currentRotation, isFrontCamera());
            }

            // Reuse the video buffer
            camera.addCallbackBuffer(data);
        }
        mPreviewBufferLock.unlock();
    }
}
