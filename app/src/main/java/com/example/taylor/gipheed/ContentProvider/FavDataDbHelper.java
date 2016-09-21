package com.example.taylor.gipheed.ContentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Taylor on 9/17/2016.
 */
public class FavDataDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GifData.db";

    public static final String SQL_CREATE_MAIN = "CREATE TABLE " +
            FavDataContract.FavGifData.TABLE_NAME + " (" +
            FavDataContract.FavGifData._ID + " INTEGER PRIMARY KEY," +
            FavDataContract.FavGifData.COLUMN_NAME_GIPHY_ID + TEXT_TYPE + " )";

    public FavDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MAIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
