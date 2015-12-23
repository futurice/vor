package com.futurice.hereandnow;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.futurice.cascade.AsyncBuilder;
import com.futurice.hereandnow.services.LocationService;
import com.futurice.hereandnow.utils.SharedPreferencesManager;

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
                .on(Constants.MESSAGE_KEY, args -> {
                    SharedPreferencesManager.saveToSharedPreferences((JSONObject) args[0], this);
                })
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

    public static Socket getSocket() {
        return sSocket;
    }
}
