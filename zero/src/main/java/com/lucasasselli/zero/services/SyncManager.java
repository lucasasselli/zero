package com.lucasasselli.zero.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.lucasasselli.zero.MainActivity;
import com.lucasasselli.zero.R;
import com.lucasasselli.zero.Utils;
import com.lucasasselli.zero.data.Catalog;
import com.lucasasselli.zero.data.CatalogItem;
import com.lucasasselli.zero.utils.InternalData;
import com.lucasasselli.zero.utils.UrlFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static com.lucasasselli.zero.Constants.LD_CATALOG;
import static com.lucasasselli.zero.Constants.LD_TIMESTAMP;
import static com.lucasasselli.zero.Constants.T_SERVER_TIMEOUT;
import static com.lucasasselli.zero.Constants.T_SYNC_PERIOD;
import static com.lucasasselli.zero.Utils.getTimestamp;

public class SyncManager extends IntentService {

    private final static String TAG = "SyncManager";

    // Constants
    private final static int NOTIFICATION_ID = 1;
    private final static String EXTRA_SILENT = "silent";
    public final static String EXTRA_RESULT = "result";
    public final static String ACTION_SYNC = "com.lucasasselli.zero.services.SyncManager.ACTION_SYNC";
    public final static int RESULT_SUCCESS = 0;
    public final static int RESULT_FAIL = 1;
    public final static int RESULT_TIMEOUT = 2;

    private NotificationManager notificationManager;
    private InternalData internalData;

    private static boolean isRunning;

    public static void start(Context context, boolean isSilent) {
        Intent intent = new Intent(context, SyncManager.class);
        intent.putExtra(EXTRA_SILENT, isSilent);
        context.startService(intent);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public SyncManager() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (!isRunning) {
            isRunning = true;
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        internalData = new InternalData(this);

        Catalog oldCatalog = new Catalog();
        Catalog newCatalog = new Catalog();

        int serviceResult = RESULT_SUCCESS;

        boolean isSilent;
        if (intent != null) {
            isSilent = intent.getBooleanExtra(EXTRA_SILENT, true);
        } else {
            serviceResult = RESULT_FAIL;
            isSilent = true;

            Log.e(TAG, "Null intent!");
        }

        if (isSilent) {
            Log.d(TAG, "Service started in loud mode!");
        } else {
            Log.d(TAG, "Service started in silent mode!");
        }

        // Retrieve current catalog
        oldCatalog.loadFromCache(this);

        if (Utils.checkConnection(this) && serviceResult == RESULT_SUCCESS) {
            serviceResult = downloadCatalog();
            if (serviceResult == RESULT_SUCCESS) {
                // Download okay!
                newCatalog.loadFromCache(this);

                boolean notificationEnabled = sharedPreferences.getBoolean(getString(R.string.pref_notification_key), getResources().getBoolean(R.bool.pref_notification_default));

                // Show notification only if isSilent (no broadcast)
                if (notificationEnabled && isSilent) {
                    if (newCatalog.size() > oldCatalog.size()) {
                        int newCount = newCatalog.size() - oldCatalog.size();
                        showNotification(newCount);
                        Log.d(TAG, "Catalog download completed: " + newCount + " new items.");
                    } else {
                        Log.d(TAG, "Catalog download completed: no new wallpaper.");
                    }
                }
            }
        } else {
            // End service
            serviceResult = RESULT_FAIL;

            Log.e(TAG, "ERROR: No internet connection!");
        }

        long wakeUpDelay = T_SYNC_PERIOD * 1000;

        // Set pending intent to this service
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, SyncManager.class), 0);

        // Alarm
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + wakeUpDelay, pendingIntent);

        Log.d(TAG, "Service tasks completed! Setting alarm in " + wakeUpDelay + " millis");

        // Send broadcast
        if (!isSilent) {
            // Broadcast
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_SYNC);
            broadcastIntent.putExtra(EXTRA_RESULT, serviceResult);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Reset flag
        isRunning = false;
    }

    private int downloadCatalog() {
        HttpURLConnection urlConnection = null;
        String response = "";

        try {
            String urlString = UrlFactory.getCatalogUrl();
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(T_SERVER_TIMEOUT);
            urlConnection.setReadTimeout(T_SERVER_TIMEOUT);
            urlConnection.connect();

            int code = urlConnection.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }

                in.close();
            }
        } catch (SocketTimeoutException ignored) {
            // Connection timeout
            Log.e(TAG, "Connection timeout");
            return RESULT_TIMEOUT;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL exception");
            e.printStackTrace();
            return RESULT_FAIL;
        } catch (IOException e) {
            Log.e(TAG, "IO exception");
            e.printStackTrace();
            return RESULT_FAIL;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (!response.isEmpty()) {

            // Parse with gson
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            CatalogItem[] items;

            try {
                items = gson.fromJson(response, CatalogItem[].class);
            } catch (JsonSyntaxException ex) {

                Log.e(TAG, "Json syntax error");
                return RESULT_FAIL;
            }

            // Store internally
            int result;

            result = internalData.saveString(response, LD_CATALOG);
            if (result == InternalData.ERROR) {
                return RESULT_FAIL;
            }

            result = internalData.saveLong(getTimestamp(), LD_TIMESTAMP);
            if (result == InternalData.ERROR) {
                return RESULT_FAIL;
            }

            Log.d(TAG, items.length + " catalog items loaded!");

            return RESULT_SUCCESS;

        } else {
            return RESULT_FAIL;
        }

    }

    private void showNotification(int newCount) {


        Intent disableIntent = new Intent(this, MainActivity.class);
        disableIntent.putExtra(MainActivity.EXTRA_DISABLE_NOTIFICATION, true);

        Intent startIntent = new Intent(this, MainActivity.class);

        PendingIntent startPending = PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(startPending);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_stat_zero_symbol);

        // Set different message if multiple wallpapers
        if (newCount > 0) {
            // Only one wallpaper
            builder.setTicker(getString(R.string.notif_new_title_one));
            builder.setContentTitle(getString(R.string.notif_new_title_one));
            builder.setContentText(getString(R.string.notif_new_message_one));
        } else {
            builder.setTicker(getString(R.string.notif_new_title_multi));
            builder.setContentTitle(getString(R.string.notif_new_title_multi));
            String message = newCount + " " + getString(R.string.notif_new_message_multi);
            builder.setContentText(message);
        }

        Notification notification = builder.build();

        // Send the notification.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // System start listener
    public static class AutoStart extends BroadcastReceiver {

        public AutoStart() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Boot completed, starting service...");

            start(context, true);
        }
    }
}