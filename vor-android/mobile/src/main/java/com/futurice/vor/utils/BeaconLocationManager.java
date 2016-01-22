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

    /**
     * Continue ranging for beacons.
     */
    public void resume() {
        if (beacons == null) {
            return;
        }

        for (BeaconCollection collection : beacons) {
            collection.manager.connect(() -> collection.manager.startRanging(collection.region));
        }
    }

    /**
     * Stop ranging for beacons.
     */
    public void pause() {
        if (beacons == null) {
            return;
        }

        for (BeaconCollection collection : beacons) {
            collection.manager.stopRanging(collection.region);
        }
    }

    /**
     * Add a new beacon to this manager.
     * @param id ID for the beacon.
     * @param floor Floor for the beacon.
     * @param x X coordinate of the beacon.
     * @param y Y coordinate of the beacon.
     * @param c Context.
     */
    public void addBeacon(@NonNull final String id,
                          final int floor,
                          final float x,
                          final float y,
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

    /**
     * Calculate the location for the user.
     * @param sendToServer If true, send the value to the server.
     */
    public void calculateLocation(boolean sendToServer) {
        List<Map.Entry<FutuBeacon, Double>> sorted = getFourClosestBeacons();
        if (sorted == null) {
            return;
        }

        // Check if all beacons are located on the same floor.
        if (!isSameFloorForEntireList(sorted)) {
            return;
        }

        int floor = sorted.get(0).getKey().floor; // Get the floor from the closest beacon.
        float[] position = calculateWeightedAverage(sorted);

        if (sendToServer) {
            sendToServer(position[0], position[1], floor);
        }

        if (mLocationCallback != null) {
            mLocationCallback.onOwnPositionUpdate(position[0], position[1], floor);
        }
    }

    /**
     * Sort the list by distance.
     * @param unsorted Map of unsorted beacons.
     * @return List of sorted beacons.
     */
    private static List<Map.Entry<FutuBeacon, Double>> sortByDistance(Map<FutuBeacon, Double> unsorted) {
        List<Map.Entry<FutuBeacon, Double>> list = new LinkedList<>(unsorted.entrySet());
        Collections.sort(list, (left, right) -> left.getValue().compareTo(right.getValue()));
        return list;
    }

    /**
     * Method for calculating indoor location as described in
     * "Accuracy Enhancements in Indoor Localization with the Weighted Average Technique"
     * by Grigorios G. Anagnostopoulos and Michel Deriaz
     *
     * http://tam.unige.ch/assets/documents/publications/SENSORCOMM2014_Anagnostopoulos.pdf
     * @param sorted List of 4 closest beacons and distance to those beacons.
     */
    private float[] calculateWeightedAverage(List<Map.Entry<FutuBeacon, Double>> sorted) {
        float xNumerator, yNumerator, xDenominator, yDenominator;
        xNumerator = yNumerator = xDenominator = yDenominator = 0f;

        for (Map.Entry<FutuBeacon, Double> beaconEntry : sorted) {
            double distance = beaconEntry.getValue();

            xNumerator += (beaconEntry.getKey().x / distance);
            yNumerator += (beaconEntry.getKey().y / distance);
            xDenominator += (1f / distance);
            yDenominator += (1f / distance);
        }

        return new float[] { (xNumerator / xDenominator), (yNumerator / yDenominator) };
    }

    /**
     * Check that all four closest beacons are located on the same floor
     * @param sorted List of four closest beacons.
     * @return True if they are all located in the same floor, false otherwise.
     */
    private boolean isSameFloorForEntireList(List<Map.Entry<FutuBeacon, Double>> sorted) {
        int first = sorted.get(0).getKey().floor;

        for (Map.Entry<FutuBeacon, Double> beaconEntry : sorted) {
            if (beaconEntry.getKey().floor != first) {
                return false;
            }
        }

        return true;
    }

    /**
     * Retrieve four closest beacons.
     * @return List of four closest beacons.
     */
    private List<Map.Entry<FutuBeacon, Double>> getFourClosestBeacons() {
        if (beacons == null) {
            return null;
        }

        Map<FutuBeacon, Double> tempMap = new HashMap<>();
        for (BeaconCollection collection : beacons) {
            if (collection.accuracy > 0) {
                tempMap.put(collection.beacon, collection.accuracy);
            }
        }

        List<Map.Entry<FutuBeacon, Double>> sorted = sortByDistance(tempMap);
        if (sorted.size() < 4) {
            return null; // Return null if the client doesn't detect at least 4 beacons.
        }

        return sorted.subList(0, 4);
    }

    /**
     * Receive location update from the server and pass it on to the callback method.
     * @param jsonObject new location update as a JSON object.
     */
    public void onLocation(JSONObject jsonObject) {
        if (mLocationCallback != null) {
            mLocationCallback.onLocationUpdate(jsonObject.toString());
        }
    }

    /**
     * Send the clien'ts own location to the server.
     * @param x X coordinate.
     * @param y Y Coordinate.
     * @param floor Current floor.
     */
    private void sendToServer(final float x, final float y, final int floor) {
        JSONObject jo = new JSONObject();
        try {
            jo.put(TYPE_KEY, LOCATION_KEY);
            jo.put(EMAIL_KEY, preferences.getString(SettingsActivity.EMAIL_KEY,
                    context.getString(R.string.pref_my_email_default)));
            jo.put(LOCATION_X_KEY, x);
            jo.put(LOCATION_Y_KEY, y);
            jo.put(FLOOR_KEY, floor);

            Log.d(TAG, "Sending " + jo.toString());
            VorApplication.getSocket().emit(MESSAGE_KEY, jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Static class to store the information of a single beacon.
     */
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

        void onOwnPositionUpdate(float meterLocationX, float meterLocationY, int floor);
    }

    public void setOnLocationUpdateListener(OnLocationUpdateListener listener) {
        mLocationCallback = listener;
    }

    /**
     * Class for storing all information related to a single beacon.
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
