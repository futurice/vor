package com.futurice.hereandnow;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.futurice.cascade.AsyncBuilder;
import com.futurice.hereandnow.services.LocationService;
import com.spacetimenetworks.scampiandroidlib.ScampiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Main application
 */
public class HereAndNowApplication extends Application implements ScampiService.StateChangeCallback {

    public static String TAG = HereAndNowApplication.class.getCanonicalName();
    private static Context sContext;

    private static Socket mSocket;

    /**
     * Reference to the service.
     */
    @Nullable

//    ScampiService service;

    public static Context getStaticContext() {
        return HereAndNowApplication.sContext;
    }

    public static void stopServiceDelayed(@NonNull final Context context) {
        final HereAndNowApplication app = ((HereAndNowApplication) (context.getApplicationContext()));
//        if (app.service != null) {
//            app.service.stop(Constants.DELAY_IN_MINUTES, TimeUnit.MINUTES);
//        }
    }

    public static void stopServiceNow(@NonNull final Context context) {
//        final HereAndNowApplication app = ((HereAndNowApplication) (sContext.getApplicationContext()));
//        if (app.service != null) {
//            app.service.stop();
//            Async.exitWithErrorCode(TAG, "User shutdown", null);
//            new Thread(() -> {
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Interrupted app exit sleep: ", e);
//                }
//                Async.exitWithErrorCode(TAG, "User shutdown", null);
//            });
//        }
    }

    public static void startServiceIf(@NonNull final Context context) {
//        final HereAndNowApplication app = ((HereAndNowApplication) (sContext.getApplicationContext()));
//
//        if (app.service != null) {
//            app.service.start();
//        } else {
//            app.initConnection();
//        }
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
            mSocket = IO.socket(Constants.SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket
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
        mSocket.connect();

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
                case Constants.SAUNA_KEY: // Example
                    testSaunaNotification(jsonObject.getString("status"));
                    break;
                case Constants.POOL_KEY:
                    String message;
                    try {
                        message = jsonObject.getString(Constants.MESSAGE_KEY);
                    } catch (JSONException e) {
                        message = "We need players!";
                    }
                    savePoolToSharedPreferences(message);
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

    private void testSaunaNotification(String status) {
        Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.tribe_icon)
                .setContentTitle("Sauna")
                .setContentText("Sauna is " + status);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(001, mBuilder.build());
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

    @Override
    public void stateChanged(@NonNull final ScampiService.RouterState routerState) {
//        Log.d(TAG, "stateChanged");
    }

//    private void initConnection() {
//        // Setup the service binding
//        super.startService(new Intent(this, ScampiService.class));
//        // Connection used to get a callback once the service is connected.
//        this.bindService(new Intent(this, ScampiService.class),
//                this.getServiceConnection(), Context.BIND_AUTO_CREATE);
//    }

//    private void startRouter() {
//        // Build a configuration for the Service. The configuration
//        // defines all user visible things such as notification tray icon
//        // and texts, as well as things like the home directory to store the
//        // router files and the configuration to use for the router. There
//        // are defaults for all the settings.
//        final ServiceConfig scampiServiceConfig =
//                ServiceConfig.builder(HereAndNowApplication.this)
//                        .logToStdout()    // Causes the router to log to stdout
//                        .debugLogLevel()  // Sets log level to debug for the router
////Use only if works only with a local server        .configFileAsset("star.conf")
//                        .notifyIntent(new Intent(this, DrawerActivity.class))
//                        .notifyContentText(HereAndNowApplication.getApplicationName())
//                        .notifyIcon(R.drawable.ic_launcher_transparent)
//                        .build();
//
//        // startScan() either starts up the router if it's not running, or does
//        // nothing otherwise.
//        if (service != null) {
//            this.service.start(scampiServiceConfig);
//        } else {
//            Log.i(TAG, "Can not startScan SCAMPI service- not connected");
//        }
//    }

//    @NonNull
//    private ServiceConnection getServiceConnection() {
//        return new ServiceConnection() {
//
//            @Override
//            public void onServiceConnected(@NonNull final ComponentName className, @NonNull final IBinder service) {
//                Log.d(TAG, "onServiceConnected");
//
//                // Get reference to the IScampiService
//                final ScampiService scampiService = ((ScampiService.ScampiBinder) service).getService();
//                HereAndNowApplication.this.service = scampiService;
//
//                // Add callbacks to the service
//                scampiService.addStateChangeCallback(HereAndNowApplication.this);
//                startRouter();
//            }
//
//            @Override
//            public void onServiceDisconnected(@NonNull final ComponentName arg0) {
//                Log.d(TAG, "onServiceDisconnected");
//                removeCallback();
//            }
//        };
//    }

    public void removeCallback() {
//        if (HereAndNowApplication.this.service != null) {
//            HereAndNowApplication.this.service.removeStateChangeCallback(HereAndNowApplication.this);
//            HereAndNowApplication.this.service = null;
//        }
    }

    public static Socket getSocket() {
        return mSocket;
    }
}
