package com.example.taylor.gipheed.Fragments;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.taylor.gipheed.Activities.MainActivityV2;
import com.example.taylor.gipheed.ContentProvider.FavDataContract;
import com.example.taylor.gipheed.GifPlaying.GifPlayManager;
import com.example.taylor.gipheed.Giphy.GiphyController;
import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.GifFeedRecyclerAdapter;
import com.example.taylor.gipheed.Utils;

/**
 * Created by Taylor on 9/13/2016.
 */
public class SearchFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "SearchFragment";

    private LinearLayout llMain;

    private RecyclerView recyclerView;
    private GifFeedRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private GifPlayManager gifPlayManager;

    private GiphyTrendRespModel giphyTrendRespModel;
    private MainActivityV2.GifFeedChangeListener feedChangeListener;

    private Utils.Sizer sizer;

    private boolean isViewModeStream = false;

    private String currentQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sizer = Utils.getSizer(getContext());
    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        llMain = new LinearLayout(getContext());
        llMain.setOrientation(LinearLayout.VERTICAL);

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

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(feedChangeListener != null) {
                    feedChangeListener.onFeedClick();
                }
                return false;
            }
        });

        return llMain;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentQuery == null || currentQuery.length() < 1) {
            loadData("star wars");
        } else {
            loadData(currentQuery);
        }
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

    public void loadData(final String query) {
        currentQuery = query;
        ThreadManager.Run(new Runnable() {
            @Override
            public void run() {
                giphyTrendRespModel = GiphyController.searchGiphy(query, 25);
                if(giphyTrendRespModel.data.length > 0) {
                    Log.v(TAG, "search results found");
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
                    Log.v(TAG, "no search results found");
                    ThreadManager.RunUI(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "No Results Found", Toast.LENGTH_SHORT).show();
                        }
                    });
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

    private final GifFeedRecyclerAdapter.ImageSelectedListener IMAGE_SELECTED_LISTENER = new GifFeedRecyclerAdapter.ImageSelectedListener() {
        @Override
        public void onImageSelected(GiphyTrendRespModel.Data imageData) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavDataContract.FavGifData.COLUMN_NAME_GIPHY_ID, imageData.id);
            Uri newFav = getContext().getContentResolver().insert(Uri.parse("content://"+ FavDataContract.FAV_GIF_URI), contentValues);
            if(newFav != null) {
                Log.v(TAG, "new Fav: " + newFav.toString());
            }
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
            feedChangeListener.onFeedClick();
        }
    };

}
