package com.tokbox.android.demo.learningopentok;

import android.content.Context;

import com.opentok.android.BaseAudioDevice;

public class BasicAudioDevice extends BaseAudioDevice {
    private final static int SAMPLING_RATE = 44100;
    private final static int NUM_CHANNELS_CAPTURING = 1;
    private final static int NUM_CHANNELS_RENDERING = 1;

    private AudioSettings mCaptureSettings;
    private AudioSettings mRendererSettings;

    private boolean mCapturerStarted;
    private boolean mRenderStarted;

    public BasicAudioDevice(Context context) {
        mCaptureSettings = new AudioSettings(SAMPLING_RATE, NUM_CHANNELS_CAPTURING);
        mRendererSettings = new AudioSettings(SAMPLING_RATE, NUM_CHANNELS_RENDERING);

        mCapturerStarted = false;
        mRenderStarted = false;
    }

    @Override
    public boolean initCapturer() {
        return true;
    }

    @Override
    public boolean startCapturer() {
        mCapturerStarted = true;
        return true;
    }

    @Override
    public boolean stopCapturer() {
        mCapturerStarted = false;
        return true;
    }

    @Override
    public boolean destroyCapturer() {
        return true;
    }

    @Override
    public boolean initRenderer() {
        return true;
    }

    @Override
    public boolean startRenderer() {
        mRenderStarted = true;
        return true;
    }

    @Override
    public boolean stopRenderer() {
        mRenderStarted = false;
        return true;
    }

    @Override
    public boolean destroyRenderer() {
        return true;
    }

    @Override
    public int getEstimatedCaptureDelay() {
        return 0;
    }

    @Override
    public int getEstimatedRenderDelay() {
        return 0;
    }

    @Override
    public AudioSettings getCaptureSettings() {
        return mCaptureSettings;
    }

    @Override
    public AudioSettings getRenderSettings() {
        return mRendererSettings;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}
