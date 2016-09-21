package com.example.taylor.gipheed.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.taylor.gipheed.GifPlaying.GifPlayer;
import com.example.taylor.gipheed.Utils;

/**
 * Created by Taylor on 9/13/2016.
 */
public class GifPlayFragment extends android.support.v4.app.Fragment {

    private LinearLayout llMain;

    private GifPlayer gifPlayer;
    private Utils.Sizer sizer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sizer = Utils.getSizer(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        llMain = new LinearLayout(getContext());

        gifPlayer = new GifPlayer(getContext());
        gifPlayer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        llMain.addView(gifPlayer);

        return llMain;
    }

    @Override
    public void onResume() {
        super.onResume();
        gifPlayer.init("http://media2.giphy.com/media/FiGiRei2ICzzG/giphy.mp4");
//        gifPlayer.play();
    }

    private final GifPlayer.PlayerReadyListener PLAYER_READY_LISTENER = new GifPlayer.PlayerReadyListener() {
        @Override
        public void onPlayerReady() {
            gifPlayer.init("http://media2.giphy.com/media/FiGiRei2ICzzG/giphy.mp4");
            gifPlayer.play();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        gifPlayer.stop();
    }
}
