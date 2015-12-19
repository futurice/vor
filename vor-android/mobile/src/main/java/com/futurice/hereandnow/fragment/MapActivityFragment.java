package com.futurice.hereandnow.fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.SettingsActivity;
import com.futurice.hereandnow.utils.BeaconLocationManager;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.hereandnow.utils.PeopleManager;
import com.futurice.hereandnow.view.MapView;
import com.futurice.hereandnow.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MapActivityFragment extends Fragment {
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
    PeopleManager peopleManager;

    SharedPreferences preferences;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnMatrixChangeListener(new MapChangedListener());
        mAttacher.setOnScaleChangeListener(new MapScaleListener());

        beaconLocationManager = new BeaconLocationManager(getContext());
        beaconLocationManager.initialize();

        peopleManager = new PeopleManager();

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
                    String email = jsonObject.getString("email");

                    // Check if this is a new person not initialized yet.
                    if (!peopleManager.exists(email)) {
                        peopleManager.addPerson(email);
                    }
                    PeopleManager.Person selectedPerson = peopleManager.getPerson(email);
                    if (selectedPerson.getColor() == null) {
                        if (selectedPerson.getEmail().equals(preferences.getString(SettingsActivity.EMAIL_KEY, ""))) {
                            selectedPerson.setColor(ContextCompat.getColor(getContext(), R.color.orange));
                        } else {
                            selectedPerson.setColor(ContextCompat.getColor(getContext(), R.color.green));
                        }
                    }

                    // Calculate a new location for the person.
                    float location[] = convertToMapLocation(
                            Float.valueOf(jsonObject.getString("x")),
                            Float.valueOf(jsonObject.getString("y")));

                    float scaleFactor = mAttacher.getScale();
                    RectF rect = mAttacher.getDisplayRect();

                    // Set the new location for the person.
                    selectedPerson.setLocation((location[0] * scaleFactor), (location[1] * scaleFactor));

                    getActivity().runOnUiThread(() -> {
                        // Invalidate the picture to make it draw the canvas again.
                        mImageView.invalidate();
                        for (PeopleManager.Person person : peopleManager.getPeople()) {
                            person.setDisplayedLocation(person.getMapLocationX() + rect.left, person.getMapLocationY()+ rect.top, false);
                        }
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

        mImageView.setOnMapDrawListener(peopleManager::getPeople);

        mAttacher.setOnViewTapListener((view, x, y) -> {
            float errorMargin = 60f * mAttacher.getScale();
            float marginX, marginY;

            PeopleManager.Person closestPerson = null;
            float closestValue = 0f;

            for (PeopleManager.Person person : peopleManager.getPeople()) {
                marginX = Math.abs(x - person.getCurrentLocationX());
                marginY = Math.abs(y - person.getLocationOnScreenY());

                if (marginX < errorMargin && marginY < errorMargin) {
                    if (closestPerson == null) {
                        closestPerson = person;
                        closestValue = marginX + marginY;
                    } else {
                        if ((marginX + marginY) < closestValue) {
                            closestPerson = person;
                            closestValue = marginX + marginY;
                        }
                    }
                }
            }

            if (closestPerson != null) {
                closestPerson.setClicked(!closestPerson.isClicked());
            }
        });
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
            mImageView.scaleRadius(scaleFactor);

            for (PeopleManager.Person person : peopleManager.getPeople()) {
                float oldX = person.getMapLocationX();
                float oldY = person.getMapLocationY();
                person.setLocation((oldX * scaleFactor), (oldY * scaleFactor));
            }
        }
    }

    private class MapChangedListener implements PhotoViewAttacher.OnMatrixChangedListener {

        // Calculate a new location on the display for the marker when the user moves the map.
        @Override
        public void onMatrixChanged(RectF rect) {

            for (PeopleManager.Person person : peopleManager.getPeople()) {
                float newX = person.getMapLocationX() + rect.left;
                float newY = person.getMapLocationY() + rect.top;
                person.setDisplayedLocation(newX, newY, true);
            }
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
        result[0] = scaleFactorX * mImageView.getDisplayedWidth();
        result[1] = scaleFactorY * mImageView.getDisplayedHeight();
        return result;
    }
}
