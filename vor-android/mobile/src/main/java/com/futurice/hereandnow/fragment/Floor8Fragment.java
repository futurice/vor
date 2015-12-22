package com.futurice.hereandnow.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Floor8Fragment extends Fragment {
    @Bind(R.id.toilet8AM) RelativeLayout mToilet8AM;
    @Bind(R.id.toilet8AW) RelativeLayout mToilet8AW;
    @Bind(R.id.toilet8BM) RelativeLayout mToilet8BM;
    @Bind(R.id.toilet8BW) RelativeLayout mToilet8BW;
    @Bind(R.id.toilet8CM) RelativeLayout mToilet8CM;
    @Bind(R.id.toilet8CW) RelativeLayout mToilet8CW;

    Activity mActivity;

    SharedPreferences mToilet8amSP;
    SharedPreferences mToilet8awSP;
    SharedPreferences mToilet8bmSP;
    SharedPreferences mToilet8bwSP;
    SharedPreferences mToilet8cmSP;
    SharedPreferences mToilet8cwSP;

    OnSharedPreferenceChangeListener toilet8amListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet8AM);
    };

    OnSharedPreferenceChangeListener toilet8awListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet8AW);
    };

    OnSharedPreferenceChangeListener toilet8bmListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet8BM);
    };

    OnSharedPreferenceChangeListener toilet8bwListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet8BW);
    };

    OnSharedPreferenceChangeListener toilet8cmListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet8CM);
    };

    OnSharedPreferenceChangeListener toilet8cwListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet8CW);
    };

    public Floor8Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mToilet8amSP = mActivity.getSharedPreferences("toilet8am", Context.MODE_PRIVATE);
        mToilet8awSP = mActivity.getSharedPreferences("toilet8aw", Context.MODE_PRIVATE);
        mToilet8bmSP = mActivity.getSharedPreferences("toilet8bm", Context.MODE_PRIVATE);
        mToilet8bwSP = mActivity.getSharedPreferences("toilet8bw", Context.MODE_PRIVATE);
        mToilet8cmSP = mActivity.getSharedPreferences("toilet8cm", Context.MODE_PRIVATE);
        mToilet8cwSP = mActivity.getSharedPreferences("toilet8cw", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_floor_8_toilet, container, false);
        ButterKnife.bind(this, view);

        updateView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mToilet8amSP.registerOnSharedPreferenceChangeListener(toilet8amListener);
        mToilet8awSP.registerOnSharedPreferenceChangeListener(toilet8awListener);
        mToilet8bmSP.registerOnSharedPreferenceChangeListener(toilet8bmListener);
        mToilet8bwSP.registerOnSharedPreferenceChangeListener(toilet8bwListener);
        mToilet8cmSP.registerOnSharedPreferenceChangeListener(toilet8cmListener);
        mToilet8cwSP.registerOnSharedPreferenceChangeListener(toilet8cwListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        mToilet8amSP.unregisterOnSharedPreferenceChangeListener(toilet8amListener);
        mToilet8awSP.unregisterOnSharedPreferenceChangeListener(toilet8awListener);
        mToilet8bmSP.unregisterOnSharedPreferenceChangeListener(toilet8bmListener);
        mToilet8bwSP.unregisterOnSharedPreferenceChangeListener(toilet8bwListener);
        mToilet8cmSP.unregisterOnSharedPreferenceChangeListener(toilet8cmListener);
        mToilet8cwSP.unregisterOnSharedPreferenceChangeListener(toilet8cwListener);
    }

    private void updateToiletView(SharedPreferences sharedPreferences, String key, View view) {
        switch (key) {
            case Constants.RESERVED_KEY:
                updateToiletBackground(view, sharedPreferences.getBoolean(key, false));
                break;
            case Constants.METHANE_KEY:
                break;
            default:
                break;
        }
    }

    private void updateView() {
        updateToiletBackground(mToilet8AM, mToilet8amSP.getBoolean(Constants.RESERVED_KEY, false));
        updateToiletBackground(mToilet8AW, mToilet8awSP.getBoolean(Constants.RESERVED_KEY, false));
        updateToiletBackground(mToilet8BM, mToilet8bmSP.getBoolean(Constants.RESERVED_KEY, false));
        updateToiletBackground(mToilet8BW, mToilet8bwSP.getBoolean(Constants.RESERVED_KEY, false));
        updateToiletBackground(mToilet8CM, mToilet8cmSP.getBoolean(Constants.RESERVED_KEY, false));
        updateToiletBackground(mToilet8CW, mToilet8cwSP.getBoolean(Constants.RESERVED_KEY, false));
    }

    public void updateToiletBackground(View view, Boolean reserved) {
        Drawable freeBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_free_bg);
        Drawable takenBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_taken_bg);
        view.setBackground(reserved ? takenBg : freeBg);
    }
}
