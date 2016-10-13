package com.example.taylor.gipheed.Activities;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.taylor.gipheed.R;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.Utils;

/**
 * Created by Taylor on 9/27/2016.
 */

public class GifDetailActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private static final String TAG = "GifDetailActivity";

    private Utils.Sizer sizer;
    private LinearLayout llMain;
    private SurfaceView surfaceView;
    private TextureView textureView;
    private SeekBar seekBar;

    private MovieDecoder movieDecoder;

    private String gifUrl;

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

        textureView = new TextureView(this);
        textureView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textureView.setSurfaceTextureListener(this);

//        surfaceView = new SurfaceView(this);
//        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(final SurfaceHolder holder) {
//                ThreadManager.Run(new Runnable() {
//                    @Override
//                    public void run() {
//                        MovieDecoder decoder = new MovieDecoder();
//                        decoder.decode(gifUrl, holder.getSurface());
//                    }
//                });
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//
//            }
//        });
        llMain.addView(textureView);

        seekBar = new SeekBar(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizer.viewSize(100), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = sizer.viewSize(5);
        seekBar.setLayoutParams(layoutParams);
        llMain.addView(seekBar);

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        ThreadManager.Run(new Runnable() {
            @Override
            public void run() {
                movieDecoder = new MovieDecoder(DECODE_CALLBACK);
                movieDecoder.prepForSeeking(gifUrl, new Surface(surface));
                seekBar.setOnSeekBarChangeListener(SEEK_LISTENER);
//                movieDecoder.decode(gifUrl, new Surface(surface));
            }
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if(movieDecoder != null) {
//            movieDecoder.setStopPlaybackFlag(true);
//            movieDecoder.releaseResources();
            movieDecoder.stopSeeking();
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
                    textureView.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(120f), (int) ((float)sizer.viewSize(120f)*(float)height / (float) width)));
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
}
