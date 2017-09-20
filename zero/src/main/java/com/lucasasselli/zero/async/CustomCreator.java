package com.lucasasselli.zero.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.lucasasselli.zero.R;

import java.io.IOException;
import java.io.InputStream;

import static com.lucasasselli.zero.utils.StorageHelper.storeCustomWallpaper;

public class CustomCreator extends MyAsync {

    // Log
    private final String TAG = getClass().getSimpleName();

    // Constants
    public static final int ID = 2;

    // Layout
    private ProgressDialog progressDialog;

    private final Context context;
    private final Uri uri;

    public CustomCreator(Context context, Uri uri) {
        super(context);

        this.context = context;
        this.uri = uri;

        init();
    }

    private void init() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.main_ongoing_customwp));
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {

        Bitmap bitmap;
        if (uri != null) {
            try {
                InputStream in = context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(in, null, null);
                if (in != null) in.close();
            } catch (IOException e) {
                return RESULT_SUCCESS;
            }
        } else {
            return RESULT_FAIL;
        }

        return storeCustomWallpaper(bitmap, context)?RESULT_SUCCESS:RESULT_FAIL;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        // Dismiss progress dialog, if exist
        try {
            if ((progressDialog != null) && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (final IllegalArgumentException e) {
            // IGNORE
        }

        // Call listener
        if (result == RESULT_SUCCESS) {
            getListener().onCompleted(ID, null);
        } else {
            getListener().onFailed(ID, null);
        }
    }
}
