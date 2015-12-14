package com.futurice.hereandnow.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.fragment.HappeningNowFragment;
import com.futurice.hereandnow.fragment.MyCardsFragment;
import com.futurice.hereandnow.fragment.TrendingFragment;

public class CardsActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
