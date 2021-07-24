package com.zero.zerolivewallpaper.wallpaper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.zero.zerolivewallpaper.wallpaper.DSP.RollingAverageFilter;

public class AccelerationParser extends RotationParser {

    private float[] accValues;
    private float[] magValues;
    private double[] degHolder = {0.0, 0.0};
    private final RollingAverageFilter accFilt = new RollingAverageFilter(3, 5);
    private final RollingAverageFilter magFilt = new RollingAverageFilter(3, 5);

    public AccelerationParser(Context context) {
        super(context);
    }

    @Override
    public Sensor[] getSensors() {
        return new Sensor[]{
                getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)};
    }

    @Override
    public double[] parse(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accValues = new float[3];
            fixOrientation(event.values, accValues);
            accFilt.add(accValues);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magValues = new float[3];
            fixOrientation(event.values, magValues);
            magFilt.add(magValues);
        }

        if (magValues != null && accValues != null) {
            float[] rotationMatrix = new float[9];

            if (SensorManager.getRotationMatrix(rotationMatrix, null, accFilt.getAverage(), magFilt.getAverage())) {
                degHolder = parseRoatationMatrix(rotationMatrix);
            }
        }

        return degHolder;
    }
}
