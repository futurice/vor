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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Floor7Fragment extends Fragment {
    private static final String TAG = Floor7Fragment.class.getSimpleName();

    @Bind(R.id.toilet7AM) RelativeLayout mToilet7AM;
    @Bind(R.id.toilet7BM) RelativeLayout mToilet7BM;

    Activity mActivity;

    SharedPreferences mToilet7amSP;
    SharedPreferences mToilet7bmSP;

    OnSharedPreferenceChangeListener toilet7amListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet7AM);
    };

    OnSharedPreferenceChangeListener toilet7bmListener = (sharedPreferences, key) -> {
        updateToiletView(sharedPreferences, key, mToilet7BM);
    };

    public Floor7Fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mToilet7amSP = mActivity.getSharedPreferences("toilet7am", Context.MODE_PRIVATE);
        mToilet7bmSP = mActivity.getSharedPreferences("toilet7bm", Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mToilet7amSP.registerOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7bmSP.registerOnSharedPreferenceChangeListener(toilet7bmListener);

    }

    @Override
    public void onPause() {
        mToilet7amSP.unregisterOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7bmSP.unregisterOnSharedPreferenceChangeListener(toilet7bmListener);
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
        updateToiletBackground(mToilet7AM, mToilet7amSP.getBoolean(Constants.RESERVED_KEY, false));
        updateToiletBackground(mToilet7BM, mToilet7bmSP.getBoolean(Constants.RESERVED_KEY, false));
    }

    private void updateToiletView(SharedPreferences sharedPreferences, String key, View view) {
        switch (key) {
            case Constants.RESERVED_KEY:
                updateToiletBackground(view, sharedPreferences.getBoolean(key, false));
                break;
            case Constants.METHANE_KEY:
                Log.d(TAG, "Methane level: " + Integer.toString(sharedPreferences.getInt(key, 0)));
                break;
            default:
                break;
        }
    }

    public void updateToiletBackground(View view, Boolean reserved) {
        Drawable freeBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_free_bg);
        Drawable takenBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_taken_bg);
        view.setBackground(reserved ? takenBg : freeBg);
    }
}
