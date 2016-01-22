package com.futurice.vor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.futurice.vor.VorApplication;
import com.futurice.vor.utils.BeaconLocationManager;

public class LocationService extends Service {
    private static final int DELAY = 3000;
    private static final String TAG = "LocationService";

    private boolean mIsRunning;
    private BeaconLocationManager mBeaconLocationManager;

    private Handler mHandler;

    @Override
    public void onCreate() {
        mIsRunning = false;
        mBeaconLocationManager = VorApplication.getBeaconLocationManager();
        mHandler = new Handler();
        Log.d(TAG, "LocationService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mIsRunning) {
            mIsRunning = true;
            mBeaconLocationManager.resume();
            mHandler.postDelayed(locationUpdate, DELAY);
            Log.d(TAG, "LocationService started");
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mIsRunning) {
            mIsRunning = false;
            mHandler = null;
            mBeaconLocationManager.pause();
            super.onDestroy();
        }
    }

    /**
     * Send the client's location to the server.
     */
    private Runnable locationUpdate = new Runnable() {
        @Override
        public void run() {
            if (mHandler != null) {
                mBeaconLocationManager.calculateLocation(true);
                mHandler.postDelayed(this, DELAY);
            }
        }
    };
}
