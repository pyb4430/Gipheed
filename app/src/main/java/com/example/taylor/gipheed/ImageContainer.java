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
 * Created by Taylor on 6/19/2016.
 */

// Draw the mask on the image and off screen in a framebuffer object, draw
public class ImageContainer {

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
                "if(maskReady == 0) {" +
                    "gl_FragColor = texture2D(texture, fragmentTexCoord);" +
                "}" +
                 "else if(maskReady == 1) {" +
                    "gl_FragColor =  vec4(texture2D(mask, fragmentTexCoord).rgb, texture2D(texture, fragmentTexCoord).r)*texture2D(texture, fragmentTexCoord).b;" +
                 "}" +
            "}"
            ;

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

    private int[] fragments;

    FloatBuffer vertexBuffer;
    FloatBuffer fragmentBuffer;

    int positionHandle;
    int[] textureHandle = new int[2];
    int vertexTexCoordHandle;
    int textureUniformHandle;
    int maskUniformHandle;
    int maskSetUniformHandle;

    int vertexStride = COORDS_PER_VERTEX * 4;
    int fragmentStride = COORDS_PER_FRAGMENT_VERTEX * 4;

    private Uri uri;

    private ContentResolver cr;

    private boolean imageSet;

    public ImageContainer() {

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
        Log.d(TAG, "error vertshader " + GLES20.glGetError() + " ");

        int fragShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        Log.d(TAG, "error fragshader " + GLES20.glGetError() + " ");

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertShader);
        Log.d(TAG, "error vertshader attach " + GLES20.glGetError() + " ");

        GLES20.glAttachShader(program, fragShader);
        Log.d(TAG, "error fragshader attach " + GLES20.glGetError() + " ");

//        positionHandle = 6;
//        GLES20.glBindAttribLocation(program, positionHandle, "position");
//        GLES20.glBindAttribLocation(program, vertexTexCoordHandle, "vertexTexCoord");

        GLES20.glLinkProgram(program);

        GLES20.glDeleteShader(vertShader);
        GLES20.glDeleteShader(fragShader);
        Log.d(TAG, "error 11 " + GLES20.glGetError() + " ");


    }

    public void draw() {
        Log.d(TAG, "error 8 " + GLES20.glGetError() + " ");

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Log.d(TAG, "error 7 " + GLES20.glGetError() + " ");

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        Log.d(TAG, "error 5 " + GLES20.glGetError() + " ");

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
        Log.d(TAG, "error 4 " + GLES20.glGetError() + " " + positionHandle + " " + program);


        vertexTexCoordHandle = GLES20.glGetAttribLocation(program, "vertexTexCoord");
        GLES20.glEnableVertexAttribArray(vertexTexCoordHandle);
        GLES20.glVertexAttribPointer(vertexTexCoordHandle, COORDS_PER_FRAGMENT_VERTEX, GLES20.GL_FLOAT, false, fragmentStride, fragmentBuffer);

        if(imageSet) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[1]);

            maskUniformHandle = GLES20.glGetUniformLocation(program, "texture");
            GLES20.glUniform1i(textureUniformHandle, 1);
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        textureUniformHandle = GLES20.glGetUniformLocation(program, "texture");
        Log.d(TAG, "error 2 " + GLES20.glGetError() + " ");

//        maskSetUniformHandle = GLES20.glGetUniformLocation(program, "maskReady");
//        GLES20.glUniform1i(textureUniformHandle, 0);
        Log.d(TAG, "error 1 " + GLES20.glGetError() + " ");



        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COORDS.length/COORDS_PER_VERTEX);
        Log.d(TAG, "error 0 " + GLES20.glGetError() + " ");

        GLES20.glDisableVertexAttribArray(vertexTexCoordHandle);
        GLES20.glDisableVertexAttribArray(positionHandle);

        Log.d(TAG, "GL error " + GLES20.glGetError());
    }

    public void setImage(Uri uri, ContentResolver cr) {
        this.uri = uri;
        this.cr = cr;
//        imageSet = true;
        Log.d(TAG, "before " + uri.getPath());
        GLES20.glGenTextures(2, textureHandle, 0);
        Log.d(TAG, "error 4.7 " + GLES20.glGetError() + " ");

        maskSetUniformHandle = GLES20.glGetUniformLocation(program, "maskReady");
        GLES20.glUniform1i(maskSetUniformHandle, 0);

        Log.d(TAG, "after");


        try {
            InputStream is = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Log.d(TAG, bitmap.getWidth() + " " + bitmap.getHeight());
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            Log.d(TAG, "error 4.6 " + GLES20.glGetError() + " ");

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            Log.d(TAG, "error 4.5 " + GLES20.glGetError() + " ");

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            Log.d(TAG, "error 4.4 " + GLES20.glGetError() + " ");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            Log.d(TAG, "error 4.3 " + GLES20.glGetError() + " ");

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void setMask(Bitmap bitmap) {
        imageSet = true;
        Log.d(TAG, "before");
//        GLES20.glGenTextures(1, textureHandle, 0);
        Log.d(TAG, "error 4.7 " + GLES20.glGetError() + " ");

        Log.d(TAG, "after");

        maskSetUniformHandle = GLES20.glGetUniformLocation(program, "maskReady");
        GLES20.glUniform1i(maskSetUniformHandle, 1);

        try {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            Log.d(TAG, bitmap.getWidth() + " " + bitmap.getHeight());
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[1]);
            Log.d(TAG, "error 4.6 " + GLES20.glGetError() + " ");

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            Log.d(TAG, "error 4.5 " + GLES20.glGetError() + " ");

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            Log.d(TAG, "error 4.4 " + GLES20.glGetError() + " ");

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
//
