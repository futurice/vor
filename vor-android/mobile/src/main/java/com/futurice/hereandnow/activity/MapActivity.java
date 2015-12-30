package com.futurice.hereandnow.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import static com.futurice.hereandnow.Constants.*;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.fragment.MapActivityFragment;
import com.futurice.hereandnow.view.CustomViewPager;

public class MapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.container);
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case MAP_7TH_FLOOR:
                    fragment = MapActivityFragment.newInstance(MAP_7TH_FLOOR);
                    break;
                case MAP_8TH_FLOOR:
                    fragment = MapActivityFragment.newInstance(MAP_8TH_FLOOR);
                    break;
                default:
                    fragment = null;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return MAPS_NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            Resources resources = getApplicationContext().getResources();
            final CharSequence title;
            switch (position) {
                case MAP_7TH_FLOOR:
                    title = resources.getString(R.string.tab_map_7th_floor_title);
                    break;
                case MAP_8TH_FLOOR:
                    title = resources.getString(R.string.tab_map_8th_floor_title);
                    break;
                default:
                    title = "";
            }
            return title;
        }
    }
}
