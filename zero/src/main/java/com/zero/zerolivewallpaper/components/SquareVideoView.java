package com.zero.zerolivewallpaper.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.VideoView;

public class SquareVideoView extends VideoView {

    private Display display;

    public SquareVideoView(Context context) {
        super(context);
        init(context);
    }

    public SquareVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }
    }
}
