package com.zero.zerolivewallpaper.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zero.zerolivewallpaper.R;

import java.io.File;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.zero.zerolivewallpaper.Constants.DEPTH_MAX;
import static com.zero.zerolivewallpaper.Constants.DEPTH_MIN;
import static com.zero.zerolivewallpaper.Constants.DIM_MAX;
import static com.zero.zerolivewallpaper.Constants.FALLBACK_MAX;
import static com.zero.zerolivewallpaper.Constants.FALLBACK_MIN;
import static com.zero.zerolivewallpaper.Constants.PREF_BACKGROUND;
import static com.zero.zerolivewallpaper.Constants.PREF_BACKGROUND_DEFAULT;
import static com.zero.zerolivewallpaper.Constants.SCROLL_AMOUNT_MAX;
import static com.zero.zerolivewallpaper.Constants.SCROLL_AMOUNT_MIN;
import static com.zero.zerolivewallpaper.Constants.SENSITIVITY_MAX;
import static com.zero.zerolivewallpaper.Constants.SENSITIVITY_MIN;
import static com.zero.zerolivewallpaper.Constants.ZOOM_MAX;
import static com.zero.zerolivewallpaper.Constants.ZOOM_MIN;
import static java.lang.Math.abs;

class MyRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "Renderer";

    // Screen
    private int orientation;
    private float deltaXMax;
    private float deltaYMax;

    // Values
    private boolean deltaInit;
    private float[][] deltaArrayNew;
    private float[][] deltaArrayOld;

    // Preferences
    private final SharedPreferences sharedPreferences;
    private boolean prefSensor;
    private boolean prefScroll;
    private boolean prefLimit;
    private double prefDepth;
    private double prefScrollAmount;
    private float prefZoom;
    private int prefDim;
    private String prefWallpaperId;

    // External
    private double offset;

    // Internal
    private String loadedWallpaperId;
    private boolean isPreview;
    private boolean hasOverlay = false;
    private boolean isFallback = false;

    private List<BackgroundHelper.Layer> layerList;
    
    // Opengl stuff
    private GLLayer glLayer;
    private final float[] MVPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private int[] textures;

    private final Parallax parallax;
    private final Context context;

    // Default constructor
    MyRenderer(Context context) {

        this.context = context;
        parallax = new Parallax(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Not a preview
        isPreview = false;

        start();
    }

    // Preview constructor
    MyRenderer(Context context, String prefWallpaperId) {

        this.context = context;
        parallax = new Parallax(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Is a preview
        this.prefWallpaperId = prefWallpaperId;
        isPreview = true;

        start();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Nothing
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // Refit wallpaper to match screen orientation
        if (orientation == ORIENTATION_PORTRAIT) {
            float ratio = (float) width / height;
            deltaXMax = (0.5f * ratio) / prefZoom;
            deltaYMax = (1 - prefZoom);
            Matrix.frustumM(projectionMatrix, 0, -ratio * prefZoom, ratio * prefZoom, -prefZoom, prefZoom, 3, 7);
        } else {
            float ratio = (float) height / width;
            deltaXMax = (1 - prefZoom);
            deltaYMax = (0.5f * ratio) / prefZoom;
            Matrix.frustumM(projectionMatrix, 0, -prefZoom, prefZoom, -ratio * prefZoom, ratio * prefZoom, 3, 7);
        }

        // Create layers only if wallpaper has changed
        if (!prefWallpaperId.equals(loadedWallpaperId)) {
            generateLayers();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Initialize arrays
        if (!deltaInit) {
            deltaArrayNew = new float[textures.length][2];
            deltaArrayOld = new float[textures.length][2];
            deltaInit = true;
        }

        // Compute deltas
        for (int i = 0; i < textures.length - 1; i++) {
            // Get layer z
            double z;
            if (!isFallback) {
                z = layerList.get(i).getZ();
            } else {
                z = 0;
            }

            // Compute the launcher page offset
            double scrollOffset;
            if (prefScroll && z != 0) {
                scrollOffset = offset / (prefScrollAmount * z);
            } else {
                scrollOffset = 0;
            }

            // Compute the x-y offset
            float deltaX = (float) -(scrollOffset + (parallax.getDegX() / 180.0 * (prefDepth * z)));
            float deltaY = (float) (parallax.getDegY() / 180.0 * (prefDepth * z));

            // Limit max offset
            if ((abs(deltaX) > deltaXMax || abs(deltaY) > deltaYMax) && prefLimit) {
                deltaArrayNew = deltaArrayOld.clone();
                break;
            }

            deltaArrayOld = deltaArrayNew.clone();

            deltaArrayNew[i][0] = deltaX;
            deltaArrayNew[i][1] = deltaY;
        }

        int layerCount;
        if (hasOverlay) {
            layerCount = textures.length - 1;
        } else {
            layerCount = textures.length;
        }

        // Draw layers
        for (int i = 0; i < layerCount; i++) {
            float[] layerMatrix = MVPMatrix.clone();
            Matrix.translateM(layerMatrix, 0, deltaArrayNew[i][0], deltaArrayNew[i][1], 0);
            glLayer.draw(textures[i], layerMatrix);
        }

        // Overlay
        if (hasOverlay) {
            // Has an overlay
            float[] layerMatrix = MVPMatrix.clone();
            glLayer.draw(textures[textures.length - 1], layerMatrix);
        }
    }

    // This method must be called every time the renderer is started or to reload the settings
    public void start() {

        reloadSettings();

        deltaInit = false;

        // Get current screen orientation
        orientation = context.getResources().getConfiguration().orientation;

        if (prefSensor) parallax.start();
    }

    // Only pauses the sensor! OpenGL view is managed elsewhere
    void stop() {
        if (prefSensor) parallax.stop();
    }

    private void reloadSettings() {
        // If preview render use provided id, else load it from settings
        if (!isPreview) {
            prefWallpaperId = sharedPreferences.getString(PREF_BACKGROUND, PREF_BACKGROUND_DEFAULT);
        }

        prefSensor = sharedPreferences.getBoolean(context.getString(R.string.pref_sensor_key), context.getResources().getBoolean(R.bool.pref_sensor_default));
        prefLimit = sharedPreferences.getBoolean(context.getString(R.string.pref_limit_key), context.getResources().getBoolean(R.bool.pref_limit_default));

        String depthString = sharedPreferences.getString(context.getString(R.string.pref_depth_key), context.getString(R.string.pref_depth_default));
        prefDepth = DEPTH_MIN + Double.parseDouble(depthString) * (DEPTH_MAX / 100.0);

        String sensitivityString = sharedPreferences.getString(context.getString(R.string.pref_sensitivity_key), context.getString(R.string.pref_sensitivity_default));
        double sensitivity = SENSITIVITY_MIN + Double.parseDouble(sensitivityString) * (SENSITIVITY_MAX / 100.0);

        String fallbackString = sharedPreferences.getString(context.getString(R.string.pref_fallback_key), context.getString(R.string.pref_fallback_default));
        double fallback = FALLBACK_MIN + Double.parseDouble(fallbackString) * (FALLBACK_MAX / 100.0);

        String zoomString = sharedPreferences.getString(context.getString(R.string.pref_zoom_key), context.getString(R.string.pref_zoom_default));
        prefZoom = (float) (ZOOM_MIN + (100 - Double.parseDouble(zoomString)) * ((ZOOM_MAX - ZOOM_MIN) / 100.0));

        prefScroll = sharedPreferences.getBoolean(context.getString(R.string.pref_scroll_key), context.getResources().getBoolean(R.bool.pref_scroll_default));

        String scrollAmountString = sharedPreferences.getString(context.getString(R.string.pref_scroll_amount_key), context.getString(R.string.pref_scroll_amount_default));
        prefScrollAmount = SCROLL_AMOUNT_MIN + Double.parseDouble(scrollAmountString) * (SCROLL_AMOUNT_MAX / 100.0);

        String dimString = sharedPreferences.getString(context.getString(R.string.pref_dim_key), context.getString(R.string.pref_dim_default));
        prefDim = (int) ((Double.parseDouble(dimString)) * (DIM_MAX / 100.0));

        // Set parallax settings
        parallax.setFallback(fallback);
        parallax.setSensitivity(sensitivity);
    }

    private void generateLayers() {
        // Clean old textures (if any) before loading the new ones
        clearTextures();

        // Assume that the layer is fallback
        int layerCount;
        isFallback = false;
        hasOverlay = true;

        if (!prefWallpaperId.equals(PREF_BACKGROUND_DEFAULT)) {
            // If the wallpaper is not the fallback one
            layerList = BackgroundHelper.loadFromFile(prefWallpaperId, context);
            if (layerList != null) {
                // Layer loaded correctly
                prefWallpaperId = PREF_BACKGROUND_DEFAULT;
                isFallback = false;
                layerCount = layerList.size();
            } else {
                deployFallbackWallpaper();
                return;
            }
        } else {
            deployFallbackWallpaper();
            return;
        }

        // Useful info
        int width = 0;
        int height = 0;

        // Create glTexture array
        textures = new int[layerCount + 1];
        GLES20.glGenTextures(layerCount + 1, textures, 0); // Layer + Overlay

        Bitmap tempBitmap;

        for (int i = 0; i < textures.length; i++) {
            if (i < textures.length - 1) {
                // Load bitmap
                File bitmapFile = layerList.get(i).getFile();
                tempBitmap = BackgroundHelper.decodeScaledFromFile(bitmapFile);
                tempBitmap.getWidth();

                width = tempBitmap.getWidth();
                height = tempBitmap.getHeight();
            } else {
                // Generate overlay
                tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                tempBitmap.eraseColor(Color.argb(prefDim, 0, 0, 0));
            }

            if (i == 0) {
                // Solid black background
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            try {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, tempBitmap, 0);
            } catch (NullPointerException e) {
                Log.e(TAG, "Null pointer wile genrating layers", e);
                deployFallbackWallpaper();
                return;
            }

            // Free memory
            tempBitmap.recycle();
        }

        glLayer = new GLLayer();

        // Set the loaded wallpaper id
        loadedWallpaperId = prefWallpaperId;
    }

    void setOffset(float offset) {
        this.offset = offset;
    }

    private void deployFallbackWallpaper() {
        clearTextures();

        isFallback = true;
        hasOverlay = false;

        Bitmap fallbackBitmap = BackgroundHelper.decodeScaledFromRes(context.getResources(), R.drawable.fallback);

        textures = new int[1];

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, fallbackBitmap, 0);

        glLayer = new GLLayer();

        loadedWallpaperId = PREF_BACKGROUND_DEFAULT;
    }

    private void clearTextures() {
        if (textures != null) {
            GLES20.glDeleteTextures(textures.length, textures, 0);
        }
    }
}
