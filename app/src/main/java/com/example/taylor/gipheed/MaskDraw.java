package com.example.taylor.gipheed;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Taylor on 7/4/2016.
 */
public class MaskDraw {

    private final static String TAG = "MaskDraw";
    private final static int COORDS_PER_VERTEX = 2;

    private final static float[] SEGMENT_COORDS =  {
            1, -1,
            0, 0,
            1, 1,
            -1, -1,
            0.5f, 1
//            -1,0,
//            0, 0
    };

    private FloatBuffer vertexBuffer;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int program;
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = SEGMENT_COORDS.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private float color[] = {0, 1f, 0, 1f};

    private ByteBuffer bb;

    private ArrayList<ArrayList<Float>> vertexCoords = new ArrayList<>();
    private ArrayList<Float> currentSpline;

    private float lastX;
    private float lastY;
    private float currentX;
    private float currentY;

    public MaskDraw() {
        ByteBuffer bb = ByteBuffer.allocateDirect(SEGMENT_COORDS.length*4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SEGMENT_COORDS);
        vertexBuffer.position(0);

        program = GLES20.glCreateProgram();
        int vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        GLES20.glLineWidth(10.0f);

    }

    public void draw() {
        GLES20.glUseProgram(program);

        mColorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mPositionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        for(ArrayList<Float> spline: vertexCoords) {
            ByteBuffer bbf = ByteBuffer.allocateDirect(spline.size()*4);
            bbf.order(ByteOrder.nativeOrder());
            vertexBuffer = bbf.asFloatBuffer();

            vertexBuffer.clear();
            vertexBuffer.position(0);
            for(Float coord : spline) {
//                Log.d("MaskDraw", "spline coord: " + coord);
                vertexBuffer.put(coord);
            }

            vertexBuffer.position(0);

            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, spline.size()/COORDS_PER_VERTEX);
        }



        GLES20.glDisableVertexAttribArray(mPositionHandle);



    }

    public void startNewSpline (float x, float y) {
        Log.d(TAG, "starting new spline");

        currentX = x;
        currentY = y;

        currentSpline = new ArrayList<>();
        currentSpline.add(x-0.01f);
        currentSpline.add(y);
        currentSpline.add(x+0.01f);
        currentSpline.add(y);
        vertexCoords.add(currentSpline);
    }

    public void addSegment(float x, float y) {
        Log.d(TAG, "adding segment " + x + " " + y);
//        currentSpline.add(x);
//        currentSpline.add(y);

        lastY = currentY;
        lastX = currentX;
        currentY = y;
        currentX = x;

        float xl;
        float yl;
        float xr;
        float yr;
        float angle = (float) Math.atan2(Math.abs(y - lastY), Math.abs(x - lastX));

        Log.v(TAG, "angle: " + angle);
        if(y-lastY > 0.0f && x-lastX > 0.0f) {
            xl = x - 0.05f * (float) Math.sin(angle);
            yl = y + 0.05f * (float) Math.cos(angle);
            xr = x + 0.05f * (float) Math.sin(angle);
            yr = y - 0.05f * (float) Math.cos(angle);
        } else if(y-lastY > 0.0f && x-lastX < 0.0f) {
            xl = x - 0.05f * (float) Math.sin(angle);
            yl = y - 0.05f * (float) Math.cos(angle);
            xr = x + 0.05f * (float) Math.sin(angle);
            yr = y + 0.05f * (float) Math.cos(angle);
        } else if(y-lastY < 0.0f && x-lastX < 0.0f) {
            xl = x - 0.05f * (float) Math.sin(angle);
            yl = y + 0.05f * (float) Math.cos(angle);
            xr = x + 0.05f * (float) Math.sin(angle);
            yr = y - 0.05f * (float) Math.cos(angle);
        } else {
            xl = x - 0.05f * (float) Math.sin(angle);
            yl = y - 0.05f * (float) Math.cos(angle);
            xr = x + 0.05f * (float) Math.sin(angle);
            yr = y + 0.05f * (float) Math.cos(angle);
        }

        vertexCoords.get(vertexCoords.size()-1).add(xl);
        vertexCoords.get(vertexCoords.size()-1).add(yl);
        vertexCoords.get(vertexCoords.size()-1).add(xr);
        vertexCoords.get(vertexCoords.size()-1).add(yr);
    }

}
