package com.example.taylor.gipheed.GifPlaying;

import android.util.Log;
import android.view.View;

import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.example.taylor.gipheed.ThreadManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Taylor on 9/15/2016.
 */
public class GifPlayManager {

    private final static String TAG = "GifPlayManager";

    private GiphyTrendRespModel giphyTrendRespModel;

    private LinkedHashMap<Integer, GifPlayer> gifPlayers = new LinkedHashMap<>();

    private int positionPlaying = -1;
    // used to play the first gif in a new set that has been loaded
    private boolean playNextAdded = false;

    public GifPlayManager() {
    }

    public void playNextAdded() {
        playNextAdded = true;
    }

    public void setGiphyTrendRespModel(GiphyTrendRespModel giphyTrendRespModel) {
        this.giphyTrendRespModel = giphyTrendRespModel;
    }

    public void addGifPlayer(int listLocation, GifPlayer gifPlayer) {
        gifPlayers.put(listLocation, gifPlayer);
        if(playNextAdded) {
            if(positionPlaying == listLocation) {
                positionPlaying = -1;
            }
            playGif(listLocation);
            playNextAdded = false;
        }
    }

    public void removeGifPlayer(int listLocation) {
        gifPlayers.remove(listLocation);
    }

    public void playGif(final int listLocation) {
        if(giphyTrendRespModel != null && positionPlaying != listLocation) {
            try {
                Log.v(TAG, "stopping gifPlayers: " + gifPlayers.size());
                for(Map.Entry gifPlayer : gifPlayers.entrySet()) {
                    if((Integer)gifPlayer.getKey() != listLocation) {
                        ((GifPlayer) gifPlayer.getValue()).stop();
                    }
                }
                if(positionPlaying > -1) {
                    final GifPlayer lastPlayer = gifPlayers.get(positionPlaying);
                    if (lastPlayer != null) {
                        lastPlayer.stop();
                    }
                }
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            }

            final GifPlayer player = gifPlayers.get(listLocation);
            if (player != null) {
                final String url = giphyTrendRespModel.data[listLocation].images.original.mp4;
                Log.v(TAG, "starting gif url: " + url);
                player.setVisibility(View.VISIBLE);
                ThreadManager.Run(new Runnable() {
                    @Override
                    public void run() {
                        player.init(url);
                    }
                });
                positionPlaying = listLocation;
            } else {
                gifPlayers.remove(listLocation);
                Log.v(TAG, "failed to start gif because player is null");
            }

        }
    }
}
