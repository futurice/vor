package com.futurice.hereandnow.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import com.futurice.hereandnow.pojo.PersonNearby;
import com.futurice.hereandnow.utils.BeaconLocationManager;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.hereandnow.utils.PeopleManager;
import com.futurice.hereandnow.view.CustomViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.futurice.hereandnow.Constants.MAP_7TH_FLOOR;
import static com.futurice.hereandnow.Constants.MAP_8TH_FLOOR;
import static com.futurice.hereandnow.Constants.PEOPLE_TAB_INDEX;

public class PeopleMapActivity extends BaseActivity {

    BeaconLocationManager mBeaconLocationManager;
    SharedPreferences mPreferences;
    PeopleManager mPeopleManager;
    CustomViewPager mViewPager;

    private float mOwnX, mOwnY;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBeaconLocationManager.setOnLocationUpdateListener(new BeaconLocationManager.OnLocationUpdateListener() {
            @Override
            public void onLocationUpdate(String position) {
                try {
                    JSONObject jsonObject = new JSONObject(position);
                    String email = jsonObject.getString("email");
                    int index = mViewPager.getCurrentItem();
                    if (index == MAP_8TH_FLOOR) {
                        // TODO 8th floor map
                    } else if (index == PEOPLE_TAB_INDEX) {
                        String userEmail = mPreferences.getString(SettingsActivity.EMAIL_KEY, "");
                        if (!mPeopleManager.exists(email) && !email.equals(userEmail)) {
                            mPeopleManager.addPerson(email);
                        }

                        if (email.equals((mPreferences.getString(SettingsActivity.EMAIL_KEY, "")))) {
                            mOwnX = Float.valueOf(jsonObject.getString("x"));
                            mOwnY = Float.valueOf(jsonObject.getString("y"));
                        } else {
                            PeopleManager.Person selectedPerson = mPeopleManager.getPerson(email);
                            selectedPerson.setLocation(Float.valueOf(jsonObject.getString("x")),
                                    Float.valueOf(jsonObject.getString("y")));
                        }

                        updateNameList();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionError() {
                Toast.makeText(PeopleMapActivity.this, R.string.error_connect, Toast.LENGTH_SHORT).show();
            }

            private void updateNameList() {
                // Invalid values for the client's own location so do not update the view.
                if (mOwnX <= 0 || mOwnY <= 0) {
                    return;
                }

                ArrayList<PersonNearby> listValues = new ArrayList<>();

                for (PeopleManager.Person person : mPeopleManager.getPeople()) {
                    float personX = person.getMapLocationX();
                    float personY = person.getMapLocationY();

                    double distance = Math.sqrt(Math.pow(personX - mOwnX, 2) + Math.pow(personY - mOwnY, 2));
                    PersonNearby personNearby = new PersonNearby(HereAndNowUtils.getName(person.getEmail()),
                            distance);
                    listValues.add(personNearby);
                }

                // Sort the values.
                Collections.sort(listValues, new PersonNearby.PersonComparator());

                // Update the view.
                SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
                PeopleFragment fragment = (PeopleFragment) adapter.getItem(PEOPLE_TAB_INDEX);
                fragment.updateView(listValues);
            }
        });
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
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
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
}
