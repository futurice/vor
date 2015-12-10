package com.futurice.hereandnow.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.futurice.hereandnow.utils.BeaconLocationManager;

public class LocationService extends Service {
    private static final int DELAY = 5000;
    private static final String TAG = "LocationService";

    private boolean isRunning;
    private Context context;
    private BeaconLocationManager beaconLocationManager;

    private Handler handler;

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        beaconLocationManager = new BeaconLocationManager(this);
        beaconLocationManager.initialize();
        handler = new Handler();
        Log.d(TAG, "LocationService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.isRunning) {
            this.isRunning = true;
            beaconLocationManager.resume();
            handler.postDelayed(locationUpdate, DELAY);
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
        Log.d(TAG, "LocationService destroyed");
        this.isRunning = false;
        handler = null;
        beaconLocationManager.destroy();
    }

    /**
     * Send the client's location to the server.
     */
    private Runnable locationUpdate = new Runnable() {
        @Override
        public void run() {
            beaconLocationManager.sendLocation();
            handler.postDelayed(this, DELAY);
        }
    };
}
