package com.example.taylor.gipheed.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Taylor on 9/17/2016.
 */
public class FavContentProvider extends ContentProvider {

    private final static String TAG = "FavContentProvider";

    private FavDataDbHelper favDataDbHelper;
    private SQLiteDatabase db;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI("com.openglpractice.app.provider", "fav_gifs", 1);
    }

    @Override
    public boolean onCreate() {

        favDataDbHelper = new FavDataDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = favDataDbHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
            case 1:
                Cursor c = db.query(
                        FavDataContract.FavGifData.TABLE_NAME,    // The table to query
                        projection,                               // The columns to return
                        selection,                                // The columns for the WHERE clause
                        selectionArgs,                            // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        FavDataContract.FavGifData._ID + " DESC"  // The sort order
                );
                return c;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, uri.toString());
        switch (sUriMatcher.match(uri)) {
            case 1:
                db = favDataDbHelper.getWritableDatabase();

                long newRowId = db.insert(FavDataContract.FavGifData.TABLE_NAME, null, values);

                return ContentUris.withAppendedId(uri, newRowId);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                return db.delete(FavDataContract.FavGifData.TABLE_NAME, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                int count = db.update(
                        FavDataContract.FavGifData.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                return count;
        }
        return 0;
    }
}
