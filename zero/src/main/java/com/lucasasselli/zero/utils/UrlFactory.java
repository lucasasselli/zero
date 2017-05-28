package com.lucasasselli.zero.utils;

import android.net.Uri;

import com.lucasasselli.zero.data.CatalogItem;

import static com.lucasasselli.zero.Constants.URL_API;
import static com.lucasasselli.zero.Constants.URL_FIELD_ID;
import static com.lucasasselli.zero.Constants.URL_FIELD_KEY;
import static com.lucasasselli.zero.Constants.URL_FIELD_OBJ;
import static com.lucasasselli.zero.Constants.URL_OBJ_PREVIEW;
import static com.lucasasselli.zero.Constants.URL_OBJ_THUMBNAIL;
import static com.lucasasselli.zero.Constants.URL_OBJ_WALLPAPER;
import static com.lucasasselli.zero.Secrets.KEY_DOWNLOAD;

public class UrlFactory {

    public static String getDownloadUrl(CatalogItem item) {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendQueryParameter(URL_FIELD_ID, item.getId());
        b.appendQueryParameter(URL_FIELD_OBJ, URL_OBJ_WALLPAPER);

        // If it's a pro wallpaper add secret key
        if (item.isPro()) {
            b.appendQueryParameter(URL_FIELD_KEY, KEY_DOWNLOAD);
        }

        return b.toString();
    }

    public static String getThumbnailUrl(CatalogItem item) {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendQueryParameter(URL_FIELD_ID, item.getId());
        b.appendQueryParameter(URL_FIELD_OBJ, URL_OBJ_THUMBNAIL);
        return b.toString();
    }

    public static String getPreviewUrl(CatalogItem item) {
        Uri.Builder b = Uri.parse(URL_API).buildUpon();
        b.appendQueryParameter(URL_FIELD_ID, item.getId());
        b.appendQueryParameter(URL_FIELD_OBJ, URL_OBJ_PREVIEW);
        return b.toString();
    }

    public static String getCatalogUrl() {
        return URL_API;
    }
}
