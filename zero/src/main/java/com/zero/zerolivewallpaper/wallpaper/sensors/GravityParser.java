package com.zero.zerolivewallpaper.wallpaper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import static com.zero.zerolivewallpaper.Constants.VERTICAL_FIX;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

public class GravityParser extends GenericParser {

    public GravityParser(Context context) {
        super(context);
    }

    @Override
    public Sensor[] getSensors() {
        return new Sensor[]{getSensorManager().getDefaultSensor(Sensor.TYPE_GRAVITY)};
    }

    @Override
    public double[] parse(SensorEvent event) {

        float[] sensorValues = event.values;
        float[] fixedValues = new float[3];

        // Remap axis according to orientation
        fixOrientation(sensorValues, fixedValues);

        // Compute the gravity vector module
        double module = sqrt(fixedValues[0] * fixedValues[0] + fixedValues[1] * fixedValues[1] + fixedValues[2] * fixedValues[2]);

        if (module != 0) {
            // Normalize
            fixedValues[0] /= module;
            fixedValues[1] /= module;
            fixedValues[2] /= module;
        }

        double pitch = 0;
        double roll = 0;

        // Compute roll and pitch
        if (fixedValues[2] != 0) {
            roll = toDegrees(atan2(fixedValues[0], sqrt(fixedValues[2] * fixedValues[2] + VERTICAL_FIX * fixedValues[1] * fixedValues[1])));

            if (fixedValues[0] != 0) {
                pitch = toDegrees(atan2(fixedValues[1], sqrt(fixedValues[0] * fixedValues[0] + fixedValues[2] * fixedValues[2])));
            }
        }

        return new double[]{pitch, roll};
    }

    @Override
    public void reset() {

    }
}
