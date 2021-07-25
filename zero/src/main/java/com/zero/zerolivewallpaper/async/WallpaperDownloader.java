package com.zero.zerolivewallpaper.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.zero.zerolivewallpaper.R;
import com.zero.zerolivewallpaper.data.CatalogItem;
import com.zero.zerolivewallpaper.utils.UrlFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.zero.zerolivewallpaper.Constants.T_SERVER_TIMEOUT;
import static com.zero.zerolivewallpaper.Utils.bytes2String;
import static com.zero.zerolivewallpaper.utils.StorageHelper.deleteFolder;
import static com.zero.zerolivewallpaper.utils.StorageHelper.getCacheFolder;
import static com.zero.zerolivewallpaper.utils.StorageHelper.getRootFolder;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class WallpaperDownloader extends MyAsync {

    // Log
    private final String TAG = getClass().getSimpleName();

    // Constants
    public static final int ID = 1;
    public static final String EXTRA_CATALOG_ITEM = "item";
    public static final String EXTRA_FAIL_CODE = "fail_code";
    public static final int FAIL_CODE_GENERIC = 0;
    public static final int FAIL_CODE_TIMEOUT = 1;

    // Layout
    private final ProgressDialog progressDialog;

    // Data
    private final CatalogItem catalogItem;

    private final Context context;

    public WallpaperDownloader(Context context, CatalogItem item) {
        super(context);

        this.context = context;
        this.catalogItem = item;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.main_ongoing_download));
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Show progress dialog
        progressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {

        String fileName = catalogItem.getId() + ".zip";

        // Create destination
        File downloadFolder = getCacheFolder(context);
        if (downloadFolder == null) return RESULT_FAIL;

        File downloadFile = new File(downloadFolder.getPath(), fileName);

        HttpURLConnection urlConnection = null;
        InputStream input = null;
        OutputStream output = null;

        try {
            String urlString = UrlFactory.getDownloadUrl(catalogItem);
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(T_SERVER_TIMEOUT);
            urlConnection.setReadTimeout(T_SERVER_TIMEOUT);
            urlConnection.connect();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return RESULT_FAIL;
            }

            // Get the length
            int fileLength = urlConnection.getContentLength();

            // Download the file
            input = urlConnection.getInputStream();
            output = new FileOutputStream(downloadFile);

            byte[] data = new byte[4096];
            int total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress(total * 100 / fileLength, total, fileLength);
                output.write(data, 0, count);
            }
        } catch (SocketTimeoutException e) {
            // Connection timeout
            Log.e(TAG, "Connection timeout");
            return RESULT_TIMEOUT;

        } catch (Exception e) {
            // Generic error
            Log.e(TAG, "IO Exception");
            e.printStackTrace();
            return RESULT_FAIL;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
            }

            if (urlConnection != null) urlConnection.disconnect();
        }

        // Create unzip destination
        File root = getRootFolder(context);
        if (root == null) {
            return RESULT_FAIL;
        }
        File unzipDestination = new File(root.getPath(), catalogItem.getId());
        // Check if destination already exist
        if (unzipDestination.exists()) {
            // Shouldn't exist: deleted
            Log.d(TAG, "Destination folder \"" + unzipDestination.getPath() + "\" already exist: delete it!");
            if (!deleteFolder(unzipDestination)) {
                Log.e(TAG, "Unable to deleted destionation folder \"" + unzipDestination.getPath() + "\"");
                return RESULT_FAIL;
            }
        }
        if (!unzipDestination.mkdir()) {
            Log.e(TAG, "Unable to create destination folder \"" + unzipDestination.getPath() + "\"");
            return RESULT_FAIL;
        }

        // Unzip it
        ZipInputStream zipInput;

        try {
            input = new FileInputStream(downloadFile);
            zipInput = new ZipInputStream(new BufferedInputStream(input));
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            int count;

            while ((zipEntry = zipInput.getNextEntry()) != null) {
                String unzipFileName = zipEntry.getName();

                // This shouldn't happen
                if (zipEntry.isDirectory()) {
                    File fmd = new File(unzipDestination.getPath(), unzipFileName);
                    fmd.mkdirs();
                    continue;
                }


                if (unzipFileName.contains("/")) {
                    String[] folders = unzipFileName.split("/");
                    for (String item : folders) {
                        File fmd = new File(unzipDestination.getPath(), item);
                        if (!item.contains(".") && !fmd.exists()) {
                            fmd.mkdirs();
                            Log.d("created folder", item);
                        }
                    }
                }

                output = new FileOutputStream(new File(unzipDestination.getPath(), unzipFileName));

                while ((count = zipInput.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                output.close();
                zipInput.closeEntry();
            }

            zipInput.close();
        } catch (IOException e) {
            e.printStackTrace();
            return RESULT_FAIL;
        }

        // Delete downloaded file
        downloadFile.delete();

        return RESULT_SUCCESS;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        // if we get here, length is known, now set indeterminate to false
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(progress[0]);
        progressDialog.setProgressNumberFormat((bytes2String(progress[1])) + "/" + (bytes2String(progress[2])));

    }

    @Override
    protected void onPostExecute(Integer result) {

        // Dismiss progress dialog, if exist
        try {
            if ((progressDialog != null) && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // IGNORE
        }

        // Call listener
        Bundle bundle = new Bundle();
        if (result == RESULT_SUCCESS) {
            // Success
            bundle.putParcelable(EXTRA_CATALOG_ITEM, catalogItem);
            getListener().onCompleted(ID, bundle);
        } else if (result == RESULT_TIMEOUT) {
            // Error: timeout
            bundle.putInt(EXTRA_FAIL_CODE, FAIL_CODE_TIMEOUT);
            getListener().onFailed(ID, bundle);
        } else {
            // Error: generic
            bundle.putInt(EXTRA_FAIL_CODE, FAIL_CODE_GENERIC);
            getListener().onFailed(ID, bundle);
        }
    }
}
