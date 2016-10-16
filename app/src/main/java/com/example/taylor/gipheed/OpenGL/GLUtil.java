package com.example.taylor.gipheed.OpenGL;

import android.opengl.GLES20;

/**
 * Created by Taylor on 10/15/2016.
 */

public class GLUtil {

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

}
