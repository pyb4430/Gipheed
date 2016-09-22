package com.example.taylor.gipheed.Fragments;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.taylor.gipheed.Activities.MainActivityV2;
import com.example.taylor.gipheed.ContentProvider.FavDataContract;
import com.example.taylor.gipheed.GifPlaying.GifPlayManager;
import com.example.taylor.gipheed.Giphy.GiphyController;
import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.GifFeedRecyclerAdapter;

/**
 * Created by Taylor on 9/13/2016.
 */
public class TrendingFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "TrendingFragment";

    private LinearLayout llMain;

    private RecyclerView recyclerView;
    private GifFeedRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private GifPlayManager gifPlayManager;

    private GiphyTrendRespModel giphyTrendRespModel;
    private MainActivityV2.GifFeedChangeListener feedChangeListener;

    private boolean isViewModeStream = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setIsViewModeStream(boolean isViewModeStream) {
        this.isViewModeStream = isViewModeStream;
        if(recyclerAdapter != null) {
            int position = layoutManager.findFirstVisibleItemPosition();
            recyclerView.setAdapter(null);
            recyclerAdapter.setisViewModeStream(isViewModeStream);
            gifPlayManager.playNextAdded();
            recyclerView.setAdapter(recyclerAdapter);
            layoutManager.scrollToPosition(position);
        }
    }

    public void setFeedChangeListener(MainActivityV2.GifFeedChangeListener feedChangeListener) {
        this.feedChangeListener = feedChangeListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        llMain = new LinearLayout(getContext());

        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        llMain.addView(recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new GifFeedRecyclerAdapter(getContext(), isViewModeStream);
        gifPlayManager = new GifPlayManager();
        recyclerAdapter.setGifPlayManager(gifPlayManager);
        recyclerAdapter.setImageSelectedListener(IMAGE_SELECTED_LISTENER);
        recyclerView.setAdapter(recyclerAdapter);

        recyclerView.addOnScrollListener(SCROLL_LISTENER);

        return llMain;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ThreadManager.Run(new Runnable() {
            @Override
            public void run() {
                giphyTrendRespModel = GiphyController.getTrending(25);
                gifPlayManager.playNextAdded();
                gifPlayManager.setGiphyTrendRespModel(giphyTrendRespModel);

                ThreadManager.RunUI(new Runnable() {
                    @Override
                    public void run() {
                        recyclerAdapter.setData(giphyTrendRespModel);
                        recyclerView.scrollToPosition(0);
                    }
                });
            }
        });
    }

    private final RecyclerView.OnScrollListener SCROLL_LISTENER = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                int position = layoutManager.findFirstCompletelyVisibleItemPosition();

                if (position < 0) {
                    position = layoutManager.findFirstVisibleItemPosition();
                } else if (layoutManager.findLastCompletelyVisibleItemPosition() == giphyTrendRespModel.data.length-1) {
                    position = giphyTrendRespModel.data.length-1;
                }

                Log.v(TAG, "onScrolled, position of playing gif: " + position);
                gifPlayManager.playGif(position);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);


        }
    };

    private final GifFeedRecyclerAdapter.ImageSelectedListener IMAGE_SELECTED_LISTENER = new GifFeedRecyclerAdapter.ImageSelectedListener() {
        @Override
        public void onImageSelected(GiphyTrendRespModel.Data imageData) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavDataContract.FavGifData.COLUMN_NAME_GIPHY_ID, imageData.id);
            Uri newFav = getContext().getContentResolver().insert(Uri.parse("content://"+ FavDataContract.FAV_GIF_URI), contentValues);
            Log.v(TAG, "new Fav: " + newFav.toString());
            if(feedChangeListener != null) {
                feedChangeListener.onFavoritesUpdated();
            }
        }

        @Override
        public void onGifDetailClick(int position) {

        }

        @Override
        public void onGifPlayClick(int position) {
            gifPlayManager.playGif(position);
        }
    };

}
