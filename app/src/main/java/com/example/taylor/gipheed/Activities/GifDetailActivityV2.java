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

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Icon;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.taylor.gipheed.GifPlaying.MovieDecoder;
import com.example.taylor.gipheed.OpenGL.GifEditingThread;
import com.example.taylor.gipheed.OpenGL.GifFrameRect;
import com.example.taylor.gipheed.R;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.Triangle;
import com.example.taylor.gipheed.Utils;

import java.util.ArrayList;

/**
 * Created by Taylor on 9/27/2016.
 */

public class GifDetailActivityV2 extends AbstractActivity implements SurfaceHolder.Callback, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "GifDetailActivity";

    private Utils.Sizer sizer;
    private LinearLayout llMain;
    private LinearLayout llHeader;
    private ImageView iconAddSprite;
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



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sizer = Utils.getSizer(this);

        llMain = new LinearLayout(this);
        llMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        llMain.setGravity(Gravity.TOP);
        llMain.setOrientation(LinearLayout.VERTICAL);
        setContentView(llMain);

        llHeader = new LinearLayout(this);
        llHeader.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sizer.viewSize(18)));
        llHeader.setOrientation(LinearLayout.HORIZONTAL);
        llMain.addView(llHeader);

        iconAddSprite = new ImageView(this);
        iconAddSprite.setImageResource(R.drawable.ic_search_black_24dp);
        iconAddSprite.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(12), sizer.viewSize(12)));
        llHeader.addView(iconAddSprite);

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

        gifEditingThread = new GifEditingThread(GIF_EDITING_LISTENER);
        gifEditingThread.start();
        gifEditingThread.initHandler();
        gifEditingThread.init();

        iconAddSprite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GifDetailActivityV2.this, ImageMaskActivity.class);
                startActivity(intent);
            }
        });
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

        @Override
        public void onReadyToSeek() {
            movieDecoder.goToFrame2(1);
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

    private final GifEditingThread.GifEditingListener GIF_EDITING_LISTENER = new GifEditingThread.GifEditingListener() {
        @Override
        public void onSurfaceTextureReady(SurfaceTexture surfaceTexture, GifEditingThread gifEditingThread) {
            movieDecoder.prepForSeeking(gifUrl, new Surface(surfaceTexture), gifEditingThread);
            seekBar.setOnSeekBarChangeListener(SEEK_LISTENER);
            surfaceTexture.setOnFrameAvailableListener(GifDetailActivityV2.this);
        }
    };

}
