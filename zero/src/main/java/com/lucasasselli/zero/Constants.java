package com.lucasasselli.zero;

@SuppressWarnings("WeakerAccess")
public class Constants {

    // System
    public final static String PACKAGE_NAME = "com.lucasasselli.zero";
    public final static String PRO_NAME = "com.lucasasselli.zeropro";
    public final static String SERVICE_NAME = PACKAGE_NAME + ".wallpaper.MyWallpaperService";

    // Parallax
    public final static double SENSITIVITY_MIN = 0.1;
    public final static double SENSITIVITY_MAX = 0.5;
    public final static double DEPTH_MIN = 0.001;
    public final static double DEPTH_MAX = 0.01;
    public final static double FALLBACK_MIN = 0.0;
    public final static double FALLBACK_MAX = 0.05;
    public final static double ZOOM_MIN = 0.6;
    public final static double ZOOM_MAX = 1.0;
    public final static double SCROLL_AMOUNT_MIN = 0.3;
    public final static double SCROLL_AMOUNT_MAX = 0.05;

    // Sensor
    public final static double VERTICAL_FIX = 0.01;

    // Time
    public final static long T_CATALOG_EXPIRATION = 24 * 3600;
    public final static long T_SYNC_PERIOD = 24 * 3600;

    // File system
    public final static String FS_DIR_ZERO = "Zero";
    public final static String FS_DIR_CACHE = "cache";

    // Local data
    public final static String LD_CATALOG = "catalog";
    public final static String LD_TIMESTAMP = "timestamp";

    // Background
    public final static String BG_FORMAT = ".png";
    public final static String BG_CUSTOM_NAME = "custom";
    public final static String BG_CUSTOM_ID = "custom";

    // Api
    public final static String URL_API = "http://www.lazyengineering.eu/zero/app/get.php";
    public final static String URL_FIELD_ID = "id";
    public final static String URL_FIELD_OBJ = "obj";
    public final static String URL_OBJ_THUMBNAIL = "t";
    public final static String URL_OBJ_PREVIEW = "p";
    public final static String URL_OBJ_WALLPAPER = "w";
    public final static String URL_FIELD_KEY = "key";

    // Preferences
    // NOTE: These are internal preferences not available to the user in the settings
    public final static String PREF_BACKGROUND = "background";
    public final static String PREF_BACKGROUND_DEFAULT = "00000";
    public final static String PREF_CHECKSENS = "checksens";
    public final static boolean PREF_CHECKSENS_DEFAULT = true;
}
