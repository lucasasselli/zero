package com.zero.zerolivewallpaper.utils;

import android.net.Uri;

import com.zero.zerolivewallpaper.data.CatalogItem;

import static com.zero.zerolivewallpaper.Constants.URL_API;

public class UrlFactory {

    public static String getDownloadUrl(CatalogItem item) {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendPath(item.getId() + ".zip");
        b.appendQueryParameter("raw", "true");
        return b.toString();
    }

    public static String getThumbnailUrl(CatalogItem item) {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendPath(item.getId() + ".png");
        b.appendQueryParameter("raw", "true");
        return b.toString();
    }

    public static String getPreviewUrl(CatalogItem item) {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendPath(item.getId() + ".mp4");
        b.appendQueryParameter("raw", "true");
        return b.toString();
    }

    public static String getCatalogUrl() {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendPath("catalog.json");
        b.appendQueryParameter("raw", "true");
        return b.toString();
    }
}
