package com.zero.zerolivewallpaper.wallpaper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

@SuppressLint("Registered")
public class GLWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return null;
    }

    class GLEngine extends Engine {

        private WallpaperGLSurfaceView glSurfaceView;
        private boolean rendererHasBeenSet;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (rendererHasBeenSet) {
                if (visible) {
                    glSurfaceView.onResume();
                } else {
                    glSurfaceView.onPause();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            glSurfaceView.onDestroy();
        }

        void setRenderer(GLSurfaceView.Renderer renderer) {
            glSurfaceView.setRenderer(renderer);
            rendererHasBeenSet = true;
        }

        @SuppressWarnings("SameParameterValue")
        void setEGLContextClientVersion(int version) {
            glSurfaceView.setEGLContextClientVersion(version);
        }

        @SuppressWarnings("SameParameterValue")
        void setPreserveEGLContextOnPause(boolean preserve) {
            glSurfaceView.setPreserveEGLContextOnPause(preserve);
        }

        class WallpaperGLSurfaceView extends GLSurfaceView {

            WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onDestroy() {
                super.onDetachedFromWindow();
            }
        }
    }
}
