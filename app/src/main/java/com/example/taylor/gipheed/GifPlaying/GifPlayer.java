package com.example.taylor.gipheed.GifPlaying;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.taylor.gipheed.Utils;

/**
 * Created by Taylor on 9/14/2016.
 */
public class GifPlayer extends TextureView implements TextureView.SurfaceTextureListener{

    private static final String TAG = "GifPlayer";

    private boolean surfaceTextureReady = false;
    private PlayerReadyListener playerReadyListener;
    private MediaPlayer mp;
    private Utils.Sizer sizer;

    private String url;

    public GifPlayer(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public void setPlayerReadyListener(PlayerReadyListener playerReadyListener) {
        this.playerReadyListener = playerReadyListener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTextureReady = true;
        if(playerReadyListener != null ) {
            playerReadyListener.onPlayerReady();
        }
        init(url);
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

    // TODO: Finish this:
    // an attempt to eliminate the brief black rectangle when a gif starts playin (the GifPlayer is getting
    // shown before the MediaPlayer is initialized)
    public boolean init(final String url, final PlayerReadyListener playerReadyListener) {
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
                playerReadyListener.onPlayerReady();
                play();
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            }
            return true;
        } else {
//            this.initFinishedListener = playerReadyListener;
            setSurfaceTextureListener(this);
            return false;
        }
    }

    public void play() {
            Log.v(TAG, "gif mp4 start play");
            try {
                mp.prepareAsync();
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                        mp.start();
                    }
                });
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            }
    }

    public void stop() {
        setVisibility(INVISIBLE);
        if(mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
    }

    public interface PlayerReadyListener {
        void onPlayerReady();
    }

//    public interface InitFinishedListener {
//        public void onInitFinished();
//    }
}
