package com.example.taylor.gipheed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Taylor on 8/7/2016.
 */
public class DrawView extends View {

    public static final float TOUCH_TOLERANCE = 4;

    private float x;
    private float y;

    private Paint paint;
    private Path drawPath;
    private Path drawPathLine;

    private Paint mPaint;
    private Paint circlePaint;
    private Paint mBitmapPaint;

    private Bitmap mBitmap;

    private ArrayList<Path> drawPathList;

    public DrawView(Context context) {
        super(context);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

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
        drawPathList = new ArrayList<>();
    }

    public void drawUp(float x, float y) {
        drawPathLine.lineTo(x, y);
        invalidate();
        drawPathList.add(new Path(drawPathLine));
//        drawPathLine.reset();
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
        invalidate();

    }

    private boolean needBitmap = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        if(needBitmap) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
//            needBitmap = false;
//        }
        for(Path path : drawPathList) {
            canvas.drawPath(path, mPaint);
        }
        canvas.drawPath(drawPathLine, mPaint);
    }

    public void setImageSize(int w, int h) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.BLUE);
    }

    Canvas mCanvas;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.BLUE);
    }

    private float mX;
    private float mY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        mScaleDetector.onTouchEvent(event);

        final int action = MotionEventCompat.getActionMasked(event);
        final int pointerIndex = MotionEventCompat.getActionIndex(event);
        final float x = MotionEventCompat.getX(event, pointerIndex);
        final float y = MotionEventCompat.getY(event, pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mX = x;
                mY = y;
                drawPathLine.reset();
                drawPathLine.moveTo(mX, mY);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    drawPathLine.quadTo(mX, mY, (mX + x) / 2, (mY + y) / 2);
                    mY = y;
                    mX = x;

//                    strokePath.reset();
//                    strokePath.addCircle(mX, mY, 50, Path.Direction.CW);
                }
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                drawPathLine.lineTo(mX, mY);
//                strokePath.reset();

                mCanvas.drawPath(drawPathLine, mPaint);
                drawPathLine.reset();

                invalidate();
                break;
            }
        }

        return true;
    }

    public Bitmap getMask() {
//        for(int x=0;x<mBitmap.getWidth();x++){
//            for(int y=0;y<mBitmap.getHeight();y++){
//                if(mBitmap.getPixel(x, y)==Color.GREEN){
//                    mBitmap.setPixel(x, y, Color.TRANSPARENT);
//                }
//            }
//        }
        return mBitmap;
    }
}
