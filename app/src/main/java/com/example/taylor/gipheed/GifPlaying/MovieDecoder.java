package com.example.taylor.gipheed.GifPlaying;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Taylor on 9/14/2016.
 */
public class MovieDecoder {

    private static final String TAG = "MovieDecoder";

    public void decode(String videoUrl) {
        MediaExtractor mediaExtractor = new MediaExtractor();
        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(0);
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String format = mediaCodecList.findDecoderForFormat(mediaFormat);

        try {
            MediaCodec mediaCodec = MediaCodec.createByCodecName(format);

            mediaCodec.createInputSurface();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }


    }
}
