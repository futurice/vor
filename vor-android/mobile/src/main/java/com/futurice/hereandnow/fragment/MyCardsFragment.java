package com.futurice.hereandnow.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.hereandnow.R;

public class MyCardsFragment extends TrendingFragment {

    @NonNull

    public static MyCardsFragment newInstance() {
        return new MyCardsFragment();
    }

    @Override
    @NonNull
    protected View setupView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container) {
        // TODO: Implement my cards stuff
        return inflater.inflate(R.layout.fragment_happening_now, container, false);
    }
}
