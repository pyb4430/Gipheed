package com.example.taylor.gipheed;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Taylor on 9/13/2016.
 */
public class Utils {

    public static final float NORMALIZED_SCREEN_WIDTH = 120f;

    public static Sizer getSizer(Context context) {
        return new Sizer(context);
    }

    public static class Sizer {

        int screenWidth;
        int screenHeight;

        public Sizer(Context context) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        }

        public int viewSize(float normalizedSize) {
            return (int)((normalizedSize/NORMALIZED_SCREEN_WIDTH)*(float) screenWidth);
        }
    }
}
