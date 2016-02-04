package com.futurice.vor.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;

import com.futurice.vor.R;
import com.futurice.vor.VorApplication;
import com.futurice.vor.fragment.MapActivityFragment;
import com.futurice.vor.fragment.PeopleFragment;
import com.futurice.vor.utils.BeaconLocationManager;
import com.futurice.vor.utils.PeopleManager;
import com.futurice.vor.view.CustomViewPager;
import com.futurice.vor.interfaces.FragmentLifecycle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.futurice.vor.Constants.*;

public class PeopleMapActivity extends BaseActivity {

    BeaconLocationManager mBeaconLocationManager;
    SharedPreferences mPreferences;
    public static PeopleManager mPeopleManager;
    CustomViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mViewPager = (CustomViewPager) findViewById(R.id.peopleMapViewPager);
        setupPeopleMapViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.peopleMapTabs);
        tabLayout.setupWithViewPager(mViewPager);

        mBeaconLocationManager = VorApplication.getBeaconLocationManager();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mPeopleManager = new PeopleManager();

        mViewPager.addOnPageChangeListener(pageChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBeaconLocationManager.setOnLocationUpdateListener(onLocationUpdateListener);
    }

    private void setupPeopleMapViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MapActivityFragment.newInstance(MAP_8TH_FLOOR), getString(R.string.tab_map_8th_floor_title));
        adapter.addFragment(MapActivityFragment.newInstance(MAP_7TH_FLOOR), getString(R.string.tab_map_7th_floor_title));
        adapter.addFragment(PeopleFragment.newInstance(), getString(R.string.title_activity_people));
        viewPager.setAdapter(adapter);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }

    private BeaconLocationManager.OnLocationUpdateListener onLocationUpdateListener = new BeaconLocationManager.OnLocationUpdateListener() {
        @Override
        public void onLocationUpdate(String position) {
            try {
                JSONObject jsonObject = new JSONObject(position);
                final String email = jsonObject.getString(LOCATION_EMAIL_KEY);
                final float meterLocationX = Float.valueOf(jsonObject.getString(LOCATION_X_KEY));
                final float meterLocationY = Float.valueOf(jsonObject.getString(LOCATION_Y_KEY));
                final int floor = Integer.valueOf(jsonObject.getString(LOCATION_FLOOR_KEY));
                final long lastUpdate = jsonObject.getLong(LOCATION_TIMESTAMP_KEY);

                PeopleManager.Person person = getPersonWithEmail(email);
                person.setLastUpdated(lastUpdate);

                updateLocationForPerson(person, meterLocationX, meterLocationY, floor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOwnPositionUpdate(float meterLocationX, float meterLocationY, int floor) {
            String email = mPreferences.getString(SettingsActivity.EMAIL_KEY,
                    getString(R.string.pref_my_email_default));

            PeopleManager.Person person = getPersonWithEmail(email);
            person.setLastUpdated(System.currentTimeMillis());

            updateLocationForPerson(person, meterLocationX, meterLocationY, floor);
        }
    };

    /**
     * Retrieve a person object with a given email. Create a new person object to the person manager
     * if no person exists with the given email.
     * @param email Email of the user.
     * @return Person object.
     */
    private PeopleManager.Person getPersonWithEmail(String email) {
        if (!mPeopleManager.exists(email)) {
            mPeopleManager.addPerson(email);
        }
        PeopleManager.Person person = mPeopleManager.getPerson(email);

        if (person.getColor() == null) {
            if (person.getEmail().equals(mPreferences.getString(SettingsActivity.EMAIL_KEY, ""))) {
                person.setColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
            } else {
                person.setColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
            }
        }

        return person;
    }

    /**
     * Store the given values to the person object and update the current active view.
     * @param person Person object to be updated.
     * @param meterLocationX New X coordinate in meters.
     * @param meterLocationY New Y coordinate in meters.
     * @param floor Current floor for the user.
     */
    private void updateLocationForPerson(@NonNull PeopleManager.Person person,
                                         float meterLocationX,
                                         float meterLocationY,
                                         int floor) {
        person.setFloor(floor);
        person.setLocationInMeters(meterLocationX, meterLocationY);

        Fragment activeFragment = getCurrentFragment();
        if (activeFragment instanceof MapActivityFragment) {
            ((MapActivityFragment) activeFragment).updateView(person);
        } else if (activeFragment instanceof PeopleFragment) {
            ((PeopleFragment) activeFragment).updateView();
        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        int currentPosition = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Empty function
        }

        @Override
        public void onPageSelected(int position) {
            SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
            FragmentLifecycle fragmentToShow = (FragmentLifecycle)adapter.getItem(position);
            fragmentToShow.onResumeFragment();

            FragmentLifecycle fragmentToHide = (FragmentLifecycle)adapter.getItem(currentPosition);
            fragmentToHide.onPauseFragment();

            currentPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Empty function
        }
    };


    private Fragment getCurrentFragment() {
        SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
        int index = mViewPager.getCurrentItem();

        switch (index) {
            case MAP_8TH_FLOOR:
                return adapter.getItem(MAP_8TH_FLOOR);
            case MAP_7TH_FLOOR:
                return adapter.getItem(MAP_7TH_FLOOR);
            case PEOPLE_TAB_INDEX:
                return adapter.getItem(PEOPLE_TAB_INDEX);
            default:
                return null;
        }
    }
}
