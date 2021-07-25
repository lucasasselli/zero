package com.zero.zerolivewallpaper.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;

public abstract class MyAsync extends AsyncTask<Void, Integer, Integer> {

    // Constants
    public static final int RESULT_FAIL = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_TIMEOUT = 2;

    private final Context context;

    private PowerManager.WakeLock wakeLock;

    private MyAsyncInterface listener;

    public MyAsync(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire(5*60);
    }

    @Override
    protected void onPostExecute(Integer aInteger) {
        super.onPostExecute(aInteger);
        wakeLock.release();
    }

    public MyAsync setListener(MyAsyncInterface listerner) {
        this.listener = listerner;
        return this;
    }

    MyAsyncInterface getListener() {
        return listener;
    }

    public interface MyAsyncInterface {
        void onCompleted(int id, Bundle extra);
        void onFailed(int id, Bundle extra);
    }
}