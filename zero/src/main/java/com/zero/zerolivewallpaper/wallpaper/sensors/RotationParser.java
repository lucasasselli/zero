package com.zero.zerolivewallpaper.wallpaper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;

public class RotationParser extends GenericParser {

    private double oldRoll;
    private double oldPitch;
    private double deltaCross;

    // Rotation direction fixer
    private double oldAngle;
    private double baseRoll;

    public RotationParser(Context context) {
        super(context);
    }

    @Override
    public Sensor[] getSensors() {
        return new Sensor[]{getSensorManager().getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)};
    }

    @Override
    public double[] parse(SensorEvent event) {

        float[] sensorValues = event.values;
        float[] fixedValues = new float[4];

        // Remap axis according to orientation
        fixOrientation(sensorValues, fixedValues);

        float[] rotationMatrix = new float[9];

        // Compute rotation matrix
        SensorManager.getRotationMatrixFromVector(rotationMatrix, fixedValues);

        return parseRoatationMatrix(rotationMatrix);
    }

    @Override
    public void reset() {
        oldRoll = 0;
        oldPitch = 0;
        deltaCross = 0;
        baseRoll = 0;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    double[] parseRoatationMatrix(float[] rotationMatrix) {
        // Remap for pitch
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotationMatrix);

        // Grab pitch
        double pitch = toDegrees(Math.asin(rotationMatrix[7]));

        // Roll can be extracted without problems when pitch is smaller than 70 degrees
        float[] orientationValues = new float[3];
        double roll;
        if (pitch < 70) {
            roll = toDegrees(Math.atan2(-rotationMatrix[6], abs(rotationMatrix[8])));
        } else {
            // Remap axis to extract roll
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_Z, rotationMatrix);
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            roll = -toDegrees(orientationValues[0]);
        }

        // Normalize roll (0, 90, 0, -90)
        /*if (roll > 90) {
            roll = 180 - roll;
        } else if (roll < -90) {
            roll = -180 - roll;
        }*/
        // TODO Move elsewhere
        if (oldAngle > 150 && roll < -150) {
            baseRoll += 360;
        } else if (oldAngle < -150 && roll > 150) {
            baseRoll -= 360;
        }
        oldAngle = roll;
        roll += baseRoll;

        // Fix cross panic
        if ((oldPitch < 70 && pitch >= 70) || (pitch < 70 && oldPitch >= 70)) {
            deltaCross = roll - oldRoll;
        }
        roll = roll - deltaCross;

        // Update old values
        oldRoll = roll;
        oldPitch = pitch;

        return new double[]{pitch, roll};
    }
}
