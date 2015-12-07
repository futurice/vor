package com.futurice.hereandnow.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futurice.hereandnow.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Floor7Fragment extends Fragment {
    @Bind(R.id.toilet7AM) RelativeLayout mToilet7AM;
    @Bind(R.id.toilet7AW) RelativeLayout mToilet7AW;
    @Bind(R.id.toilet7BM) RelativeLayout mToilet7BM;
    @Bind(R.id.toilet7BW) RelativeLayout mToilet7BW;

    Activity mActivity;

    SharedPreferences mToilet7amSP;
    SharedPreferences mToilet7awSP;
    SharedPreferences mToilet7bmSP;
    SharedPreferences mToilet7bwSP;

    Drawable mFreeBg;
    Drawable mTakenBg;

    OnSharedPreferenceChangeListener toilet7amListener = (sharedPreferences, key) -> {
        mToilet7AM.setBackground(sharedPreferences.getBoolean(key, false) ? mTakenBg : mFreeBg);
    };

    OnSharedPreferenceChangeListener toilet7awListener = (sharedPreferences, key) -> {
        mToilet7AW.setBackground(sharedPreferences.getBoolean(key, false) ? mTakenBg : mFreeBg);
    };

    OnSharedPreferenceChangeListener toilet7bmListener = (sharedPreferences, key) -> {
        mToilet7BM.setBackground(sharedPreferences.getBoolean(key, false) ? mTakenBg : mFreeBg);
    };

    OnSharedPreferenceChangeListener toilet7bwListener = (sharedPreferences, key) -> {
        mToilet7BW.setBackground(sharedPreferences.getBoolean(key, false) ? mTakenBg : mFreeBg);
    };

    public Floor7Fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mToilet7amSP = mActivity.getSharedPreferences("toilet7am", Context.MODE_PRIVATE);
        mToilet7awSP = mActivity.getSharedPreferences("toilet7aw", Context.MODE_PRIVATE);
        mToilet7bmSP = mActivity.getSharedPreferences("toilet7bm", Context.MODE_PRIVATE);
        mToilet7bwSP = mActivity.getSharedPreferences("toilet7bw", Context.MODE_PRIVATE);

        mFreeBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_free_bg);
        mTakenBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_taken_bg);
    }

    @Override
    public void onResume() {
        super.onResume();
        mToilet7amSP.registerOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7awSP.registerOnSharedPreferenceChangeListener(toilet7awListener);
        mToilet7bmSP.registerOnSharedPreferenceChangeListener(toilet7bmListener);
        mToilet7bwSP.registerOnSharedPreferenceChangeListener(toilet7bwListener);

    }

    @Override
    public void onPause() {
        mToilet7amSP.registerOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7awSP.registerOnSharedPreferenceChangeListener(toilet7awListener);
        mToilet7bmSP.registerOnSharedPreferenceChangeListener(toilet7bmListener);
        mToilet7bwSP.registerOnSharedPreferenceChangeListener(toilet7bwListener);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_floor_7_toilet, container, false);
        ButterKnife.bind(this, view);

        updateView();

        return view;
    }

    public void updateView() {
        mToilet7AM.setBackground(mToilet7amSP.getBoolean("reserved", false) ? mTakenBg : mFreeBg);
        mToilet7AW.setBackground(mToilet7awSP.getBoolean("reserved", false) ? mTakenBg : mFreeBg);
        mToilet7BM.setBackground(mToilet7bmSP.getBoolean("reserved", false) ? mTakenBg : mFreeBg);
        mToilet7BW.setBackground(mToilet7bwSP.getBoolean("reserved", false) ? mTakenBg : mFreeBg);
    }
}
