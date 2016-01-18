package com.futurice.vor.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futurice.vor.R;
import com.futurice.vor.Toilet;
import com.futurice.vor.utils.ToiletUtils;

import java.util.ArrayList;

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

    ArrayList<String> mIds = new ArrayList<>();
    ArrayList<RelativeLayout> mRelativeLayouts = new ArrayList<>();
    ArrayList<SharedPreferences> mSharedPreferences = new ArrayList<>();
    ArrayList<OnSharedPreferenceChangeListener> mOnSharedPreferenceChangeListeners = new ArrayList<>();

    public Floor8Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        mIds.add(Toilet.AM8.getId());
        mIds.add(Toilet.AW8.getId());
        mIds.add(Toilet.BM8.getId());
        mIds.add(Toilet.BW8.getId());
        mIds.add(Toilet.CM8.getId());
        mIds.add(Toilet.CW8.getId());

        for (String id : mIds) {
            mSharedPreferences.add(mActivity.getSharedPreferences(id, Context.MODE_PRIVATE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_floor_8_toilet, container, false);
        ButterKnife.bind(this, view);

        setupView();

        return view;
    }

    private void setupView() {
        mRelativeLayouts.add(mToilet8AM);
        mRelativeLayouts.add(mToilet8AW);
        mRelativeLayouts.add(mToilet8BM);
        mRelativeLayouts.add(mToilet8BW);
        mRelativeLayouts.add(mToilet8CM);
        mRelativeLayouts.add(mToilet8CW);

        for (int i = 0; i < mRelativeLayouts.size(); i++) {
            RelativeLayout relativeLayout = mRelativeLayouts.get(i);
            mOnSharedPreferenceChangeListeners.add((sharedPreferences, key) -> {
                ToiletUtils.updateView(sharedPreferences, key, relativeLayout, mActivity);
            });
            SharedPreferences sp = mSharedPreferences.get(i);
            String id = mIds.get(i);
            relativeLayout.setOnClickListener(v -> {
                ToiletUtils.setClickListener(sp.getString(id, null), mActivity);
            });
            ToiletUtils.updateView(sp, mIds.get(i), relativeLayout, mActivity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterListeners();
    }

    private void registerListeners() {
        for(int i = 0; i < mSharedPreferences.size(); i++) {
            SharedPreferences sharedPreferences = mSharedPreferences.get(i);
            OnSharedPreferenceChangeListener listener = mOnSharedPreferenceChangeListeners.get(i);
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private void unregisterListeners() {
        for(int i = 0; i < mSharedPreferences.size(); i++) {
            SharedPreferences sharedPreferences = mSharedPreferences.get(i);
            OnSharedPreferenceChangeListener listener = mOnSharedPreferenceChangeListeners.get(i);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}
