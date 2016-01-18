package com.futurice.hereandnow.fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.PeopleMapActivity;
import com.futurice.hereandnow.interfaces.FragmentLifecycle;
import com.futurice.hereandnow.utils.BeaconLocationManager;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.hereandnow.utils.PeopleManager;
import com.futurice.hereandnow.view.MapView;
import static com.futurice.hereandnow.Constants.*;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.futurice.cascade.Async.UI;

public class MapActivityFragment extends Fragment implements FragmentLifecycle {
    // Real life dimensions for the map in meters.
    private static final float FLOOR8_WIDTH = 46.1652873563f;
    private static final float FLOOR8_HEIGHT = 39.2249350649f;
    private static final float FLOOR7_WIDTH = 16.2423255f;
    private static final float FLOOR7_HEIGHT = 28.4685258f;
    private static final int FLOOR_NUMBER_OFFSET = 7;

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 71;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 72;

    @Bind(R.id.futumap) MapView mImageView;
    PhotoViewAttacher mAttacher;
    BeaconLocationManager beaconLocationManager;
    PeopleManager peopleManager;

    String mFilter = "";

    SharedPreferences preferences;

    int currentFloor;

    public MapActivityFragment() {}

    public static MapActivityFragment newInstance(int floor) {
        MapActivityFragment fragment = new MapActivityFragment();
        Bundle args = new Bundle();
        args.putInt(FLOOR_KEY, floor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        currentFloor = getArguments().getInt(FLOOR_KEY, 0) + FLOOR_NUMBER_OFFSET;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.map_filter);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        /**
         * The scale for the map resets automatically when the search icon is pressed.
         * All the locations for the people needs to be reset as well.
         */
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                resetImageView();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                resetImageView();
                return true;
            }
        });

        /**
         * Disable zooming while searching for people.
         * Zooming while having the keyboard visible results in a change in the measured
         * map width and height and therefore results in wrong coordinates for the locations.
         */
        searchView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                resetImageView();
                mAttacher.setZoomable(false);
            } else {
                mAttacher.setZoomable(true);
                mImageView.requestFocus(); // Change focus to the map.
            }
        });

        /**
         * Store the search query in the local variable mFilter.
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mFilter = query;
                searchView.clearFocus(); // Remove focus from the search bar.
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFilter = newText;
                return false;
            }
        });
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

        switch (getArguments().getInt(FLOOR_KEY, 0)) {
            case MAP_7TH_FLOOR:
                int map7th = R.drawable.map_general_7th_floor;
                mImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), map7th));
                break;
            case MAP_8TH_FLOOR:
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

        beaconLocationManager = HereAndNowApplication.getBeaconLocationManager();

        peopleManager = new PeopleManager();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mImageView.setOnMapDrawListener(new MapView.OnMapDrawListener() {
            @Override
            public ArrayList<PeopleManager.Person> getPersons() {
                return PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor);
            }

            @Override
            public ArrayList<PeopleManager.Person> getFilteredPersons() {
                if (mFilter.isEmpty()) {
                    return PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor);
                } else {
                    return PeopleMapActivity.mPeopleManager.filterPeopleWithFloor(mFilter, currentFloor);
                }
            }
        });

        /**
         * Search for the closest person related to the click and set its state accordingly.
         */
        mAttacher.setOnViewTapListener((view, x, y) -> {
            float errorMargin = 60f * mAttacher.getScale();
            float marginX, marginY;

            PeopleManager.Person closestPerson = null;
            float closestValue = 0f;

            for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor)) {
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

    public void updateView(PeopleManager.Person recentlyUpdatedPerson) {
        if (recentlyUpdatedPerson.getFloor() != currentFloor) {
            return;
        }

        float location[] = convertToMapLocation(recentlyUpdatedPerson.getMeterLocationX(),
                recentlyUpdatedPerson.getMeterLocationY());
        float scaleFactor = mAttacher.getScale();

        // Set the new location for the person.
        recentlyUpdatedPerson.setLocation((location[0] * scaleFactor), (location[1] * scaleFactor));

        UI.execute(() -> {
            // Invalidate the picture to make it draw the canvas again.
            mImageView.invalidate();
            RectF rect = mAttacher.getDisplayRect();
            for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor)) {
                person.setDisplayedLocation(person.getMapLocationX() + rect.left, person.getMapLocationY()+ rect.top, false);
            }
        });
    }

    @Override
    public void onResumeFragment() {
        mImageView.requestLayout(); // Force re-calculating the dimensions.
        redrawMarkers();
    }

    private class MapScaleListener implements PhotoViewAttacher.OnScaleChangeListener {

        // Redefine the position for the marker after the user scales the image.
        @Override
        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
            mImageView.scaleRadius(scaleFactor);

            for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor)) {
                float oldX = person.getMapLocationX();
                float oldY = person.getMapLocationY();
                person.setLocation((oldX * scaleFactor), (oldY * scaleFactor), false);
            }
        }
    }

    private class MapChangedListener implements PhotoViewAttacher.OnMatrixChangedListener {

        // Calculate a new location on the display for the marker when the user moves the map.
        @Override
        public void onMatrixChanged(RectF rect) {

            for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor)) {
                float newX = person.getMapLocationX() + rect.left;
                float newY = person.getMapLocationY() + rect.top;
                person.setDisplayedLocation(newX, newY, true);
            }
        }
    }

    private float[] convertToMapLocation(float x, float y) {
        float scaleFactorX, scaleFactorY;

        switch (getArguments().getInt(FLOOR_KEY, 0)) {
            case MAP_7TH_FLOOR:
                scaleFactorX = x / FLOOR7_WIDTH;
                scaleFactorY = y / FLOOR7_HEIGHT;
                break;
            case MAP_8TH_FLOOR:
                scaleFactorX = x / FLOOR8_WIDTH;
                scaleFactorY = y / FLOOR8_HEIGHT;
                break;
            default:
                return new float[] { -1f, -1f };
        }

        // Convert to map location.
        float[] result = new float[2];
        result[0] = scaleFactorX * mImageView.getDisplayedWidth();
        result[1] = scaleFactorY * mImageView.getDisplayedHeight();
        return result;
    }

    /**
     * Return the map to its original zoom level
     * and calculate new positions for the people accordingly.
     */
    private void resetImageView() {
        float oldScale = mAttacher.getScale();
        mAttacher.setScale(1f);
        mImageView.resetTextSize();

        UI.execute(() -> {
            // Invalidate the picture to make it draw the canvas again.
            mImageView.invalidate();

            for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor)) {
                person.setLocation(person.getMapLocationX() / oldScale, person.getMapLocationY() / oldScale, false);

                RectF rect = mAttacher.getDisplayRect();
                float newLocationX = person.getMapLocationX() + rect.left;
                float newLocationY = person.getMapLocationY() + rect.top;

                person.setDisplayedLocation(newLocationX, newLocationY, false);
                person.setCurrentLocation(newLocationX, newLocationY);
            }
        });
    }

    /**
     * Redraw all markers and set them straight to their current locations without animations.
     */
    private void redrawMarkers() {
        UI.execute(() -> {
            mImageView.requestLayout(); // Force re-calculating dimensions.
            mImageView.invalidate();

            final float scaleFactor = mAttacher.getScale();
            RectF rect = mAttacher.getDisplayRect();

            for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeopleWithFloor(currentFloor)) {
                float[] screenLocations = convertToMapLocation(person.getMeterLocationX(),
                        person.getMeterLocationY());
                person.setLocation(screenLocations[0] * scaleFactor, screenLocations[1] * scaleFactor);

                float newLocationX = person.getMapLocationX() + rect.left;
                float newLocationY = person.getMapLocationY() + rect.top;

                person.setDisplayedLocation(newLocationX, newLocationY, false);
                person.setCurrentLocation(newLocationX, newLocationY);
            }
        });
    }
}
