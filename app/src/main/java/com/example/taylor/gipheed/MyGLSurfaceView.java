package com.example.taylor.gipheed;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Taylor on 6/8/2016.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyRenderer renderer;
    private Uri imageUri;

    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        renderer = new MyRenderer(context);

        setRenderer(renderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public void setImage(final Uri uri) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.setImage(uri, getContext().getContentResolver());
            }
        });
        requestRender();

    }

    public void setMask(final Bitmap maskBitmap) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.setMask(maskBitmap);
                requestRender();
            }
        });

    }

    public void startSpline(final MotionEvent event) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
//                renderer.startSpline(event);
                requestRender();
            }
        });
    }

    public void addSplineWaypoint(final MotionEvent event) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
//                renderer.addSplineWaypoint(event);
                requestRender();
            }
        });
    }
}
