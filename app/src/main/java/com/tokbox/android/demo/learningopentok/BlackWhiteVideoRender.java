package com.tokbox.android.demo.learningopentok;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.opentok.android.BaseVideoRenderer;

import java.nio.ByteBuffer;

public class BlackWhiteVideoRender extends BaseVideoRenderer {

    GLSurfaceView mRenderView;
    GLRendererHelper mRenderer;

    public BlackWhiteVideoRender(Context context) {
        mRenderView = new GLSurfaceView(context);
        mRenderView.setEGLContextClientVersion(2);

        mRenderer = new GLRendererHelper();
        mRenderView.setRenderer(mRenderer);

        mRenderView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onFrame(Frame frame) {
        // Modify U and V planes to produce a black and white image
        ByteBuffer imageBuffer = frame.getBuffer();
        int startU = frame.getWidth() * frame.getHeight();
        for (int i = startU; i < imageBuffer.capacity(); i++) {
            imageBuffer.put(i, (byte)128);
        }

        mRenderer.displayFrame(frame);
        mRenderView.requestRender();
    }

    @Override
    public void setStyle(String key, String value) {
        if (BaseVideoRenderer.STYLE_VIDEO_SCALE.equals(key)) {
            if (BaseVideoRenderer.STYLE_VIDEO_FIT.equals(value)) {
                mRenderer.enableVideoFit(true);
            } else if (BaseVideoRenderer.STYLE_VIDEO_FILL.equals(value)) {
                mRenderer.enableVideoFit(false);
            }
        }
    }

    @Override
    public void onVideoPropertiesChanged(boolean videoEnabled) {
        mRenderer.disableVideo(!videoEnabled);
    }

    @Override
    public View getView() {
        return mRenderView;
    }

    @Override
    public void onPause() {
        mRenderView.onPause();
    }

    @Override
    public void onResume() {
        mRenderView.onResume();
    }
}
