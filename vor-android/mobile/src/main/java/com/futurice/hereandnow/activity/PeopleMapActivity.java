package com.futurice.hereandnow.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.fragment.MapActivityFragment;
import com.futurice.hereandnow.fragment.PeopleFragment;
import com.futurice.hereandnow.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

import static com.futurice.hereandnow.Constants.MAP_7TH_FLOOR;
import static com.futurice.hereandnow.Constants.MAP_8TH_FLOOR;

public class PeopleMapActivity extends BaseActivity {

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

        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.peopleMapViewPager);
        setupPeopleMapViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.peopleMapTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupPeopleMapViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MapActivityFragment.newInstance(MAP_8TH_FLOOR), getString(R.string.tab_map_8th_floor_title));
        adapter.addFragment(MapActivityFragment.newInstance(MAP_7TH_FLOOR), getString(R.string.tab_map_7th_floor_title));
        adapter.addFragment(new PeopleFragment(), getString(R.string.title_activity_people));
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
            if (position == 0) {
                position = 1;
            } else if (position == 1) {
                position = 0;
            }
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                position = 1;
            } else if (position == 1) {
                position = 0;
            }
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}
