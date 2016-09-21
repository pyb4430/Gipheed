package com.example.taylor.gipheed;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Taylor on 8/17/2016.
 */
public class ImageContainerV2 {

    public final static String TAG = "ImageContainer";


    private final static int COORDS_PER_VERTEX = 2;
    private final static int COORDS_PER_FRAGMENT_VERTEX = 2;

    private final float[] VERTEX_COORDS = {
            -1,-1,
            1,-1,
            -1,1,
            1,1
    };


    private final float[] FRAGMENT_COORDS = {
            0, 1,
            1, 1,
            0, 0,
            1, 0
    };

    private final String vertexShader =
            "attribute vec4 position;" +
                    "attribute vec2 vertexTexCoord;" +
                    "varying vec2 fragmentTexCoord;" +
                    "void main ()" +
                    "{" +
                    "gl_Position = position;" +
                    "fragmentTexCoord = vertexTexCoord;" +
                    "}"
            ;

    private final String fragmentShader =
            "precision mediump float;" +
                    "uniform sampler2D texture;" +
                    "uniform sampler2D mask;" +
                    "uniform int maskReady;" +
                    "varying vec2 fragmentTexCoord;" +
                    "void main ()" +
                    "{" +
//                "if(maskReady == 0) {" +
                    "gl_FragColor = texture2D(texture, fragmentTexCoord);" +
//                "}" +
//                 "else if(maskReady == 1) {" +
//                    "gl_FragColor = vec4(texture2D(texture, fragmentTexCoord).rgb, texture2D(mask, fragmentTexCoord).b);" +
//                 "}" +
                    "}"
            ;

    int vertexStride = COORDS_PER_VERTEX * 4;
    int fragmentStride = COORDS_PER_FRAGMENT_VERTEX * 4;

    private int program;

    int positionHandle;
    int[] textureHandle = new int[1];
    int vertexTexCoordHandle;
    int textureUniformHandle;
    int maskUniformHandle;
    int maskSetUniformHandle;

    FloatBuffer vertexBuffer;
    FloatBuffer fragmentBuffer;

    public ImageContainerV2() {
        ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_COORDS.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(VERTEX_COORDS);
        vertexBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(FRAGMENT_COORDS.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        fragmentBuffer = bb2.asFloatBuffer();
        fragmentBuffer.put(FRAGMENT_COORDS);
        fragmentBuffer.position(0);

        int vertShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fragShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertShader);
        GLES20.glAttachShader(program, fragShader);

        GLES20.glLinkProgram(program);

        GLES20.glDeleteShader(vertShader);
        GLES20.glDeleteShader(fragShader);
    }

    public void setImage(Uri uri, ContentResolver cr) {
//        this.uri = uri;
//        this.cr = cr;
//        imageSet = true;
        GLES20.glGenTextures(1, textureHandle, 0);

        maskSetUniformHandle = GLES20.glGetUniformLocation(program, "maskReady");
        GLES20.glUniform1i(maskSetUniformHandle, 0);

        try {
            InputStream is = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void draw() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(program);

//        if(imageSet) {
//            Log.d(TAG, "before");
//            GLES20.glGenTextures(1, textureHandle, 0);
//            Log.d(TAG, "error 4.7 " + GLES20.glGetError() + " ");
//
//            Log.d(TAG, "after");
//
//            try {
//                InputStream is = cr.openInputStream(uri);
//                Bitmap bitmap = BitmapFactory.decodeStream(is);
//                Log.d(TAG, bitmap.getWidth() + " " + bitmap.getHeight());
//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
//                Log.d(TAG, "error 4.6 " + GLES20.glGetError() + " ");
//
//                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//                Log.d(TAG, "error 4.5 " + GLES20.glGetError() + " ");
//
//                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//
//                Log.d(TAG, "error 4.4 " + GLES20.glGetError() + " ");
//
//                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//            } catch (Exception e) {
//                Log.d(TAG, e.getMessage());
//            }
//        }

        positionHandle = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        vertexTexCoordHandle = GLES20.glGetAttribLocation(program, "vertexTexCoord");
        GLES20.glEnableVertexAttribArray(vertexTexCoordHandle);
        GLES20.glVertexAttribPointer(vertexTexCoordHandle, COORDS_PER_FRAGMENT_VERTEX, GLES20.GL_FLOAT, false, fragmentStride, fragmentBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        textureUniformHandle = GLES20.glGetUniformLocation(program, "texture");
        maskSetUniformHandle = GLES20.glGetUniformLocation(program, "maskReady");

        GLES20.glUniform1i(textureUniformHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COORDS.length/COORDS_PER_VERTEX);

        GLES20.glDisableVertexAttribArray(vertexTexCoordHandle);
        GLES20.glDisableVertexAttribArray(positionHandle);

    }
}
