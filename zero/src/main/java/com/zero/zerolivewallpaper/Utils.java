package com.zero.zerolivewallpaper;


import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import com.zero.zerolivewallpaper.wallpaper.MyWallpaperService;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@SuppressWarnings("SameParameterValue")
public class Utils {

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Get string between to string (excluded)
    public static String getBetweenStrings(String text, String textFrom, String textTo) {

        String result = text.substring(
                text.indexOf(textFrom) + textFrom.length()
        );

        // Cut the excessive ending of the text:
        result = result.substring(0, result.indexOf(textTo));

        return result;
    }

    // Seconds since EPOCH
    public static long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    // Get average color of a bitmap
    public static int calculateAverageColor(android.graphics.Bitmap bitmap, int pixelSpacing) {
        int R = 0;
        int G = 0;
        int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += pixelSpacing) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return Color.rgb(R / n, G / n, B / n);
    }

    // Check if sensor is available
    public static boolean sensorsAvailable(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            return true;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            return true;
        }

        return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
    }

    // Open system live wallpaper setter
    public static void openLWSetter(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, MyWallpaperService.class));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_wpsetter, Toast.LENGTH_LONG).show();
        }
    }

    // Open browser
    public static void openBrowser(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            context.startActivity(browserIntent);
        } catch (RuntimeException e) {
            Toast.makeText(context, R.string.error_browser, Toast.LENGTH_SHORT).show();
        }
    }

    private static final double SPACE_KB = 1024;
    private static final double SPACE_MB = 1024 * SPACE_KB;
    private static final double SPACE_GB = 1024 * SPACE_MB;
    private static final double SPACE_TB = 1024 * SPACE_GB;

    public static String bytes2String(long sizeInBytes) {

        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        try {
            if (sizeInBytes < SPACE_KB) {
                return nf.format(sizeInBytes) + " Byte(s)";
            } else if (sizeInBytes < SPACE_MB) {
                return nf.format(sizeInBytes / SPACE_KB) + " KB";
            } else if (sizeInBytes < SPACE_GB) {
                return nf.format(sizeInBytes / SPACE_MB) + " MB";
            } else if (sizeInBytes < SPACE_TB) {
                return nf.format(sizeInBytes / SPACE_GB) + " GB";
            } else {
                return nf.format(sizeInBytes / SPACE_TB) + " TB";
            }
        } catch (Exception e) {
            return sizeInBytes + " Byte(s)";
        }

    }
}
