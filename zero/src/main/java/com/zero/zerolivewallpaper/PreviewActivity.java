package com.zero.zerolivewallpaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zero.zerolivewallpaper.async.MyAsync;
import com.zero.zerolivewallpaper.components.SquareVideoView;
import com.zero.zerolivewallpaper.data.CatalogItem;
import com.zero.zerolivewallpaper.utils.StorageHelper;
import com.zero.zerolivewallpaper.utils.UrlFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import static android.view.View.GONE;
import static com.zero.zerolivewallpaper.Constants.T_SERVER_TIMEOUT;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";

    // Constants
    public static final String EXTRA_CATALOG_ITEM = "item";
    public static final int PREVIEW_ACTIVITY_REQUEST_CODE = 5;

    // Layout
    private TextView titleText;
    private TextView authorText;
    private TextView errorText;
    private ImageView downloadBtn;
    private ImageView linkBtn;
    private SquareVideoView videoView;
    private ProgressBar progressBar;

    // Data
    private CatalogItem catalogItem;


    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Grab extra
        Intent intent = getIntent();

        if (intent != null) {
            catalogItem = intent.getExtras().getParcelable(EXTRA_CATALOG_ITEM);
        } else {
            finish();
        }

        // Get preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set title text
        titleText = findViewById(R.id.prev_text_title);
        titleText.setText(catalogItem.getTitle());

        // Set author text
        authorText = findViewById(R.id.prev_text_author);
        authorText.setText(catalogItem.getAuthor());

        // Set error text
        errorText = findViewById(R.id.prev_text_error);
        errorText.setVisibility(GONE);

        // Set download button
        downloadBtn = findViewById(R.id.prev_btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_CATALOG_ITEM, catalogItem);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        // Set link button
        linkBtn = findViewById(R.id.prev_btn_link);
        if (catalogItem.getSite() != null && !catalogItem.getSite().equals("")) {
            // Site field set
            linkBtn.setVisibility(View.VISIBLE);
            linkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open authors site
                    Utils.openBrowser(getApplicationContext(), catalogItem.getSite());
                }
            });
        } else {
            // Site field empty
            linkBtn.setVisibility(GONE);
        }

        // Set video preview
        videoView = findViewById(R.id.prev_video);
        progressBar = findViewById(R.id.prev_progress);
        videoView.setZOrderOnTop(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                showError(R.string.error_preview_unknown);
                return true;
            }
        });

        // Get the preview
        File previewFile = StorageHelper.getPreviewFile(catalogItem.getId(), this);
        if (previewFile != null && previewFile.exists()) {
            setVideo(Uri.fromFile(previewFile));
            Log.d(TAG, "Preview file \"" + previewFile.getName() + "\" already downloaded!");
        } else {
            if (Utils.checkConnection(this)) {
                new PreviewDownloader(this, catalogItem).execute();
            } else {
                showError(R.string.error_connection);
            }
        }
    }

    private void setVideo(Uri uri) {
        progressBar.setVisibility(GONE);

        videoView.setVideoURI(uri);
        videoView.start();

        // Show preview toast
        boolean firstPrev = sharedPreferences.getBoolean(Constants.PREF_FIRSTPREV, Constants.PREF_FIRSTPREV_DEFAULT);
        if (firstPrev) {
            Toast.makeText(this, getString(R.string.prev_thisisaprev_toast), Toast.LENGTH_LONG).show();
            sharedPreferences.edit().putBoolean(Constants.PREF_FIRSTPREV, false).apply();
        }
    }

    private void showError(int stringId) {
        progressBar.setVisibility(GONE);

        errorText.setVisibility(View.VISIBLE);
        errorText.setText(getString(stringId));
    }

    private class PreviewDownloader extends MyAsync {

        // Log
        private final String TAG = getClass().getSimpleName();

        // Constants
        public static final int RESULT_NOTFOUND = 2;
        public static final int RESULT_TIMEOUT = 3;

        private final CatalogItem catalogItem;
        private final Context context;
        private File downloadFile;

        PreviewDownloader(Context context, CatalogItem catalogItem) {
            super(context);
            this.catalogItem = catalogItem;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            downloadFile = StorageHelper.getPreviewFile(catalogItem.getId(), context);
            if (downloadFile == null) return RESULT_FAIL;

            HttpURLConnection urlConnection = null;

            InputStream input = null;
            OutputStream output = null;

            try {
                String urlString = UrlFactory.getPreviewUrl(catalogItem);
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(T_SERVER_TIMEOUT);
                urlConnection.setReadTimeout(T_SERVER_TIMEOUT);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return RESULT_NOTFOUND;
                } else if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
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
            } catch (SocketTimeoutException ignored) {
                Log.e(TAG, "Connection timeout!");
                return RESULT_TIMEOUT;
            } catch (Exception e) {
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

            return RESULT_SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            // if we get here, length is known, now set indeterminate to false
            progressBar.setIndeterminate(true);
            progressBar.setProgress(progress[0]);
            progressBar.setMax(100);

        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == RESULT_SUCCESS) {
                setVideo(Uri.fromFile(downloadFile));
            }else if (result == RESULT_NOTFOUND) {
                showError(R.string.error_preview_notfound);
            } else if (result == RESULT_TIMEOUT) {
                showError(R.string.error_timeout);
            } else {
                showError(R.string.error_preview_unknown);
            }
        }

    }
}
