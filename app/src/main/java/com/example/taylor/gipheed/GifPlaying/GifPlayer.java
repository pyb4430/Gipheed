package com.example.taylor.gipheed.GifPlaying;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.taylor.gipheed.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Taylor on 9/14/2016.
 */
public class GifPlayer extends TextureView implements TextureView.SurfaceTextureListener{

    private static final String TAG = "GifPlayer";

    private MediaPlayer mp;
    private Utils.Sizer sizer;

    private String url;

    private AtomicBoolean isPlaying = new AtomicBoolean(false);

    private PlayerPreparedListener playerPreparedListener;

    public GifPlayer(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public boolean isPlaying() {
        return isPlaying.get();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTextureReady = true;
        if(url != null) {
            init(url);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.v(TAG, "GifPlayer SurfaceTextureDestroyed");
        surfaceTextureReady = false;
        if(mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public boolean init(final String url) {
        this.url = url;
        Log.v(TAG, "init start, isAvailable: " + isAvailable());
        if(isAvailable() && url != null) {
            Log.v(TAG, "gif mp4 url: " + url);
            SurfaceTexture surfaceTexture = getSurfaceTexture();
            Surface surface = new Surface(surfaceTexture);

            mp = new MediaPlayer();
            mp.setSurface(surface);
            try {
                mp.setDataSource(getContext(), Uri.parse(url));
                play();
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            }
            return true;
        } else {
            setSurfaceTextureListener(this);
            return false;
        }
    }

    public boolean init(final String url, final PlayerPreparedListener playerPreparedListener) {
        this.url = url;
        this.playerPreparedListener = playerPreparedListener;
        Log.v(TAG, "init start, isAvailable: " + isAvailable() + " " + url);
        if(isAvailable() && url != null) {
            Log.v(TAG, "gif mp4 url: " + url);
            SurfaceTexture surfaceTexture = getSurfaceTexture();
            Surface surface = new Surface(surfaceTexture);

            if(mp != null && mp.isPlaying()) {
                stop();
            }
            mp = new MediaPlayer();
            mp.setSurface(surface);
            try {
                mp.setDataSource(getContext(), Uri.parse(url));
                play();
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            }
            return true;
        } else {
            setSurfaceTextureListener(this);
            return false;
        }
    }

    public void play() {
        isPlaying.set(true);
        Log.v(TAG, "gif mp4 start play");
        try {
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.start();
                    if(playerPreparedListener != null) {
                        playerPreparedListener.onPlayerPrepared();
                    }
                }
            });
        } catch (Exception e) {
            isPlaying.set(false);
            Log.v(TAG, e.getMessage());
        }
    }

    public void stop() {
        url = null;
        isPlaying.set(false);
        if(mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }

    }

    public interface PlayerPreparedListener {
        void onPlayerPrepared();
    }
}
