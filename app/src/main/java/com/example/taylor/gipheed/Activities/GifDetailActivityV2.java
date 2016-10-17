/*
 * Copyright 2016 Daniel Taylor Harrison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This work contains code from and code modeled after Google Inc.'s Grafika project, specifically the
 * classes Texture2dProgram.java, EglCore.java, ContinuousCaptureActivity.java, EglSurfaceBase.java,
 * Drawable2d.java, and FullFrameRect.java. This work also contains code from and code modeled after
 * The Android Open Source Project's Compatibility Test Suite project (specifically the jb-mr2-release branch)
 * Any code from the Grafika project or the Compatibility Test Suite project that is included in this work
 * may have been modified from its original form by the author of this work.
 * The Grafika project contains the following copyright notice:
 * Copyright 2013 Google Inc. All rights reserved.
 * The Compatibility Test Suite project contains the the following copyright notice:
 * Copyright (C) 2013 The Android Open Source Project
 *
 */

package com.example.taylor.gipheed.Activities;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.taylor.gipheed.GifPlaying.MovieDecoder;
import com.example.taylor.gipheed.OpenGL.GifFrameRect;
import com.example.taylor.gipheed.R;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.Triangle;
import com.example.taylor.gipheed.Utils;

/**
 * Created by Taylor on 9/27/2016.
 */

public class GifDetailActivityV2 extends AppCompatActivity implements SurfaceHolder.Callback, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "GifDetailActivity";

    private Utils.Sizer sizer;
    private LinearLayout llMain;
    private SurfaceView surfaceView;
    private SeekBar seekBar;

    private MovieDecoder movieDecoder;

    private String gifUrl;

    private GifEditingThread gifEditingThread;
    private GifEditingThread.GifEditingHandler gifEditingHandler;
