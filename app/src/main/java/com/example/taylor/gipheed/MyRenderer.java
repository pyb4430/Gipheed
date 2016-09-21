package com.example.taylor.gipheed;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Taylor on 6/8/2016.
 */
public class MyRenderer implements GLSurfaceView.Renderer{

    private Triangle triangle;
    private ImageContainer imageContainer;
    private MaskDraw mask;

    private Uri imagePath;

    private int viewHeight;
    private int viewWidth;

    private Context context;

    private boolean needsLoadImage = false;

    MyRenderer(Context context) {
        this.context = context;
//        triangle = new Triangle();

//        mask = new MaskDraw();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.2f, 0.7f, 1f);
        imageContainer = new ImageContainer();
//        triangle = new Triangle();
//        imageContainer = new ImageContainer();
//        mask = new MaskDraw();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewHeight = height;
        viewWidth = width;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        Log.d("MyRenderer", "gl error in on draw frame: " + GLES20.glGetError());
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        triangle.draw();
        if(needsLoadImage) {
            needsLoadImage = false;
            imageContainer.setImage(imagePath, context.getContentResolver());
        }
        if(imagePath != null) {
            imageContainer.draw();
        }
//        mask.draw();
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    public void setImage(Uri uri, ContentResolver cr) {
        this.imagePath = uri;


//        imageContainer.setImage(uri, cr);
        needsLoadImage = true;
    }

    public void setMask(Bitmap maskBitmap) {

        imageContainer.setMask(maskBitmap);
    }

//    public void startSpline(MotionEvent event) {
//
//        float x = (event.getX()/ (float) viewWidth)*2.0f - 1.0f;
//        float y = (event.getY()/ (float) viewHeight)*2.0f - 1.0f;
//
//        mask.startNewSpline(x,-y);
//    }
//
//    public void addSplineWaypoint(MotionEvent event) {
//
//        float x = (event.getX()/ (float) viewWidth)*2.0f - 1.0f;
//        float y = (event.getY()/ (float) viewHeight)*2.0f - 1.0f;
//
//        Log.d("Renderer", "new spline waypoint: " + x + " " + y);
//        mask.addSegment(x,-y);
//    }
}
