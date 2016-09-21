package com.example.taylor.gipheed;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Taylor on 6/8/2016.
 */
public class Triangle {

    private FloatBuffer floatBuffer;
    public final int mPrgram;

    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {0, 0.6f, 0, -0.5f, 0, -.3f, 0, 0.5f, -.3f, 0 };

    float color[] = {0, 1f, 0, 1f};

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

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Triangle() {
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);

        bb.order(ByteOrder.nativeOrder());

        floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(triangleCoords);
        floatBuffer.position(0);

        int vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mPrgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mPrgram, vertexShader);
        GLES20.glAttachShader(mPrgram, fragmentShader);
        GLES20.glLinkProgram(mPrgram);


    }

    public void draw() {
        GLES20.glUseProgram(mPrgram);

        mPositionHandle = GLES20.glGetAttribLocation(mPrgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, floatBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mPrgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
