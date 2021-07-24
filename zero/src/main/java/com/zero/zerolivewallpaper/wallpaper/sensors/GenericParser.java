package com.zero.zerolivewallpaper.wallpaper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public abstract class GenericParser {

    private final Display display;

    private final SensorManager sensorManager;

    GenericParser(Context context) {
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        reset();
    }

    public abstract Sensor[] getSensors();

    // Parse a sensor event and returns rotation
    public abstract double[] parse(SensorEvent event);

    // resets any internal data
    protected abstract void reset();

    void fixOrientation(float[] input, float[] fixed) {
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                fixed[0] = input[0];
                fixed[1] = input[1];
                break;
            case Surface.ROTATION_90:
                fixed[0] = -input[1];
                fixed[1] = input[0];
                break;
            case Surface.ROTATION_180:
                fixed[0] = -input[0];
                fixed[1] = -input[1];
                break;
            case Surface.ROTATION_270:
                fixed[0] = input[1];
                fixed[1] = -input[0];
                break;
        }

        fixed[2] = input[2];

        if (input.length > 3) {
            fixed[3] = input[3];
        }
    }

    SensorManager getSensorManager() {
        return sensorManager;
    }
}
