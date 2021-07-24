package com.zero.zerolivewallpaper.wallpaper;

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

            // Set version
            setEGLContextClientVersion(2);
            setPreserveEGLContextOnPause(true);

            // Set renderer
            renderer = new MyRenderer(getApplicationContext());
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