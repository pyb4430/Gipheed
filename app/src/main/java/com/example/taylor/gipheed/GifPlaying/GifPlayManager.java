package com.example.taylor.gipheed.GifPlaying;

import android.util.Log;
import android.view.View;

import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.GifFeedRecyclerAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Taylor on 9/15/2016.
 */
public class GifPlayManager {

    private final static String TAG = "GifPlayManager";

    private GiphyTrendRespModel giphyTrendRespModel;

    private Map<Integer, GifFeedRecyclerAdapter.GifFeedViewHolder> gifPlayerHolders = new LinkedHashMap<>();

    private int positionPlaying = -1;

    // this is used to play the first gif in a new set that has been loaded
    private boolean playNextAdded = false;

    public GifPlayManager() {
    }

    public void playNextAdded() {
        playNextAdded = true;
    }

    public void setGiphyTrendRespModel(GiphyTrendRespModel giphyTrendRespModel) {
        this.giphyTrendRespModel = giphyTrendRespModel;
    }

    public void addGifPlayer(int listLocation, GifFeedRecyclerAdapter.GifFeedViewHolder gifPlayerHolder) {
        gifPlayerHolders.put(listLocation, gifPlayerHolder);
        if(playNextAdded) {
            if(positionPlaying == listLocation) {
                positionPlaying = -1;
            }
            playGif(listLocation);
            playNextAdded = false;
        }
    }

    public void removeGifPlayer(int listLocation) {
        gifPlayerHolders.remove(listLocation);
    }

    public void playGif(final int listLocation) {
        if(giphyTrendRespModel != null) {
            if(positionPlaying == listLocation && gifPlayerHolders.get(positionPlaying).gifPlayer.isPlaying()) {
                return;
            }
            try {
                Log.v(TAG, "stopping gifPlayerHolders: " + gifPlayerHolders.size());

                for(Map.Entry<Integer, GifFeedRecyclerAdapter.GifFeedViewHolder> gifPlayerEntry : gifPlayerHolders.entrySet()) {
                    if(gifPlayerEntry.getKey() != listLocation) {
                        Log.v(TAG, "stopping gifPlayer: " + gifPlayerEntry.getKey());
                        gifPlayerEntry.getValue().gifPlayer.stop();
                        gifPlayerEntry.getValue().imageView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            }

            final GifFeedRecyclerAdapter.GifFeedViewHolder holder = gifPlayerHolders.get(listLocation);
            if (holder != null && holder.gifPlayer != null) {
                final String url = giphyTrendRespModel.data[listLocation].images.original.mp4;
                Log.v(TAG, "starting gif url: " + url);

                // Used a 250 ms delay here to smooth the apparent delay between the MediaPlayer.OnPreparedListener
                // callback and the MediaPlayer actually playing the video on some phones. May cause
                // a skipped gif frame or two on faster phones, but it will only occur the first time
                // (better than the few frames of white background that occur without the delay)
                final GifPlayer.PlayerPreparedListener playerPreparedListener = new GifPlayer.PlayerPreparedListener() {
                    @Override
                    public void onPlayerPrepared() {
                        ThreadManager.RunUIWait(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(TAG, "gifPlayer set to visible: " + listLocation);
                                holder.imageView.setVisibility(View.INVISIBLE);
                            }
                        }, 250);
                    }
                };
                ThreadManager.Run(new Runnable() {
                    @Override
                    public void run() {
                        holder.gifPlayer.init(url, playerPreparedListener);
                    }
                });
                positionPlaying = listLocation;
            } else {
                gifPlayerHolders.remove(listLocation);
                Log.v(TAG, "failed to start gif because player is null");
            }
        }
    }
}
