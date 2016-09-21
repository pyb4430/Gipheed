package com.example.taylor.gipheed.Fragments;

import android.database.Cursor;
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

import com.example.taylor.gipheed.ContentProvider.FavDataContract;
import com.example.taylor.gipheed.GifPlaying.GifPlayManager;
import com.example.taylor.gipheed.Giphy.GiphyController;
import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.TrendingRecyclerAdapter;

import java.util.ArrayList;

/**
 * Created by Taylor on 9/13/2016.
 */
public class FavoritesFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "TrendingFragment";

    private LinearLayout llMain;

    private RecyclerView recyclerView;
    private TrendingRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private GifPlayManager gifPlayManager;

    private GiphyTrendRespModel giphyTrendRespModel;

    private boolean isViewModeStream = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        recyclerAdapter = new TrendingRecyclerAdapter(getContext(), isViewModeStream, "Remove");
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

    public void loadData() {
        ThreadManager.Run(new Runnable() {
            public Thread thread;
            @Override
            public void run() {
                Thread.currentThread();
                try {
                    ArrayList<String> gifIds = new ArrayList<String>();
                    String[] projection = {
                            FavDataContract.FavGifData._ID,
                            FavDataContract.FavGifData.COLUMN_NAME_GIPHY_ID
                    };
                    Cursor cursor = getContext().getContentResolver().query(Uri.parse("content://" + FavDataContract.FAV_GIF_URI), projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            gifIds.add(cursor.getString(1));
                            while (cursor.moveToNext()) {
                                gifIds.add(cursor.getString(1));
                            }
                        }
                        cursor.close();
                    }

                    if (gifIds.size() > 0) {
                        giphyTrendRespModel = GiphyController.getListByIds(gifIds);
                        gifPlayManager.playNextAdded();
                        gifPlayManager.setGiphyTrendRespModel(giphyTrendRespModel);

                        ThreadManager.RunUI(new Runnable() {
                            @Override
                            public void run() {
                                recyclerAdapter.setData(giphyTrendRespModel);
                                recyclerView.scrollToPosition(0);
                            }
                        });
                    } else {
                        //TODO: display something that says no favorites
                    }
                } catch (Exception e) {
                    Log.v(TAG, e.getMessage());
                }
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

    private final TrendingRecyclerAdapter.ImageSelectedListener IMAGE_SELECTED_LISTENER = new TrendingRecyclerAdapter.ImageSelectedListener() {
        @Override
        public void onImageSelected(GiphyTrendRespModel.Data imageData) {
            String selection = FavDataContract.FavGifData.COLUMN_NAME_GIPHY_ID + " = ?";
            String[] selectionArgs = {imageData.id};
            int newFav = getContext().getContentResolver().delete(Uri.parse("content://"+ FavDataContract.FAV_GIF_URI), selection, selectionArgs);
            Log.v(TAG, "Favorite deleted: " + newFav);
            loadData();
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
