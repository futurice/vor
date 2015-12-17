package com.futurice.hereandnow.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.fragment.Floor7Fragment;
import com.futurice.hereandnow.fragment.Floor8Fragment;
import com.futurice.hereandnow.fragment.ToiletMapFragment;
import com.futurice.hereandnow.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

public class ToiletActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toilet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.toiletViewPager);
        setupToiletViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.toiletTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToiletViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Floor7Fragment(), "7th Floor");
        adapter.addFragment(new Floor8Fragment(), "8th Floor");
        adapter.addFragment(new ToiletMapFragment(), "Map");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
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
