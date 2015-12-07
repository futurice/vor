package com.futurice.hereandnow.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.Utils.Proximity;
import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.utils.HereAndNowUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A fragment representing a list of Beacons.
 */
public class BeaconFragment extends Fragment {
    private static final String TAG = BeaconFragment.class.getSimpleName();

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 71;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 72;

    public static final String IDENTIFIER_B1 = "futu-b1";
    public static final String IDENTIFIER_B2 = "futu-b2";
    public static final String IDENTIFIER_B3 = "futu-b3";

    public static final String BEACON_KEY_ID = "id";
    public static final String BEACON_KEY_DISTANCE = "distance";
    public static final String BEACON_KEY_USER_IDENTIFIER = "email";
    public static final String BEACON_KEY_FLOOR = "floor";

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

    private Socket mSocket;

    @Bind(R.id.b1LinearLayout) LinearLayout mB1LinearLayout;
    @Bind(R.id.b1AccuracyTextView) TextView mB1AccuracyTextView;
    @Bind(R.id.b1ProximityTextView) TextView mB1ProximityTextView;
    @Bind(R.id.b1Button) Button mB1Button;

    @Bind(R.id.b2LinearLayout) LinearLayout mB2LinearLayout;
    @Bind(R.id.b2AccuracyTextView) TextView mB2AccuracyTextView;
    @Bind(R.id.b2ProximityTextView) TextView mB2ProximityTextView;
    @Bind(R.id.b2Button) Button mB2Button;

    @Bind(R.id.b3LinearLayout) LinearLayout mB3LinearLayout;
    @Bind(R.id.b3AccuracyTextView) TextView mB3AccuracyTextView;
    @Bind(R.id.b3ProximityTextView) TextView mB3ProximityTextView;
    @Bind(R.id.b3Button) Button mB3Button;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BeaconFragment() {
    }

    @NonNull

    public static BeaconFragment newInstance() {
        return new BeaconFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accessCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (HereAndNowUtils.hasPermission(getActivity(), accessCoarseLocation)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
        }

        String accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        if (HereAndNowUtils.hasPermission(getActivity(), accessFineLocation)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        }

        try {
            mSocket = IO.socket(Constants.SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket
                .on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "EVENT_CONNECT"))
                .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .on(Constants.LOCATION_KEY, onLocation)
                .on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "EVENT_DISCONNECT"));
        mSocket.connect();
    }

    @Override
    @NonNull

    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_beacons, container, false);
        ButterKnife.bind(this, rootView);

        mB1Manager = new BeaconManager(getContext());
        mB2Manager = new BeaconManager(getContext());
        mB3Manager = new BeaconManager(getContext());

        keepTrackOfBeacons();

        mB1Manager.setRangingListener((region, beaconList) -> {
            if (!beaconList.isEmpty()) {
                mB1Accuracy = Utils.computeAccuracy(beaconList.get(0));
            }
            setupView(beaconList, mB1LinearLayout, mB1AccuracyTextView, mB1ProximityTextView);
        });

        mB2Manager.setRangingListener((region, beaconList) -> {
            if (!beaconList.isEmpty()) {
                mB2Accuracy = Utils.computeAccuracy(beaconList.get(0));
            }
            setupView(beaconList, mB2LinearLayout, mB2AccuracyTextView, mB2ProximityTextView);
        });

        mB3Manager.setRangingListener((region, beaconList) -> {
            if (!beaconList.isEmpty()) {
                mB3Accuracy = Utils.computeAccuracy(beaconList.get(0));
            }
            setupView(beaconList, mB3LinearLayout, mB3AccuracyTextView, mB3ProximityTextView);
        });

        mB1Button.setOnClickListener(v -> sendToServer(mB1, mB1Accuracy));
        mB2Button.setOnClickListener(v -> sendToServer(mB2, mB2Accuracy));
        mB3Button.setOnClickListener(v -> sendToServer(mB3, mB3Accuracy));

        return rootView;
    }

    private void setupView(
            List<Beacon> beaconList,
            LinearLayout linearLayout,
            TextView accuracyTextView,
            TextView proximityTextView) {

        if (!beaconList.isEmpty()) {
            int lightGreen = ContextCompat.getColor(getContext(), R.color.light_green);
            linearLayout.setBackgroundColor(lightGreen);

            double accuracy = Utils.computeAccuracy(beaconList.get(0));
            accuracyTextView.setText("Accuracy: " + accuracy);

            Proximity proximity = Utils.proximityFromAccuracy(accuracy);
            proximityTextView.setText("Proximity: " + proximity.name());
        } else {
            int lightRed = ContextCompat.getColor(getContext(), R.color.light_red);
            linearLayout.setBackgroundColor(lightRed);
            accuracyTextView.setText("I'm gone :'(");
            proximityTextView.setText("I'm gone :'(");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mB1Manager.connect(() -> mB1Manager.startRanging(mB1Region));
        mB2Manager.connect(() -> mB2Manager.startRanging(mB2Region));
        mB3Manager.connect(() -> mB3Manager.startRanging(mB3Region));
    }

    @Override
    public void onPause() {
        mB1Manager.stopRanging(mB1Region);
        mB2Manager.stopRanging(mB2Region);
        mB3Manager.stopRanging(mB3Region);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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

    private Emitter.Listener onConnectError = args -> getActivity().runOnUiThread(() -> {
        Log.d(TAG, "EVENT_CONNECT_ERROR");
        for (Object arg : args) {
            Log.d(TAG, arg.toString());
        }
        Context context = getActivity().getApplicationContext();
        Toast.makeText(context, R.string.error_connect, Toast.LENGTH_LONG).show();
    });

    private Emitter.Listener onLocation = args -> getActivity().runOnUiThread(() -> {
        Log.d(TAG, "LOCATION RECEIVED");
//        String location = (String) args[0];
        /*
        for (Object o : args) {
            Toast.makeText(getContext(), o.toString(), Toast.LENGTH_LONG).show();
        }
        */
    });

    /**
     * http://developer.estimote.com/android/tutorial/part-2-background-monitoring/
     * There’s no way to keep track of “interim” beacons’ “enters” and “exits”
     * other than creating a single region per each beacon of course.
     */
    private void keepTrackOfBeacons() {
        UUID proximityUUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");

        mB1 = new FutuBeacon(IDENTIFIER_B1, 32095, 19454); // mint
        mB2 = new FutuBeacon(IDENTIFIER_B2, 23964, 38945); // blueberry
        mB3 = new FutuBeacon(IDENTIFIER_B3, 21061, 29133); // ice

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
            jo.put(BEACON_KEY_ID, beacon.identifier + "-" + beacon.major + "-" + beacon.minor);
            jo.put(BEACON_KEY_DISTANCE, accuracy);
            jo.put(BEACON_KEY_USER_IDENTIFIER, "luis.ramalho@futurice.com");
            jo.put(BEACON_KEY_FLOOR, FLOOR_7);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Sending " + jo.toString());
        mSocket.emit(Constants.BEACON_KEY, jo);
    }

    private static class FutuBeacon {
        String identifier;
        Integer major;
        Integer minor;

        private FutuBeacon(String identifier, Integer major, Integer minor) {
            this.identifier = identifier;
            this.major = major;
            this.minor = minor;
        }
    }
}
