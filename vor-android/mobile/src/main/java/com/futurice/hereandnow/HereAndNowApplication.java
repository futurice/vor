package com.futurice.hereandnow;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.futurice.cascade.AsyncBuilder;
import com.futurice.hereandnow.services.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Main application
 */
public class HereAndNowApplication extends Application {
    public static String TAG = HereAndNowApplication.class.getCanonicalName();

    private static Context sContext;
    private static Socket sSocket;

    /**
     * Reference to the service.
     */
    @Nullable

    public static Context getStaticContext() {
        return HereAndNowApplication.sContext;
    }

    public static String getApplicationName() {
        return sContext.getString(sContext.getApplicationInfo().labelRes);
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        // LeakCanary.install(this);

        sContext = getApplicationContext();

        new AsyncBuilder(this)
                .setStrictMode(false) //TODO Fix strict mode on startup for performance and testing
                .build();

        try {
            sSocket = IO.socket(Constants.SERVER_URL);
            IO.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        sSocket
                .on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "EVENT_CONNECT"))
                .on(Socket.EVENT_CONNECT_ERROR, args -> {
                    Log.d(TAG, "EVENT_CONNECT_ERROR");
                    for (Object arg : args) {
                        Log.d(TAG, arg.toString());
                    }
                })
                .on(Constants.LOCATION_KEY, args -> Log.d(TAG, "LOCATION RECEIVED"))
                .on(Constants.MESSAGE_KEY, args -> updateToSharedPreferences((JSONObject) args[0]))
                .on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "EVENT_DISCONNECT"));
        sSocket.connect();

        // Start the service for updating location if connected to the correct network.
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        //TODO Display a notification if the user is not connected to the right network.
        if (wifiInfo.getSSID().equals(Constants.NETWORK_SSID)) {
            startService(new Intent(this, LocationService.class));
        }
    }

    public void updateToSharedPreferences(JSONObject jsonObject) {
        try {
            switch (jsonObject.getString(Constants.TYPE_KEY)) {
                case Constants.TOILET_KEY:
                    saveToiletToSharedPreferences(jsonObject);
                    break;
                case Constants.POOL_KEY:
                    savePoolToSharedPreferences(jsonObject.getString(Constants.IMAGE_KEY));
                    break;
                case Constants.FOOD_KEY:
                    saveFoodToSharedPreferences(jsonObject.getString(Constants.IMAGE_KEY));
                    break;
                case Constants.TEST_KEY: // Test
                    saveTestToSharedPreferences(jsonObject.getString("message"));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveToiletToSharedPreferences(JSONObject jsonObject) throws JSONException {
        String toiletId = Constants.TOILET_KEY + jsonObject.getString(Constants.ID_KEY);
        SharedPreferences toilets = getSharedPreferences(toiletId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = toilets.edit();
        editor.putBoolean(Constants.RESERVED_KEY, jsonObject.getBoolean(Constants.RESERVED_KEY));
        editor.putInt(Constants.METHANE_KEY, jsonObject.getInt(Constants.METHANE_KEY));
        editor.apply();
    }

    private void saveTestToSharedPreferences(String message) {
        SharedPreferences testSP = getSharedPreferences(Constants.TEST_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = testSP.edit();
        editor.putString(Constants.MESSAGE_KEY, message);
        editor.apply();
    }

    private void savePoolToSharedPreferences(String file) {
        SharedPreferences poolSP = getSharedPreferences(Constants.POOL_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = poolSP.edit();
        editor.putString(Constants.IMAGE_KEY, file);
        editor.apply();
    }

    private void saveFoodToSharedPreferences(String file) {
        SharedPreferences foodSP = getSharedPreferences(Constants.FOOD_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = foodSP.edit();
        editor.putString(Constants.IMAGE_KEY, file);
        editor.apply();
    }

    public static Socket getSocket() {
        return sSocket;
    }
}
