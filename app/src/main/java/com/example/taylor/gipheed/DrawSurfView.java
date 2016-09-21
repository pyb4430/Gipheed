package com.example.taylor.gipheed;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

/**
 * Created by Taylor on 8/7/2016.
 */
public class DrawSurfView extends SurfaceView implements SurfaceHolder.Callback{

    private DrawingThread drawingThread;
    private SurfaceHolder surfaceHolder;


    DrawSurfView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        drawingThread = new DrawingThread(new WeakReference<SurfaceHolder>(surfaceHolder));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Paint paint;
//        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.BLUE);
//        canvas.drawCircle(0, 0, 1000, paint);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void drawUp(float x, float y) {
        drawingThread.drawUp(x, y);
    }

    public void drawDown(float x, float y) {
        drawingThread.drawDown(x, y);
    }

    public void drawMove(float x, float y) {
        Log.v("DrawSurfView", "drawing ");
        drawingThread.drawMove(x, y);
    }

    static class DrawingThread extends Thread {
        WeakReference<SurfaceHolder> holderWeakRef;

        private float x;
        private float y;

        private Paint paint;
        private Path drawPath;
        private Path drawPathLine;

        private Paint mPaint;
        private Paint circlePaint;

        DrawingThread(WeakReference<SurfaceHolder> holderWeakRef) {
            super();
            this.holderWeakRef = holderWeakRef;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(30f);
            paint.setStrokeCap(Paint.Cap.ROUND);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.GREEN);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(30f);

            circlePaint = new Paint();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);

            drawPath = new Path();
            drawPathLine = new Path();
        }

        public void drawUp(float x, float y) {
            drawPathLine.lineTo(x, y);
            run();
            drawPathLine.reset();
        }

        public void drawDown(float x, float y) {
            drawPathLine.reset();
            drawPathLine.moveTo(x,y);
//            drawPath.addCircle(x, y, 30, Path.Direction.CW);

            this.x = x;
            this.y = y;
        }

        public void drawMove(float x, float y) {


            drawPathLine.quadTo(this.x, this.y, (this.x+x)/2, (this.y+y)/2);
//            drawPath.addCircle(x, y, 30, Path.Direction.CW);
            this.x = x;
            this.y = y;
            run();

        }

        @Override
        public void run() {
            Log.v("DrawSurfView", "drawing point : " + x + " " + y);
            SurfaceHolder surfaceHolder = holderWeakRef.get();
            Canvas canvas = surfaceHolder.lockCanvas();

//            canvas.drawCircle(x, y, 30, paint);

//            canvas.drawPath(drawPath, circlePaint);
            canvas.drawPath(drawPathLine, mPaint);

            surfaceHolder.unlockCanvasAndPost(canvas);
            Log.v("DrawSurfView", "drawn");


        }
    }
}
