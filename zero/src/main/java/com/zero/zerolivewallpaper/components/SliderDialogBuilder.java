package com.zero.zerolivewallpaper.components;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zero.zerolivewallpaper.R;

public class SliderDialogBuilder extends AlertDialog.Builder implements SeekBar.OnSeekBarChangeListener {

    // Layout
    private final LinearLayout sliderView;
    private final SeekBar seekBar;
    private final TextView progressText;

    private int progress;

    public SliderDialogBuilder(@NonNull Context context) {
        super(context);

        sliderView = (LinearLayout) View.inflate(context, R.layout.dialog_slider, null);
        setView(sliderView);

        seekBar = sliderView.findViewById(R.id.dialog_slider_seek);
        progressText = sliderView.findViewById(R.id.dialog_slider_text);

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(100);
    }

    public void setInitialValue(int value) {
        progress = value;
        setTextValue(progress);
        seekBar.setProgress(progress);
    }

    public int getValue() {
        return progress;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.progress = progress;
        setTextValue(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setTextValue(int value) {
        String progressString = value + " %";
        progressText.setText(progressString);
    }
}
