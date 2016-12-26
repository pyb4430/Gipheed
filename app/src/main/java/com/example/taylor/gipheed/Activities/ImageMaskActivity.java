package com.example.taylor.gipheed.Activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.taylor.gipheed.DrawSurfView;
import com.example.taylor.gipheed.DrawView;
import com.example.taylor.gipheed.MyGLSurfaceView;
import com.example.taylor.gipheed.Utils;

import java.io.InputStream;

/**
 * Created by Taylor on 8/20/2016.
 */
public class ImageMaskActivity extends AbstractActivity {

    public final static String TAG = "ImageMaskActivity";

    private Utils.Sizer sizer;

    private FrameLayout flMain;
    private Button button;
    private Button setImageButton;
    private MyGLSurfaceView surfaceView;
    private ImageView imageView;
    private DrawSurfView drawSurfView;
    private DrawView drawView;

    private boolean needRender = false;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sizer = Utils.getSizer(this);

        flMain = new FrameLayout(this);
        flMain.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        flMain.setBackgroundColor(Color.GRAY);
        button = new Button(this);
        button.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        button.setText("Set Mask");
        setImageButton = new Button(this);
        setImageButton.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setImageButton.setText("Load Image");
        surfaceView = new MyGLSurfaceView(this);
        flMain.addView(surfaceView);
        flMain.addView(button);
        flMain.addView(setImageButton);
        imageView = new ImageView(ImageMaskActivity.this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(400, 600, Gravity.RIGHT));
        flMain.addView(imageView);


//        drawSurfView = new DrawSurfView(this);
//        drawSurfView.setAlpha(0.5f);
//        drawSurfView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        flMain.addView(drawSurfView);

        drawView = new DrawView(this);
        drawView.setAlpha(0.5f);
        drawView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        flMain.addView(drawView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                surfaceView.setMask(drawView.getMask());

                drawView.setVisibility(View.GONE);
                imageView = new ImageView(ImageMaskActivity.this);
                imageView.setLayoutParams(new FrameLayout.LayoutParams(400, 600, Gravity.RIGHT));
                flMain.addView(imageView);
//                imageView.setImageBitmap(drawView.getMask());
            }
        });

        setImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 9);
            }
        });

        setContentView(flMain);

//        drawView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
////                        Log.d(TAG, "down");
////                        surfaceView.startSpline(event);
////                        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//
//                        drawView.drawDown(event.getX(), event.getY());
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        drawView.drawUp(event.getX(), event.getY());
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
////                        Log.d(TAG, "move");
////                        surfaceView.addSplineWaypoint(event);
////                        Log.v("Draw", "fucking draw");
//                        drawView.drawMove(event.getX(), event.getY());
//                        return true;
//                }
//
//                return false;
//            }
//        });

//        drawSurfView.setAlpha(0.3f);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.d(TAG, "result");

        if(resultCode == RESULT_OK) {
            Log.d(TAG, data.getData().toString());
            imageUri = data.getData();
            needRender = true;
            imageView.setImageURI(imageUri);

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(needRender) {
            surfaceView.setImage(imageUri);
            surfaceView.requestRender();
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            try {
                InputStream is = this.getContentResolver().openInputStream(imageUri);
                BitmapFactory.decodeStream(is, null, o);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }

            drawView.setImageSize(o.outWidth, o.outHeight);
            drawView.setLayoutParams(new FrameLayout.LayoutParams(sizer.viewSize(120), (int)((float)sizer.viewSize(120)*((float)o.outHeight/(float)o.outWidth))));
            surfaceView.setLayoutParams(new FrameLayout.LayoutParams(sizer.viewSize(120), (int)((float)sizer.viewSize(120)*((float)o.outHeight/(float)o.outWidth))));
        }
    }
}
