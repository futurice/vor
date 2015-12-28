package com.futurice.hereandnow.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.activity.SettingsActivity;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkInfo";

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    WifiInfo info = wifiManager.getConnectionInfo();
                    if (info.getSSID().equals(Constants.NETWORK_SSID)) {
                        Log.d(TAG, "Connected to correct Wifi.");

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        Boolean serviceEnabled = preferences.getBoolean(SettingsActivity.BACKGROUND_SERVICE_KEY, true);

                        // Start the location service.
                        if (serviceEnabled) {
                            context.startService(new Intent(context, LocationService.class));
                        }
                    }
                }

                if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    Log.d(TAG, "Disconnected from the Wifi.");

                    // Stop the location service if it's running.
                    context.stopService(new Intent(context, LocationService.class));
                }
            }
        }
    }
}
