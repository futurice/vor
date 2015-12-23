package com.futurice.hereandnow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.SettingsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.socket.client.Socket;

public class BeaconLocationManager {
    private static final String TAG = "BeaconLocation";

    public static final String IDENTIFIER_B1 = "futu-b1";
    public static final String IDENTIFIER_B2 = "futu-b2";
    public static final String IDENTIFIER_B3 = "futu-b3";

    public static final String BEACON_KEY = "beacon";

    public static final String BEACON_KEY_ID = "id";
    public static final String BEACON_KEY_DISTANCE = "distance";
    public static final String BEACON_KEY_USER_IDENTIFIER = "email";
    public static final String BEACON_KEY_FLOOR = "floor";
    public static final String BEACON_TEMPERATURE_KEY = "temperature";

    public static final int FLOOR_8 = 8;
    public static final int FLOOR_7 = 7;

    private FutuBeacon mB1;
    private FutuBeacon mB2;
    private FutuBeacon mB3;

    private BeaconManager mB1Manager;
    private BeaconManager mB2Manager;
    private BeaconManager mB3Manager;

    private Region mB1Region;
    private Region mB2Region;
    private Region mB3Region;

    private double mB1Accuracy;
    private double mB2Accuracy;
    private double mB3Accuracy;

    private OnLocationUpdateListener mLocationCallback;

    private Socket mSocket;

    Context context;
    SharedPreferences preferences;

    public BeaconLocationManager(Context c) {
        this.context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void initialize() {
        mB1Manager = new BeaconManager(context);
        mB2Manager = new BeaconManager(context);
        mB3Manager = new BeaconManager(context);

        mB1Manager = new BeaconManager(context);
        mB2Manager = new BeaconManager(context);
        mB3Manager = new BeaconManager(context);

        keepTrackOfBeacons();

        // Set ranging listeners.
        mB1Manager.setRangingListener((region, list) -> {
            if (!list.isEmpty()) {
                mB1Accuracy = Utils.computeAccuracy(list.get(0));
            }
        });
        mB2Manager.setRangingListener((region, list) -> {
            if (!list.isEmpty()) {
                mB2Accuracy = Utils.computeAccuracy(list.get(0));
            }
        });
        mB3Manager.setRangingListener((region, list) -> {
            if (!list.isEmpty()) {
                mB3Accuracy = Utils.computeAccuracy(list.get(0));
            }
        });
    }

    public void resume() {
        mB1Manager.connect(() -> mB1Manager.startRanging(mB1Region));
        mB2Manager.connect(() -> mB2Manager.startRanging(mB2Region));
        mB3Manager.connect(() -> mB3Manager.startRanging(mB3Region));
    }

    public void pause() {
        mB1Manager.stopRanging(mB1Region);
        mB2Manager.stopRanging(mB2Region);
        mB3Manager.stopRanging(mB3Region);
    }

    public void destroy() {
        mB1Manager.stopRanging(mB1Region);
        mB2Manager.stopRanging(mB2Region);
        mB3Manager.stopRanging(mB3Region);

        mB1Manager = null;
        mB2Manager = null;
        mB3Manager = null;

        mB1Region = null;
        mB2Region = null;
        mB3Region = null;

        mSocket.disconnect();
    }

    public void sendLocation() {
        // Send all beacon locations to the server.
        sendToServer(mB1, mB1Accuracy);
        sendToServer(mB2, mB2Accuracy);
        sendToServer(mB3, mB3Accuracy);
    }

    public void onLocation(JSONObject jsonObject) {
        if (mLocationCallback != null) {
            mLocationCallback.onLocationUpdate(jsonObject.toString());
        }
    }

    /**
     * http://developer.estimote.com/android/tutorial/part-2-background-monitoring/
     * There’s no way to keep track of “interim” beacons’ “enters” and “exits”
     * other than creating a single region per each beacon of course.
     */
    private void keepTrackOfBeacons() {
        UUID proximityUUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");

        mB1 = new FutuBeacon(IDENTIFIER_B1, 32095, 19454, FLOOR_7); // mint
        mB2 = new FutuBeacon(IDENTIFIER_B2, 23964, 38945, FLOOR_7); // blueberry
        mB3 = new FutuBeacon(IDENTIFIER_B3, 21061, 29133, FLOOR_7); // ice

        mB1Region = new Region(mB1.identifier, proximityUUID, mB1.major, mB1.minor);
        mB2Region = new Region(mB2.identifier, proximityUUID, mB2.major, mB2.minor);
        mB3Region = new Region(mB3.identifier, proximityUUID, mB3.major, mB3.minor);

        mB1Manager.connect(() -> mB1Manager.startMonitoring(mB1Region));
        mB2Manager.connect(() -> mB2Manager.startMonitoring(mB2Region));
        mB3Manager.connect(() -> mB3Manager.startMonitoring(mB3Region));
    }

    private void sendToServer(FutuBeacon beacon, double accuracy) {
        JSONObject jo = new JSONObject();
        try {
            jo.put(Constants.TYPE_KEY, BEACON_KEY);
            jo.put(BEACON_KEY_ID, beacon.identifier + "-" + beacon.major + "-" + beacon.minor);
            jo.put(BEACON_KEY_DISTANCE, accuracy);
            jo.put(BEACON_KEY_USER_IDENTIFIER, preferences.getString(SettingsActivity.EMAIL_KEY,
                    context.getString(R.string.pref_my_email_default)));
            jo.put(BEACON_KEY_FLOOR, beacon.floor);
            jo.put(BEACON_TEMPERATURE_KEY, 19.4f);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Sending " + jo.toString());
        HereAndNowApplication.getSocket().emit(Constants.MESSAGE_KEY, jo);
    }

    private static class FutuBeacon {
        String identifier;
        Integer major;
        Integer minor;
        Integer floor;

        private FutuBeacon(String identifier, Integer major, Integer minor, Integer floor) {
            this.identifier = identifier;
            this.major = major;
            this.minor = minor;
            this.floor = floor;
        }
    }

    /**
     * Inteface for receiving location updates.
     */
    public interface OnLocationUpdateListener {
        void onLocationUpdate(String position);

        void onConnectionError();
    }

    public void setOnLocationUpdateListener(OnLocationUpdateListener listener) {
        mLocationCallback = listener;
    }
}
