package com.lucasasselli.zero.wallpaper;

import android.view.SurfaceHolder;

public class MyWallpaperService extends GLWallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new OpenGLES2Engine();
    }

    private class OpenGLES2Engine extends GLWallpaperService.GLEngine {

        MyRenderer renderer;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            // Create the renderer
            renderer = new MyRenderer(getApplicationContext());

            // Request an OpenGL ES 2.0 compatible context.
            setEGLContextClientVersion(2);

            // On Honeycomb+ devices, this improves the performance when
            // leaving and resuming the live wallpaper.
            setPreserveEGLContextOnPause(true);

            // Set the renderer to our user-defined renderer.
            setRenderer(renderer);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                renderer.start();
            } else {
                renderer.stop();
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            renderer.setOffset(xOffset);
        }
    }
}