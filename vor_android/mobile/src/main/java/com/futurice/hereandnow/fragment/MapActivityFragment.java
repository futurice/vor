package com.futurice.hereandnow.fragment;

import android.Manifest;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.utils.BeaconLocationManager;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.hereandnow.view.MapView;
import com.futurice.hereandnow.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MapActivityFragment extends Fragment {
    private static final int DELAY = 8000;

    //TODO Measure real dimensions for the 8th floor.
    // Real life dimensions for the map in meters.
    private static final float FLOOR8_WIDTH = 20f;
    private static final float FLOOR8_HEIGHT = 20f;
    private static final float FLOOR7_WIDTH = 16.2423255f;
    private static final float FLOOR7_HEIGHT = 28.4685258f;

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 71;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 72;

    @Bind(R.id.futumap) MapView mImageView;
    PhotoViewAttacher mAttacher;
    BeaconLocationManager beaconLocationManager;

    public MapActivityFragment() {}

    public static MapActivityFragment newInstance(int floor) {
        MapActivityFragment activity = new MapActivityFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.FLOOR_KEY, floor);
        activity.setArguments(args);
        return activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);

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

        switch (getArguments().getInt(Constants.FLOOR_KEY, 0)) {
            case Constants.MAP_7TH_FLOOR:
                int map7th = R.drawable.map_general_7th_floor;
                mImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), map7th));
                break;
            case Constants.MAP_8TH_FLOOR:
                int map8th = R.drawable.map_general_8th_floor;
                mImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), map8th));
                break;
            default:
                break;
        }

        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnMatrixChangeListener(new MapChangedListener());
        mAttacher.setOnScaleChangeListener(new MapScaleListener());

        beaconLocationManager = new BeaconLocationManager(getContext());
        beaconLocationManager.initialize();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        beaconLocationManager.resume();
        beaconLocationManager.setOnLocationUpdateListener(new BeaconLocationManager.OnLocationUpdateListener() {
            @Override
            public void onLocationUpdate(String position) {
                if (getActivity() == null) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(position);
                    String email = jsonObject.getString("email"); //TODO update only with the correct email.
                    float location[] = convertToMapLocation(
                            Float.valueOf(jsonObject.getString("x")),
                            Float.valueOf(jsonObject.getString("y")));

                    getActivity().runOnUiThread(() -> {
                        // Invalidate the picture to make it draw the canvas again.
                        mImageView.invalidate();
                        float scaleFactor = mAttacher.getScale();
                        mImageView.setLocation((location[0] * scaleFactor), (location[1] * scaleFactor));
                        RectF rect = mAttacher.getDisplayRect();
                        mImageView.setDisplayedLocation((location[0] * scaleFactor) + rect.left, (location[1] * scaleFactor) + rect.top, false);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionError() {
                if (getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.error_connect, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Set the handler for updating the location.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                new LocationUpdateTask().execute();
                handler.postDelayed(this, DELAY);
            }

        }, DELAY);
    }

    @Override
    public void onPause() {
        beaconLocationManager.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconLocationManager.destroy();
    }

    private class MapScaleListener implements PhotoViewAttacher.OnScaleChangeListener {

        // Redefine the position for the marker after the user scales the image.
        @Override
        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
            float oldX = mImageView.getMapLocationX();
            float oldY = mImageView.getMapLocationY();
            mImageView.setLocation((oldX * scaleFactor), (oldY * scaleFactor));
            mImageView.scaleRadius(scaleFactor);
        }
    }

    private class MapChangedListener implements PhotoViewAttacher.OnMatrixChangedListener {

        // Calculate a new location on the display for the marker when the user moves the map.
        @Override
        public void onMatrixChanged(RectF rect) {
            float newX = mImageView.getMapLocationX() + rect.left;
            float newY = mImageView.getMapLocationY() + rect.top;
            mImageView.setDisplayedLocation(newX, newY, true);
        }
    }

    private class LocationUpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            beaconLocationManager.sendLocation();
            return null;
        }
    }

    private float[] convertToMapLocation(float x, float y) {
        float scaleFactorX, scaleFactorY;

        switch (getArguments().getInt(Constants.FLOOR_KEY, 0)) {
            case Constants.MAP_7TH_FLOOR:
                scaleFactorX = x / FLOOR7_WIDTH;
                scaleFactorY = y / FLOOR7_HEIGHT;
                break;
            case Constants.MAP_8TH_FLOOR:
                scaleFactorX = x / FLOOR8_WIDTH;
                scaleFactorY = y / FLOOR8_HEIGHT;
                break;
            default:
                return new float[] {-1f, -1f};
        }

        // Convert to map location.
        float[] result = new float[2];
        result[0] = scaleFactorX * mImageView.getWidth();
        result[1] = scaleFactorY * mImageView.getHeight();
        return result;
    }
}
