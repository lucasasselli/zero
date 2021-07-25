package com.zero.zerolivewallpaper.utils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.zero.zerolivewallpaper.Constants.FS_DIR_CACHE;
import static com.zero.zerolivewallpaper.Constants.FS_DIR_ZERO;

public class StorageHelper {

    private static final String TAG = "StorageHelper";

    public static File getRootFolder(Context context) {
        // Get Zero folder
        String filePath = context.getFilesDir().toString();
        filePath = filePath + "/" + FS_DIR_ZERO + "/";
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        } else {
            if (file.mkdir()) {
                Log.e(TAG, "Root directory \"" + file.getPath() + "\" not found: create it");
                return file;
            } else {
                Log.e(TAG, "Unable to create root directory \"" + file.getPath() + "\"");
            }
        }

        return null;
    }

    public static File getCacheFolder(Context context) {
        File root = getRootFolder(context);
        if (root != null) {
            String filePath = root.getPath() + "/" + FS_DIR_CACHE + "/";
            File file = new File(filePath);
            if (file.exists()) {
                return file;
            } else {
                if (file.mkdir()) {
                    Log.e(TAG, "Download directory \"" + file.getPath() + "\" not found: create it");
                    return file;
                } else {
                    Log.e(TAG, "Unable to create download directory \"" + file.getPath() + "\"");
                }
            }
        }

        return null;
    }

    public static File getBackgroundFolder(String id, Context context) {
        File[] files; // File holder

        // Get files from root folder
        File root = getRootFolder(context);
        if (root != null) {
            files = root.listFiles();
        } else {
            return null;
        }

        File backgroundPath = null;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().equals(id)) {
                    backgroundPath = file;
                }
            }
        } else {
            Log.e(TAG, "Directory " + id + " returned null content! Are permission ok?");
            return null;
        }

        return backgroundPath;
    }

    public static File getPreviewFile(String id, Context context) {
        File root = getCacheFolder(context);
        if (root != null) {
            return new File(root.getPath() + "/" + id + ".mp4");
        }

        return null;
    }

    // Lists all the downloaded wallpapers ids
    public static List<String> getDownloadedIds(Context context) {
        List<String> idList = new ArrayList<>(); // Output list
        File[] files; // File holder

        // Get files from root folder
        File root = getRootFolder(context);
        if (root != null) {
            files = root.listFiles();
        } else {
            return null;
        }

        // Scan file list and fill id list
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    idList.add(file.getName());
                }
            }
        } else {
            Log.e(TAG, "Unable to access directory! Are permission ok?");
            return null;
        }

        return idList;
    }

    public static boolean backgroundExist(String id, Context context) {
        File file = getBackgroundFolder(id, context);
        return (file != null);
    }

    public static boolean deleteFolder(File file) {
        if (file != null) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                Log.e(TAG, "Unable to delete folder " + file.getName(), e);
                return false;
            }

            return true;
        } else {
            return false;
        }
    }
}
