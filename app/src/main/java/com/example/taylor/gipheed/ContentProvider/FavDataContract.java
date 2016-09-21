package com.example.taylor.gipheed.ContentProvider;

import android.provider.BaseColumns;

/**
 * Created by Taylor on 9/17/2016.
 */
public class FavDataContract {

    public static final String FAV_GIF_URI = "com.openglpractice.app.provider/fav_gifs";

    private FavDataContract(){}

    public static class FavGifData implements BaseColumns {
        public static final String TABLE_NAME = "fav_gifs";
        public static final String COLUMN_NAME_GIPHY_ID = "giphy_id";
    }
}