//    private SurfaceTexture surfaceTexture;
//    private EGLDisplay mEGLDisplay;
//    private EGLContext mEglContext;
//    private EGLConfig mEGLConfig;
//    private GifFrameRect mGifFrameRect;
//    private int mTextureId;

    /**
     * Constructor flag: surface must be recordable.  This discourages EGL from using a
     * pixel format that cannot be converted efficiently to something usable by the video
     * encoder.
     */
    public static final int FLAG_RECORDABLE = 0x01;

    // Android-specific extension.
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sizer = Utils.getSizer(this);

        llMain = new LinearLayout(this);
        llMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        llMain.setGravity(Gravity.CENTER);
        llMain.setOrientation(LinearLayout.VERTICAL);
        setContentView(llMain);

        gifUrl = getIntent().getStringExtra("gifUrl");
        movieDecoder = new MovieDecoder(DECODE_CALLBACK);
        movieDecoder.getVideoSize(gifUrl);

        surfaceView = new SurfaceView(this);
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(120f), (int) ((float)sizer.viewSize(120f)*(movieDecoder.getHeight() / movieDecoder.getWidth()))));
        surfaceView.getHolder().addCallback(this);
        llMain.addView(surfaceView);

        seekBar = new SeekBar(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizer.viewSize(100), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = sizer.viewSize(5);
        seekBar.setLayoutParams(layoutParams);
        llMain.addView(seekBar);

        gifEditingThread = new GifEditingThread();
        gifEditingThread.start();
        gifEditingThread.initHandler();
        gifEditingThread.init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gifEditingThread.surfaceInit(holder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, "surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        float[] matrix = new float[16];
        surfaceTexture.getTransformMatrix(matrix);
//        for (float i : matrix) {
//            Log.v(TAG, "frameAvailable matrix: " + i);
//        }
        gifEditingThread.notifyFrameAvailable(matrix);
    }

    @Override
    protected void onStop() {
        if(movieDecoder != null) {
//            movieDecoder.setStopPlaybackFlag(true);
//            movieDecoder.releaseResources();
            movieDecoder.stopSeeking();
        }
        if(gifEditingThread != null) {
            gifEditingThread.release();
        }
        super.onStop();
    }

    private final MovieDecoder.DecodeCallback DECODE_CALLBACK = new MovieDecoder.DecodeCallback() {
        @Override
        public void metaDataRetrieved(final int width, final int height, final int numberOfFrames, final long lastTimeStamp) {
            Log.v(TAG, "video width, height, and frameCount: " + width + " " + height + " " + numberOfFrames);
            ThreadManager.RunUI(new Runnable() {
                @Override
                public void run() {
//                    surfaceView.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(120f), (int) ((float)sizer.viewSize(120f)*(float)height / (float) width)));
                    seekBar.setMax(numberOfFrames);
                }
            });
        }
    };

    private final SeekBar.OnSeekBarChangeListener SEEK_LISTENER = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            Log.v(TAG, "seekbar seeked to: " + progress + "/" + seekBar.getMax());
            movieDecoder.goToFrame2(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            movieDecoder.onlyRenderTarget();
        }
    };


    public class GifEditingThread extends HandlerThread {

        public static final String TAG = "GifEditingThread";

        public static final int MSG_INIT = 0;
        public static final int MSG_SURFACE_TEXTURE_READY = 1;
        public static final int MSG_UPDATE_FRAME = 2;
        public static final int MSG_FRAME_AVAILABLE = 3;

        private GifEditingHandler handler;

        private SurfaceTexture surfaceTexture;
        private EGLDisplay mEGLDisplay;
        private EGLContext mEglContext;
        private EGLSurface mEglSurface;
        private EGLConfig mEGLConfig;
        private GifFrameRect mGifFrameRect;
        private Triangle mTriangle;
        private int mTextureId;

        public GifEditingThread() {
            super("gif_editing_thread");
        }

        @Override
        public void run() {
            super.run();
        }

        public SurfaceTexture getSurfaceTexture() {
            return surfaceTexture;
        }

        public void initHandler() {
//            while(getLooper() == null) {
//
//            }
//            try {
                handler = new GifEditingHandler(getLooper());
//            } catch (Exception e) {
//                Log.d(TAG, "wuh" + e.getMessage());
//            }
        }

        public void init() {
            handler.sendEmptyMessage(MSG_INIT);
        }

        public void surfaceInit(SurfaceHolder holder) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_SURFACE_TEXTURE_READY;
            msg.obj = holder;
            handler.sendMessage(msg);
        }

        public void notifyFrameAvailable(float[] transformMatrix) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_FRAME_AVAILABLE;
            msg.obj = transformMatrix;
            handler.sendMessage(msg);
        }

        public void updateFrame() {
            handler.sendEmptyMessage(MSG_UPDATE_FRAME);
        }

        private void setupEgl() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            EGLConfig config = getConfig(FLAG_RECORDABLE, 2);

            int[] attrib2_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };

            Log.d(TAG, "egl display null? " + (mEGLDisplay ==null));
            EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, EGL14.EGL_NO_CONTEXT, attrib2_list, 0);
            mEglContext = context;
            mEGLConfig = config;

        }

        /**
         * Finds a suitable EGLConfig.
         *
         * @param flags Bit flags from constructor.
         * @param version Must be 2 or 3.
         */
        private EGLConfig getConfig(int flags, int version) {
            int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
            if (version >= 3) {
                renderableType |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
            }

            // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
            // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
            // when reading into a GL_RGBA buffer.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    //EGL14.EGL_DEPTH_SIZE, 16,
                    //EGL14.EGL_STENCIL_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, renderableType,
                    EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                    EGL14.EGL_NONE
            };
            if ((flags & FLAG_RECORDABLE) != 0) {
                attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
                attribList[attribList.length - 2] = 1;
            }
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0)) {
                Log.w(TAG, "unable to find RGB8888 / " + version + " EGLConfig");
                return null;
            }
            return configs[0];
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  This must be
         * called from the thread where the context was created.
         * <p>
         * On completion, no context will be current.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                if(mEglSurface != null) {
                    releaseSurface(mEglSurface);
                }
                // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
                // every eglInitialize() we need an eglTerminate().
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroyContext(mEGLDisplay, mEglContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEglContext = EGL14.EGL_NO_CONTEXT;
            mEGLConfig = null;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                    // We're limited here -- finalizers don't run on the thread that holds
                    // the EGL state, so if a surface or context is still current on another
                    // thread we can't fully release it here.  Exceptions thrown from here
                    // are quietly discarded.  Complain in the log file.
                    Log.w(TAG, "WARNING: EglCore was not explicitly released -- state may be leaked");
                    release();
                }
            } finally {
                super.finalize();
            }
        }

        /**
         * Destroys the specified surface.  Note the EGLSurface won't actually be destroyed if it's
         * still current in a context.
         */
        public void releaseSurface(EGLSurface eglSurface) {
            EGL14.eglDestroySurface(mEGLDisplay, eglSurface);
        }

        public EGLSurface createWindowSurface(Surface surface) {
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            return EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribs, 0);
        }

        public class GifEditingHandler extends Handler {

            public GifEditingHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MSG_INIT:
                        setupEgl();
                        break;
                    case MSG_SURFACE_TEXTURE_READY:

                        SurfaceHolder holder = (SurfaceHolder) msg.obj;

                        mEglSurface = createWindowSurface(holder.getSurface());

                        EGL14.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEglContext);

                        mGifFrameRect = new GifFrameRect();
                        mTriangle = new Triangle();

                        int[] textures = new int[1];
                        GLES20.glGenTextures(1, textures, 0);

                        mTextureId = textures[0];
                        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

                        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                                GLES20.GL_NEAREST);
                        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                                GLES20.GL_LINEAR);
                        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                                GLES20.GL_CLAMP_TO_EDGE);
                        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                                GLES20.GL_CLAMP_TO_EDGE);

                        surfaceTexture = new SurfaceTexture(mTextureId);
                        movieDecoder.prepForSeeking(gifUrl, new Surface(surfaceTexture), GifEditingThread.this);
                        seekBar.setOnSeekBarChangeListener(SEEK_LISTENER);
                        surfaceTexture.setOnFrameAvailableListener(GifDetailActivityV2.this);
                        break;
                    case MSG_UPDATE_FRAME:
                        surfaceTexture.updateTexImage();
                        break;
                    case MSG_FRAME_AVAILABLE:
                        mGifFrameRect.draw(mTextureId, (float[]) msg.obj);
//                        mTriangle.draw();
                        EGL14.eglSwapBuffers(mEGLDisplay, mEglSurface);
                        break;
                }
            }
        }
    }
}
