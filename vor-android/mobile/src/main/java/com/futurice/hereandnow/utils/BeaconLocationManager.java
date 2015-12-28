package com.futurice.hereandnow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeaconLocationManager {
    private static final String TAG = "BeaconLocation";
    public static final UUID proximityUUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");

    public static final String BEACON_KEY = "beacon";

    public static final String BEACON_KEY_ID = "id";
    public static final String BEACON_KEY_DISTANCE = "distance";
    public static final String BEACON_KEY_USER_IDENTIFIER = "email";
    public static final String BEACON_KEY_FLOOR = "floor";
    public static final String BEACON_TEMPERATURE_KEY = "temperature";

    private OnLocationUpdateListener mLocationCallback;

    private ArrayList<BeaconCollection> beacons;

    Context context;
    SharedPreferences preferences;

    public BeaconLocationManager(Context c) {
        this.context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        beacons = new ArrayList<BeaconCollection>();
    }

    public void resume() {
        if (beacons == null) {
            return;
        }

        for (BeaconCollection collection : beacons) {
            collection.manager.connect(() -> collection.manager.startRanging(collection.region));
        }
    }

    public void pause() {
        if (beacons == null) {
            return;
        }

        for (BeaconCollection collection : beacons) {
            collection.manager.stopRanging(collection.region);
        }
    }

    public void addBeacon(@NonNull final String id, @NonNull final int floor, Context c) {
        String[] splitted = id.split("-");
        String identifier = String.format("%s-%s", splitted[0], splitted[1]);
        int major = Integer.valueOf(splitted[2]);
        int minor = Integer.valueOf(splitted[3]);

        // Check that no duplicates are added.
        if (beacons != null) {
            for (BeaconCollection collection : beacons) {
                if (collection.beacon.identifier.equals(identifier)) {
                    return;
                }
            }
        }

        FutuBeacon beacon = new FutuBeacon(identifier, major, minor, floor);
        BeaconManager manager = new BeaconManager(c);
        Region beaconRegion = new Region(beacon.identifier, proximityUUID, beacon.major, beacon.minor);
        BeaconCollection collection = new BeaconCollection(beacon, manager, beaconRegion);

        collection.manager.setRangingListener((region, list) -> {
          if (!list.isEmpty()) {
              collection.accuracy = Utils.computeAccuracy(list.get(0));
          }
        });
        beacons.add(collection);
    }

    public void sendLocation() {
        if (beacons == null) {
            return;
        }

        // Send three closest beacon locations to the server.
        Map<FutuBeacon, Double> tempMap = new HashMap<>();
        for (BeaconCollection collection : beacons) {
            if (collection.accuracy > 0) {
                tempMap.put(collection.beacon, collection.accuracy);
            }
        }

        List<Map.Entry<FutuBeacon, Double>> sorted = sortByDistance(tempMap);
        if (sorted.size() >= 3) {
            sendToServer(sorted.get(0).getKey(), sorted.get(0).getValue());
            sendToServer(sorted.get(1).getKey(), sorted.get(1).getValue());
            sendToServer(sorted.get(2).getKey(), sorted.get(2).getValue());
        }
    }

    private static List<Map.Entry<FutuBeacon, Double>> sortByDistance(Map<FutuBeacon, Double> unsorted) {
        List<Map.Entry<FutuBeacon, Double>> list = new LinkedList<>(unsorted.entrySet());
        Collections.sort(list, (left, right) -> left.getValue().compareTo(right.getValue()));
        return list;
    }

    public void onLocation(JSONObject jsonObject) {
        if (mLocationCallback != null) {
            mLocationCallback.onLocationUpdate(jsonObject.toString());
        }
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

    /**
     * Class for storing all information related to beacons.
     */
    private static class BeaconCollection {
        private FutuBeacon beacon;
        private BeaconManager manager;
        private Region region;
        private double accuracy;

        private BeaconCollection(
                @NonNull final FutuBeacon beacon,
                @NonNull final  BeaconManager manager,
                @NonNull final  Region region) {
            this.beacon = beacon;
            this.manager = manager;
            this.region = region;
        }
    }
}
