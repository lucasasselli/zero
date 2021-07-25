package com.zero.zerolivewallpaper.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zero.zerolivewallpaper.R;

@SuppressWarnings("SameParameterValue")
public class InfoView extends RelativeLayout {

    // Layout
    private LinearLayout infoInner;
    private TextView mainText;
    private TextView secondaryText;

    public InfoView(Context context) {
        super(context);
        init();
    }

    public InfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.info_view, this);

        // Load content
        infoInner = findViewById(R.id.info_inner);
        mainText = findViewById(R.id.info_text_main);
        secondaryText = findViewById(R.id.info_text_secondary);
    }

    public void show(int main, int secondary) {
        infoInner.setVisibility(VISIBLE);

        mainText.setText(main);
        secondaryText.setText(secondary);
    }

    public void hide() {
        infoInner.setVisibility(GONE);
    }
}
