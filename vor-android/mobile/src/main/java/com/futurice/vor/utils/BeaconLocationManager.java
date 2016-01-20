package com.futurice.vor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import static com.futurice.vor.Constants.*;
import com.futurice.vor.R;
import com.futurice.vor.VorApplication;
import com.futurice.vor.activity.SettingsActivity;

import org.json.JSONArray;
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

    private OnLocationUpdateListener mLocationCallback;

    private ArrayList<BeaconCollection> beacons;

    Context context;
    SharedPreferences preferences;

    public BeaconLocationManager(Context c) {
        this.context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        beacons = new ArrayList<>();
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

    public void addBeacon(@NonNull final String id,
                          @NonNull final int floor,
                          @NonNull final float x,
                          @NonNull final float y,
                          Context c) {
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

        FutuBeacon beacon = new FutuBeacon(identifier, major, minor, floor, x, y);
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
            sendToServer(sorted.subList(0, 3));
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

    private void sendToServer(List<Map.Entry<FutuBeacon, Double>> sorted) {
        JSONObject jo = new JSONObject();
        try {
            jo.put(TYPE_KEY, BEACON_KEY);
            jo.put(EMAIL_KEY, preferences.getString(SettingsActivity.EMAIL_KEY,
                    context.getString(R.string.pref_my_email_default)));

            JSONArray beaconsArray = new JSONArray();
            for (Map.Entry<FutuBeacon, Double> beaconEntry : sorted) {
                JSONObject beaconObject = new JSONObject();
                FutuBeacon beacon = beaconEntry.getKey();

                beaconObject.put(ID_KEY, beacon.identifier + "-" + beacon.major + "-" + beacon.minor);
                beaconObject.put(DISTANCE_KEY, beaconEntry.getValue());
                beaconObject.put(FLOOR_KEY, beacon.floor);
                beaconObject.put(TEMPERATURE_KEY, 19.4f);

                beaconsArray.put(beaconObject);
            }

            jo.put("beacons", beaconsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Sending " + jo.toString());
        VorApplication.getSocket().emit(MESSAGE_KEY, jo);
    }

    private static class FutuBeacon {
        String identifier;
        Integer major;
        Integer minor;
        Integer floor;
        Float x;
        Float y;

        private FutuBeacon(String identifier, Integer major, Integer minor, Integer floor, Float x, Float y) {
            this.identifier = identifier;
            this.major = major;
            this.minor = minor;
            this.floor = floor;
            this.x = x;
            this.y = y;
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
