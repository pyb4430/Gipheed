/*
 * Copyright 2016 Daniel Taylor Harrison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This work contains code from and code modeled after Google Inc.'s Grafika project, specifically the
 * classes Texture2dProgram.java, EglCore.java, ContinuousCaptureActivity.java, EglSurfaceBase.java,
 * Drawable2d.java, and FullFrameRect.java. This work also contains code from and code modeled after
 * The Android Open Source Project's Compatibility Test Suite project (specifically the jb-mr2-release branch)
 * Any code from the Grafika project or the Compatibility Test Suite project that is included in this work
 * may have been modified from its original form by the author of this work.
 * The Grafika project contains the following copyright notice:
 * Copyright 2013 Google Inc. All rights reserved.
 * The Compatibility Test Suite project contains the the following copyright notice:
 * Copyright (C) 2013 The Android Open Source Project
 *
 */

package com.example.taylor.gipheed.OpenGL;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by Taylor on 10/15/2016.
 */

public class GifFrameRect {

    private static final String TAG = "GifFrameRect";

    private final static int COORDS_PER_VERTEX = 2;
    private final static int COORDS_PER_FRAGMENT_VERTEX = 2;

    private final float[] VERTEX_COORDS = {
            -1,-1,
            1,-1,
            -1,1,
            1,1
    };

    private final float[] FRAGMENT_COORDS = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private static final String VERTEX_SHADER =
            "uniform mat4 uTexMatrix;" +
            "attribute vec4 position;" +
            "attribute vec4 vertexTexCoord;" +
            "varying vec2 fragmentTexCoord;" +
            "void main ()" +
            "{" +
                "gl_Position = position;" +
                "fragmentTexCoord = (uTexMatrix*vertexTexCoord).xy;" +
            "}"
            ;

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
            "uniform sampler2D texture;" +
            "varying vec2 fragmentTexCoord;" +
            "void main ()" +
            "{" +
                "gl_FragColor = texture2D(texture, fragmentTexCoord);" +
            "}"
            ;

    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 fragmentTexCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, fragmentTexCoord);\n" +
            "}\n";

    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int fragmentStride = COORDS_PER_FRAGMENT_VERTEX * 4;

    private FloatBuffer vertexBuffer;
    private FloatBuffer fragmentBuffer;

    private int programPointer;

    // program variable handles
    private int vertexPositionHandle;
    private int vertexTexCoordHandle;
    private int uTexMatrixHandle;

    public GifFrameRect() {
        GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

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

        int vertexShader = GLUtil.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = GLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_EXT);

        programPointer = GLES20.glCreateProgram();
        GLES20.glAttachShader(programPointer, vertexShader);
        GLES20.glAttachShader(programPointer, fragmentShader);
        GLES20.glLinkProgram(programPointer);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programPointer, GLES20.GL_LINK_STATUS, linkStatus, 0);

        Log.v(TAG, "shader GifFrameRect: " + GLES20.glGetError() + " " + (linkStatus[0]==GLES20.GL_TRUE) + " " + GLES20.glGetProgramInfoLog(programPointer));

    }

    public void draw(int textureId, float[] transformMatrix) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

//        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(programPointer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        vertexPositionHandle = GLES20.glGetAttribLocation(programPointer, "position");
        vertexTexCoordHandle = GLES20.glGetAttribLocation(programPointer, "vertexTexCoord");
        uTexMatrixHandle = GLES20.glGetUniformLocation(programPointer, "uTexMatrix");

        GLES20.glEnableVertexAttribArray(vertexPositionHandle);
        GLES20.glVertexAttribPointer(vertexPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(vertexTexCoordHandle);
        GLES20.glVertexAttribPointer(vertexTexCoordHandle, COORDS_PER_FRAGMENT_VERTEX, GLES20.GL_FLOAT, false, fragmentStride, fragmentBuffer);

        GLES20.glUniformMatrix4fv(uTexMatrixHandle, 1, false, transformMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COORDS.length/COORDS_PER_VERTEX);

        GLES20.glDisableVertexAttribArray(vertexTexCoordHandle);
        GLES20.glDisableVertexAttribArray(vertexPositionHandle);
        Log.v(TAG, "drawing GifFrameRect: " + GLES20.glGetError());

    }
}
