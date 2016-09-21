package com.example.taylor.gipheed.Giphy;

/**
 * Created by Taylor on 9/12/2016.
 */
public class GiphyTrendRespModel {

    public Data[] data;

    public static class Data {
        public String id;
        public Images images;
    }

    public static class Images {
        public StillImageObject fixed_height_still;
        public StillImageObject fixed_width_small_still;
        public VideoObject original;
        public VideoObject fixed_height;
    }

    public static class StillImageObject {
        public String url;
        public int width;
        public int height;
    }

    public static class VideoObject {
        public String mp4;
        public int width;
        public int height;
        public long mp4_size;
    }


}
