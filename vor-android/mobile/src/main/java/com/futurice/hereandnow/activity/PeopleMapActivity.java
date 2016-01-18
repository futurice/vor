package com.futurice.hereandnow.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.widget.Toast;

import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.fragment.MapActivityFragment;
import com.futurice.hereandnow.fragment.PeopleFragment;
import com.futurice.hereandnow.interfaces.FragmentLifecycle;
import com.futurice.hereandnow.utils.BeaconLocationManager;
import com.futurice.hereandnow.utils.PeopleManager;
import com.futurice.hereandnow.view.CustomViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.futurice.hereandnow.Constants.*;

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

        mBeaconLocationManager = HereAndNowApplication.getBeaconLocationManager();
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

                // If this is the first location received from the user, add it to the manager.
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

                // Set the values for the person.
                person.setFloor(floor);
                person.setLocationInMeters(meterLocationX, meterLocationY);

                Fragment activeFragment = getCurrentFragment();
                if (activeFragment instanceof MapActivityFragment) {
                    ((MapActivityFragment) activeFragment).updateView(person);
                } else if (activeFragment instanceof PeopleFragment) {
                    ((PeopleFragment) activeFragment).updateView();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectionError() {
            Toast.makeText(PeopleMapActivity.this, R.string.error_connect, Toast.LENGTH_SHORT).show();
        }
    };

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
