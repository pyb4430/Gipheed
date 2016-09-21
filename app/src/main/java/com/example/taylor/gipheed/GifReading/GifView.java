package com.example.taylor.gipheed.GifReading;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

/**
 * Created by Taylor on 9/10/2016.
 */
public class GifView extends ImageView {

    private GifReaderV3 gifReader;
    private Bitmap bm;
    private Handler animationHandler = new Handler();

    public GifView(Context context) {
        super(context);
        gifReader = new GifReaderV3(this.getContext());
    }

    public void setGifResource(int resId) {
        gifReader.load(resId);
    }

    public void animateGif() {
        Thread animThread = new Thread(ANIMATE_GIF);
        animThread.start();
    }

    private final Runnable ANIMATE_GIF = new Runnable() {
        @Override
        public void run() {
            for(int i = 0; i < gifReader.getFrameCount(); i++) {
                bm = gifReader.getFrame(i);
                animationHandler.post(SET_NEW_FRAME);
                try {
                    Thread.sleep(gifReader.getDelay(i));
                } catch (InterruptedException e ) {

                }
            }
        }
    };

    private final Runnable SET_NEW_FRAME = new Runnable() {
        @Override
        public void run() {
            setImageBitmap(bm);
        }
    };
}
