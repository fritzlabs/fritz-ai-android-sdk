package ai.fritz.vision.video;

// Adapted from: https://bigflake.com/mediacodec

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
class SurfaceTextureRenderer {

    private static final String TAG = SurfaceTextureRenderer.class.getSimpleName();
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTICES_DATA_ST_OFFSET = 3;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    
    private final float[] verticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };
    private FloatBuffer vertices;
    private float[] mvpMatrix = new float[16];
    private float[] stMatrix = new float[16];
    private int textureId = -1;
    private int program;
    private int mvpHandle;
    private int stHandle;
    private int positionHandle;
    private int texBufferHandle;

    SurfaceTextureRenderer() {
        vertices = ByteBuffer.allocateDirect(
                verticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(verticesData).position(0);
        Matrix.setIdentityM(stMatrix, 0);
    }

    int getTextureId() {
        return textureId;
    }

    /**
     * Draws the current frame.
     *
     * @param source The texture with frame data.
     */
    void drawFrame(SurfaceTexture source) {
        checkGlError("onDrawFrame start");
        source.getTransformMatrix(stMatrix);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);
        checkGlError("glUseProgram");

        // Set the texture to draw on and assign the source
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        // Connect vertex buffer to "aPosition".
        // Enable the "aPosition" vertex attribute.
        vertices.position(VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                VERTICES_DATA_STRIDE_BYTES, vertices);
        checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(positionHandle);
        checkGlError("glEnableVertexAttribArray");

        // Connect texture buffer to "aTextureCoord".
        // Enable the "aTextureCoord" vertex attribute.
        vertices.position(VERTICES_DATA_ST_OFFSET);
        GLES20.glVertexAttribPointer(texBufferHandle, 2, GLES20.GL_FLOAT, false,
                VERTICES_DATA_STRIDE_BYTES, vertices);
        checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(texBufferHandle);
        checkGlError("glEnableVertexAttribArray");

        // Copy matrices and render data
        Matrix.setIdentityM(mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(stHandle, 1, false, stMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        GLES20.glFinish();
    }

    /**
     * Initializes GL state. Call this after the EGL surface has been created and made current.
     */
    void surfaceCreated() {
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (program == 0) {
            throw new RuntimeException("Failed creating program");
        }
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        checkLocation(positionHandle, "aPosition");

        texBufferHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        checkLocation(texBufferHandle, "aTextureCoord");

        mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        checkLocation(mvpHandle, "uMVPMatrix");

        stHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        checkLocation(stHandle, "uSTMatrix");

        // Generate a texture ID
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        // Assign the texture an ID
        textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        checkGlError("glBindTexture");

        // Assign texture parameters
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
    }

    /**
     * Replaces the fragment shader.
     */
    void changeFragmentShader(String fragmentShader) {
        GLES20.glDeleteProgram(program);
        program = createProgram(VERTEX_SHADER, fragmentShader);
        if (program == 0) {
            throw new RuntimeException("Failed creating program");
        }
    }

    /**
     * Load a shader into memory.
     *
     * @param shaderType The type of shader.
     * @param source The shader code.
     * @return Reference to the shader in memory.
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * Create the program used to render the texture.
     *
     * @param vertexSource The vertex shader code.
     * @param fragmentSource The fragment shader code.
     * @return Reference to the program in memory.
     */
    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * Checks for an error after performing a GL operation.
     *
     * @param operation The GL operation to check.
     */
    void checkGlError(String operation) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(operation + ": glError " + error);
        }
    }

    /**
     * Checks that a variable is located in the vertex shader.
     *
     * @param location Reference to the variable in memory.
     * @param label The name of the variable.
     */
    private void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }
}