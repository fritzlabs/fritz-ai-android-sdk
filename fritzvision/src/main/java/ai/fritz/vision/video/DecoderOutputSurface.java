package ai.fritz.vision.video;

// Adapted from: https://bigflake.com/mediacodec

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

/**
 * Holds state associated with a Surface used for MediaCodec decoder output.
 * <p>
 * The (width,height) constructor for this class will prepare GL, create a SurfaceTexture,
 * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
 * MediaCodec.configure() to receive decoder output.  When a frame arrives, we latch the
 * texture with updateTexImage, then render the texture with GL to a pbuffer.
 * <p>
 * The no-arg constructor skips the GL preparation step and doesn't allocate a pbuffer.
 * Instead, it just creates the Surface and SurfaceTexture, and when a frame arrives
 * we just draw it on whatever surface is current.
 * <p>
 * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
 * can potentially drop frames.
 */
class DecoderOutputSurface {

    private static final String TAG = DecoderOutputSurface.class.getSimpleName();
    private static final String THREAD_NAME = "Decoder Output Surface Callback Thread";
    private static final int TIMEOUT_MS = 500;
    private static final int EGL_ES2_BIT = 4;
    private final Object frameSync = new Object();
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;
    private SurfaceTexture outputTexture;
    private Surface outputSurface;
    private SurfaceTextureRenderer textureRenderer;
    private HandlerThread handlerThread;
    private boolean isFrameAvailable;

    /**
     * Creates an DecoderOutputSurface backed by a pbuffer with the specified dimensions. The new
     * EGL context and surface will be made current. Creates a Surface that can be passed
     * to MediaCodec.configure().
     */
    DecoderOutputSurface(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid dimensions.");
        }
        eglSetup(width, height);
        makeCurrent();
        setup();
    }

    /**
     * Creates an OutputSurface using the current EGL context (rather than establishing a
     * new one). Creates a Surface that can be passed to MediaCodec.configure().
     */
    DecoderOutputSurface() {
        setup();
    }

    /**
     * Creates instances of SurfaceTextureRender and SurfaceTexture, and a Surface associated
     * with the SurfaceTexture.
     */
    private void setup() {
        textureRenderer = new SurfaceTextureRenderer();
        textureRenderer.surfaceCreated();

        // Listen for frames on a different thread to avoid waiting on the main thread.
        handlerThread = new HandlerThread(THREAD_NAME);
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        outputTexture = new SurfaceTexture(textureRenderer.getTextureId());
        outputTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                synchronized (frameSync) {
                    if (isFrameAvailable) {
                        throw new RuntimeException("Frame already available, frame could be dropped");
                    }
                    isFrameAvailable = true;
                    frameSync.notifyAll();
                }
            }
        }, handler);
        outputSurface = new Surface(outputTexture);
    }

    /**
     * Prepares EGL. We want a GLES 2.0 context and a surface that supports pbuffer.
     */
    private void eglSetup(int width, int height) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        final int[] handles = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, handles, 0, handles, 1)) {
            throw new RuntimeException("unable to initialize EGL14");
        }
        // Configure EGL for pbuffer and OpenGL ES 2.0
        int[] configAttributes = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_RENDERABLE_TYPE, EGL_ES2_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(eglDisplay, configAttributes, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+pbuffer EGL config");
        }
        // Configure context for OpenGL ES 2.0
        int[] contextAttributes = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
        checkEglError("eglCreateContext");
        if (eglContext == null) {
            throw new RuntimeException("null context");
        }
        // Create a pbuffer surface. Enables glReadPixels.
        int[] surfaceAttributes = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };
        eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, configs[0], surfaceAttributes, 0);
        checkEglError("eglCreatePbufferSurface");
        if (eglSurface == null) {
            throw new RuntimeException("Unable to create surface.");
        }
    }

    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    void release() {
        if (EGL14.eglGetCurrentContext().equals(eglContext)) {
            releaseEglContext();
        }
        EGL14.eglDestroySurface(eglDisplay, eglSurface);
        EGL14.eglDestroyContext(eglDisplay, eglContext);

        handlerThread.quitSafely();
        outputSurface.release();
        eglDisplay = null;
        eglContext = null;
        eglSurface = null;
        textureRenderer = null;
        outputSurface = null;
        outputTexture = null;
    }

    /**
     * Attach EGL context to the current thread.
     */
    void makeCurrent() {
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Detach EGL context from the current thread.
     */
    void releaseEglContext() {
        if (!EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Returns the Surface that we draw onto.
     */
    Surface getSurface() {
        return outputSurface;
    }

    /**
     * Replaces the fragment shader.
     *
     * @param fragmentShader Code for the fragment shader.
     */
    void changeFragmentShader(String fragmentShader) {
        textureRenderer.changeFragmentShader(fragmentShader);
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the DecoderOutputSurface object, after the onFrameAvailable callback has signaled that new
     * data is available.
     */
    void awaitNewImage() {
        synchronized (frameSync) {
            while (!isFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us. Use a timeout to avoid
                    // stalling if it doesn't arrive.
                    frameSync.wait(TIMEOUT_MS);
                    if (!isFrameAvailable) {
                        throw new RuntimeException("Surface frame wait timed out.");
                    }
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
            isFrameAvailable = false;
        }
        // Latch the data.
        textureRenderer.checkGlError("before updateTexImage");
        outputTexture.updateTexImage();
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    void drawImage() {
        textureRenderer.drawFrame(outputTexture);
    }

    /**
     * Checks for EGL errors.
     *
     * @param operation The EGL operation to check.
     */
    private void checkEglError(String operation) {
        int error = EGL14.eglGetError();
        if (error != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(operation + ": EGL error " + error);
        }
    }
}