package com.lucasasselli.zero.wallpaper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLWallpaperPreview extends GLSurfaceView {

    private MyRenderer renderer;

    private final Context context;

    public GLWallpaperPreview(Context context) {
        super(context);
        this.context = context;

    }

    public GLWallpaperPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void init(String id) {
        setEGLContextClientVersion(2);
        renderer = new MyRenderer(context, id);
        setRenderer(renderer);
    }

    public void start() {
        renderer.start();
    }

    public void stop() {
        renderer.stop();
    }
}