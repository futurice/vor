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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.view.MapView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ToiletMapFragment extends Fragment {
    @Bind(R.id.toiletMap) MapView mToiletMapImageView;

    private static final int FLOOR_7_AA_BA_DRAWABLE = R.drawable.map_toilet_7th_floor_aa_ba;
    private static final int FLOOR_7_AA_BT_DRAWABLE = R.drawable.map_toilet_7th_floor_aa_bt;
    private static final int FLOOR_7_AT_BA_DRAWABLE = R.drawable.map_toilet_7th_floor_at_ba;
    private static final int FLOOR_7_AT_BT_DRAWABLE = R.drawable.map_toilet_7th_floor_at_bt;
    private static final int FLOOR_8_DRAWABLE = R.drawable.map_general_8th_floor;

    Activity mActivity;

    SharedPreferences mToilet7amSP;
    SharedPreferences mToilet7bmSP;

    OnSharedPreferenceChangeListener toilet7amListener = (sharedPreferences, key) -> updateView();
    OnSharedPreferenceChangeListener toilet7bmListener = (sharedPreferences, key) -> updateView();

    private int mCurrentFloor;

    public ToiletMapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCurrentFloor = 7;

        mActivity = getActivity();
        mToilet7amSP = mActivity.getSharedPreferences("toilet7am", Context.MODE_PRIVATE);
        mToilet7bmSP = mActivity.getSharedPreferences("toilet7bm", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_toilet, container, false);
        ButterKnife.bind(this, view);
        updateView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mToilet7amSP.registerOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7bmSP.registerOnSharedPreferenceChangeListener(toilet7bmListener);

    }

    @Override
    public void onPause() {
        mToilet7amSP.registerOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7bmSP.registerOnSharedPreferenceChangeListener(toilet7bmListener);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toilet_map, menu);
        MenuItem changeFloorAction = menu.findItem(R.id.action_change_floor);
        int floorResourceId = mCurrentFloor == 7 ? R.string.map_8th_floor : R.string.map_7th_floor;
        changeFloorAction.setTitle(getString(floorResourceId));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_floor:
                if (mCurrentFloor == 7) {
                    setToiletMapImageView(FLOOR_8_DRAWABLE);
                    item.setTitle(getString(R.string.map_7th_floor));
                    mCurrentFloor = 8;
                } else {
                    updateView();
                    item.setTitle(getString(R.string.map_8th_floor));
                    mCurrentFloor = 7;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setToiletMapImageView(int id) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        mToiletMapImageView.setImageDrawable(drawable);
    }

    public void updateView() {
        Boolean am = mToilet7amSP.getBoolean(Constants.RESERVED_KEY, false);
        Boolean bm = mToilet7bmSP.getBoolean(Constants.RESERVED_KEY, false);

        if (am && bm) {
            setToiletMapImageView(FLOOR_7_AT_BT_DRAWABLE);
        } else if (am && !bm) {
            setToiletMapImageView(FLOOR_7_AA_BT_DRAWABLE);
        } else if (!am && bm) {
            setToiletMapImageView(FLOOR_7_AT_BA_DRAWABLE);
        } else {
            setToiletMapImageView(FLOOR_7_AA_BA_DRAWABLE);
        }
    }
}
