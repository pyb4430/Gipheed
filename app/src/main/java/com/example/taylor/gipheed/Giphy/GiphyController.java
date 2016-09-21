package com.example.taylor.gipheed.Giphy;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Taylor on 9/12/2016.
 */
public class GiphyController {
    private static final String TAG = "GiphyController";

    private static final String GIPHY_API_KEY = "dc6zaTOxFJmzC";
    private static final String GIPHY_ENDPT = "http://api.giphy.com/v1/gifs";

    public static GiphyTrendRespModel getTrending(int limit) {
        Uri uri = Uri.parse(GIPHY_ENDPT).buildUpon().appendPath("trending").appendQueryParameter("limit", Integer.toString(limit)).appendQueryParameter("api_key", GIPHY_API_KEY).build();
        return getGiphyList(uri.toString());
    }

    public static GiphyTrendRespModel searchGiphy(String query, int limit) {
        Uri uri = Uri.parse(GIPHY_ENDPT).buildUpon().appendPath("search").appendQueryParameter("q", query).appendQueryParameter("limit", Integer.toString(limit)).appendQueryParameter("api_key", GIPHY_API_KEY).build();
        return getGiphyList(uri.toString());
    }

    public static GiphyTrendRespModel getListByIds(ArrayList<String> ids) {
        StringBuilder sb = new StringBuilder(ids.get(0));
        for(int i = 1; i < ids.size(); i++) {
            sb.append(",");
            sb.append(ids.get(i));
        }
        Uri uri = Uri.parse(GIPHY_ENDPT).buildUpon().appendQueryParameter("ids", sb.toString()).appendQueryParameter("api_key", GIPHY_API_KEY).build();
        return getGiphyList(uri.toString());
    }

    private static GiphyTrendRespModel getGiphyList(String urlString) {
        GiphyTrendRespModel response = null;
        try {
            URL url = new URL(urlString);

            URLConnection connection = url.openConnection();

            connection.connect();

            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            Gson gson = new Gson();
            response = gson.fromJson(br, GiphyTrendRespModel.class);

        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return response;
    }

}
