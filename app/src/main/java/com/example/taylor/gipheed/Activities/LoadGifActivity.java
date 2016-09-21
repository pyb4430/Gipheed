package com.example.taylor.gipheed.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.taylor.gipheed.GifReading.GifReaderV3;
import com.example.taylor.gipheed.GifReading.GifView;
import com.example.taylor.gipheed.R;

/**
 * Created by Taylor on 8/20/2016.
 */
public class LoadGifActivity extends Activity {

    private GifView imageView;
    private Bitmap bm;
    private int bmIndex = 0;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout llMain = new LinearLayout(this);
        llMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(llMain);

        imageView = new GifView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        llMain.addView(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.animateGif();
            }
        });

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                imageView.setImageBitmap((Bitmap) msg.obj);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        final GifReaderV3 gifReader = new GifReaderV3(this);

        imageView.setGifResource(R.raw.chicken);
    }

    private final GifReaderV3.FrameChangeListener FRAME_CHANGE_LISTENER = new GifReaderV3.FrameChangeListener() {
        @Override
        public void onFrameChange(final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    };
}
