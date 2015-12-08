package com.futurice.hereandnow.adapter;


import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;

public class CustomPagerAdapter extends PagerAdapter {

    @Override
    public int getCount() {
        //Do we need this to work? Should this be abstract?
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull final View view,
                                    @NonNull final Object object) {
        //FIXME Do we need this to work?
        return false;
    }
}
