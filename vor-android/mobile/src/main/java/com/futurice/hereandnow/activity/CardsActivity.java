package com.futurice.hereandnow.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.fragment.HappeningNowFragment;
import com.futurice.hereandnow.fragment.MyCardsFragment;
import com.futurice.hereandnow.fragment.TrendingFragment;

public class CardsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cards);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
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
                case Constants.CARDS_HAPPENING_NOW:
                    fragment = HappeningNowFragment.newInstance();
                    break;
                case Constants.CARDS_TRENDING:
                    fragment = TrendingFragment.newInstance();
                    break;
                case Constants.CARDS_MY_CARDS:
                    fragment = MyCardsFragment.newInstance();
                    break;
                default:
                    fragment = null;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return Constants.CARDS_NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            Resources resources = getApplicationContext().getResources();
            final CharSequence title;
            switch (position) {
                case Constants.CARDS_HAPPENING_NOW:
                    title = resources.getString(R.string.tab_now_title);
                    break;
                case Constants.CARDS_TRENDING:
                    title = resources.getString(R.string.tab_trending_title);
                    break;
                case Constants.CARDS_MY_CARDS:
                    title = resources.getString(R.string.tab_my_cards_title);
                    break;
                default:
                    title = "";
            }

            return title;
        }
    }
}
