package com.zero.zerolivewallpaper.wallpaper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.zero.zerolivewallpaper.wallpaper.DSP.LowPassFilter;
import com.zero.zerolivewallpaper.wallpaper.sensors.AccelerationParser;
import com.zero.zerolivewallpaper.wallpaper.sensors.GenericParser;
import com.zero.zerolivewallpaper.wallpaper.sensors.GravityParser;
import com.zero.zerolivewallpaper.wallpaper.sensors.RotationParser;

class Parallax implements SensorEventListener {

    private final String TAG = getClass().getSimpleName();

    // Filters
    private final LowPassFilter sensitivityFilter = new LowPassFilter(2);
    private final LowPassFilter fallbackFilter = new LowPassFilter(2);
    private double[] resetDeg = new double[2];
    private boolean filtersInit;

    // Outputs
    private double degX;
    private double degY;

    private final SensorManager sensorManager;
    private final GenericParser parser;
    private final Context context;

    Parallax(Context context) {

        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        parser = getParser();

        if (parser == null) {
            Log.e(TAG, "No valid sensor available!");
        }

        filtersInit = false;
    }

    double getDegX() {
        return degX;
    }

    double getDegY() {
        return degY;
    }

    void setFallback(double fallback) {
        fallbackFilter.setFactor(fallback);
    }

    void setSensitivity(double sensitivity) {
        sensitivityFilter.setFactor(sensitivity);
    }

    void start() {

        if (parser != null) {
            for (Sensor sensor : parser.getSensors()) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }

        Log.d(TAG, "Sensor listener started!");
    }

    void stop() {
        sensorManager.unregisterListener(this);

        Log.d(TAG, "Sensor listener stopped!");
    }

    // SensorEventListenerMethods
    @Override
    public void onSensorChanged(SensorEvent event) {

        double[] newDeg = parser.parse(event);

        // Set the initial value of the filters to current val
        if (!filtersInit) {
            sensitivityFilter.setLast(newDeg);
            fallbackFilter.setLast(newDeg);
            filtersInit = true;
        }

        // Apply filter
        newDeg = sensitivityFilter.filter(newDeg);

        degY = newDeg[0] - resetDeg[0];
        degX = newDeg[1] - resetDeg[1];

        resetDeg = fallbackFilter.filter(newDeg);

        if (degX > 180) {
            resetDeg[1] += degX - 180;
            degX = 180;
        }

        if (degX < -180) {
            resetDeg[1] += degX + 180;
            degX = -180;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Return the best sensor available
    private GenericParser getParser() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            Log.d(TAG, "Using rotation vector");
            return new RotationParser(context);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            Log.d(TAG, "Using gravity");
            return new GravityParser(context);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            Log.d(TAG, "Using accelerometer+magnetometer");
            return new AccelerationParser(context);
        }

        return null;
    }
}
