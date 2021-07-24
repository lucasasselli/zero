package com.zero.zerolivewallpaper.wallpaper;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class GLLayer {

    private final float[] vertices = {
            -1, -1,
            1, -1,
            -1, 1,
            1, 1,
    };

    private final float[] textureVertices = {
            0, 1,
            1, 1,
            0, 0,
            1, 0
    };

    private FloatBuffer verticesBuffer;
    private FloatBuffer textureBuffer;

    private int vertexShader;
    private int fragmentShader;

    private int program;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec2 aTexPosition;" +
                    "varying vec2 vTexPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * aPosition;" +
                    "  vTexPosition = aTexPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vTexPosition;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, vTexPosition);" +
                    "}";

    private void initializeBuffers() {
        ByteBuffer buff = ByteBuffer.allocateDirect(vertices.length * 4);
        buff.order(ByteOrder.nativeOrder());
        verticesBuffer = buff.asFloatBuffer();
        verticesBuffer.put(vertices);
        verticesBuffer.position(0);

        buff = ByteBuffer.allocateDirect(textureVertices.length * 4);
        buff.order(ByteOrder.nativeOrder());
        textureBuffer = buff.asFloatBuffer();
        textureBuffer.put(textureVertices);
        textureBuffer.position(0);
    }

    private void initializeProgram() {
        vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        GLES20.glLinkProgram(program);
    }

    void draw(int texture, float[] mvpMatrix) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(program);

        GLES20.glEnable(GLES20.GL_BLEND);

        // Note: android uses premultiplied alpha for bitmaps!
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        int textureHandle = GLES20.glGetUniformLocation(program, "uTexture");
        int texturePositionHandle = GLES20.glGetAttribLocation(program, "aTexPosition");
        int MVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        GLES20.glVertexAttribPointer(texturePositionHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(texturePositionHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(textureHandle, 0);

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, verticesBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    GLLayer() {
        initializeBuffers();
        initializeProgram();
    }

}