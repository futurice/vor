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
    private static final String TAG = ToiletMapFragment.class.getSimpleName();

    @Bind(R.id.toiletMap) MapView mToiletMapImageView;

    Activity mActivity;

    SharedPreferences mToilet7amSP;
    SharedPreferences mToilet7bmSP;

    SharedPreferences mToilet8amSP;
    SharedPreferences mToilet8bmSP;
    SharedPreferences mToilet8cmSP;
    SharedPreferences mToilet8awSP;
    SharedPreferences mToilet8bwSP;
    SharedPreferences mToilet8cwSP;

    OnSharedPreferenceChangeListener toilet7amListener = (sharedPreferences, key) -> updateView7th();
    OnSharedPreferenceChangeListener toilet7bmListener = (sharedPreferences, key) -> updateView7th();

    OnSharedPreferenceChangeListener toilet8amListener = (sharedPreferences, key) -> updateView8th();
    OnSharedPreferenceChangeListener toilet8bmListener = (sharedPreferences, key) -> updateView8th();
    OnSharedPreferenceChangeListener toilet8cmListener = (sharedPreferences, key) -> updateView8th();
    OnSharedPreferenceChangeListener toilet8awListener = (sharedPreferences, key) -> updateView8th();
    OnSharedPreferenceChangeListener toilet8bwListener = (sharedPreferences, key) -> updateView8th();
    OnSharedPreferenceChangeListener toilet8cwListener = (sharedPreferences, key) -> updateView8th();

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

        mToilet8amSP = mActivity.getSharedPreferences("toilet8am", Context.MODE_PRIVATE);
        mToilet8bmSP = mActivity.getSharedPreferences("toilet8bm", Context.MODE_PRIVATE);
        mToilet8cmSP = mActivity.getSharedPreferences("toilet8cm", Context.MODE_PRIVATE);
        mToilet8awSP = mActivity.getSharedPreferences("toilet8aw", Context.MODE_PRIVATE);
        mToilet8bwSP = mActivity.getSharedPreferences("toilet8bw", Context.MODE_PRIVATE);
        mToilet8cwSP = mActivity.getSharedPreferences("toilet8cw", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_toilet, container, false);
        ButterKnife.bind(this, view);
        updateView7th();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mToilet7amSP.registerOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7bmSP.registerOnSharedPreferenceChangeListener(toilet7bmListener);

        mToilet8amSP.registerOnSharedPreferenceChangeListener(toilet8amListener);
        mToilet8bmSP.registerOnSharedPreferenceChangeListener(toilet8bmListener);
        mToilet8cmSP.registerOnSharedPreferenceChangeListener(toilet8cmListener);
        mToilet8awSP.registerOnSharedPreferenceChangeListener(toilet8awListener);
        mToilet8bwSP.registerOnSharedPreferenceChangeListener(toilet8bwListener);
        mToilet8cwSP.registerOnSharedPreferenceChangeListener(toilet8cwListener);
    }

    @Override
    public void onPause() {
        mToilet7amSP.unregisterOnSharedPreferenceChangeListener(toilet7amListener);
        mToilet7bmSP.unregisterOnSharedPreferenceChangeListener(toilet7bmListener);

        mToilet8amSP.unregisterOnSharedPreferenceChangeListener(toilet8amListener);
        mToilet8bmSP.unregisterOnSharedPreferenceChangeListener(toilet8bmListener);
        mToilet8cmSP.unregisterOnSharedPreferenceChangeListener(toilet8cmListener);
        mToilet8awSP.unregisterOnSharedPreferenceChangeListener(toilet8awListener);
        mToilet8bwSP.unregisterOnSharedPreferenceChangeListener(toilet8bwListener);
        mToilet8cwSP.unregisterOnSharedPreferenceChangeListener(toilet8cwListener);
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
                    updateView8th();
                    item.setTitle(getString(R.string.map_7th_floor));
                    mCurrentFloor = 8;
                } else {
                    updateView7th();
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

    public void updateView7th() {
        Boolean am = mToilet7amSP.getBoolean(Constants.RESERVED_KEY, false);
        Boolean bm = mToilet7bmSP.getBoolean(Constants.RESERVED_KEY, false);

        if (am && bm) {
            setToiletMapImageView(R.drawable.map_toilet_7th_floor_aa_ba);
        } else if (am && !bm) {
            setToiletMapImageView(R.drawable.map_toilet_7th_floor_aa_bt);
        } else if (!am && bm) {
            setToiletMapImageView(R.drawable.map_toilet_7th_floor_at_ba);
        } else {
            setToiletMapImageView(R.drawable.map_toilet_7th_floor_at_bt);
        }
    }

    public void updateView8th() {
        Boolean am = !mToilet8amSP.getBoolean(Constants.RESERVED_KEY, true);
        Boolean bm = !mToilet8bmSP.getBoolean(Constants.RESERVED_KEY, true);
        Boolean cm = !mToilet8cmSP.getBoolean(Constants.RESERVED_KEY, true);

        Boolean aw = !mToilet8awSP.getBoolean(Constants.RESERVED_KEY, true);
        Boolean bw = !mToilet8bwSP.getBoolean(Constants.RESERVED_KEY, true);
        Boolean cw = !mToilet8cwSP.getBoolean(Constants.RESERVED_KEY, true);

        if (am && aw && bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwa_cma_cwa);
        } else if (am && aw && bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwa_cma_cwt);
        } else if (am && aw && bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwa_cmt_cwa);
        } else if (am && aw && bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwa_cmt_cwt);
        } else if (am && aw && bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwt_cma_cwa);
        } else if (am && aw && bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwt_cma_cwt);
        } else if (am && aw && bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwt_cmt_cwa);
        } else if (am && aw && bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bma_bwt_cmt_cwt);
        } else if (am && aw && !bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwa_cma_cwa);
        } else if (am && aw && !bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwa_cma_cwt);
        } else if (am && aw && !bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwa_cmt_cwa);
        } else if (am && aw && !bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwa_cmt_cwt);
        } else if (am && aw && !bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwt_cma_cwa);
        } else if (am && aw && !bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwt_cma_cwt);
        } else if (am && aw && !bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwt_cmt_cwa);
        } else if (am && aw && !bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awa_bmt_bwt_cmt_cwt);
        } else if (am && !aw && bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwa_cma_cwa);
        } else if (am && !aw && bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwa_cma_cwt);
        } else if (am && !aw && bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwa_cmt_cwa);
        } else if (am && !aw && bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwa_cmt_cwt);
        } else if (am && !aw && bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwt_cma_cwa);
        } else if (am && !aw && bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwt_cma_cwt);
        } else if (am && !aw && bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwt_cmt_cwa);
        } else if (am && !aw && bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bma_bwt_cmt_cwt);
        } else if (am && !aw && !bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwa_cma_cwa);
        } else if (am && !aw && !bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwa_cma_cwt);
        } else if (am && !aw && !bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwa_cmt_cwa);
        } else if (am && !aw && !bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwa_cmt_cwt);
        } else if (am && !aw && !bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwt_cma_cwa);
        } else if (am && !aw && !bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwt_cma_cwt);
        } else if (am && !aw && !bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwt_cmt_cwa);
        } else if (am && !aw && !bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_ama_awt_bmt_bwt_cmt_cwt);
        } else if (!am && aw && bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwa_cma_cwa);
        } else if (!am && aw && bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwa_cma_cwt);
        } else if (!am && aw && bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwa_cmt_cwa);
        } else if (!am && aw && bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwa_cmt_cwt);
        } else if (!am && aw && bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwt_cma_cwa);
        } else if (!am && aw && bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwt_cma_cwt);
        } else if (!am && aw && bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwt_cmt_cwa);
        } else if (!am && aw && bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bma_bwt_cmt_cwt);
        } else if (!am && aw && !bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwa_cma_cwa);
        } else if (!am && aw && !bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwa_cma_cwt);
        } else if (!am && aw && !bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwa_cmt_cwa);
        } else if (!am && aw && !bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwa_cmt_cwt);
        } else if (!am && aw && !bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwt_cma_cwa);
        } else if (!am && aw && !bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwt_cma_cwt);
        } else if (!am && aw && !bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwt_cmt_cwa);
        } else if (!am && aw && !bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awa_bmt_bwt_cmt_cwt);
        } else if (!am && !aw && bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwa_cma_cwa);
        } else if (!am && !aw && bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwa_cma_cwt);
        } else if (!am && !aw && bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwa_cmt_cwa);
        } else if (!am && !aw && bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwa_cmt_cwt);
        } else if (!am && !aw && bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwt_cma_cwa);
        } else if (!am && !aw && bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwt_cma_cwt);
        } else if (!am && !aw && bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwt_cmt_cwa);
        } else if (!am && !aw && bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bma_bwt_cmt_cwt);
        } else if (!am && !aw && !bm && bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwa_cma_cwa);
        } else if (!am && !aw && !bm && bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwa_cma_cwt);
        } else if (!am && !aw && !bm && bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwa_cmt_cwa);
        } else if (!am && !aw && !bm && bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwa_cmt_cwt);
        } else if (!am && !aw && !bm && !bw && cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwt_cma_cwa);
        } else if (!am && !aw && !bm && !bw && cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwt_cma_cwt);
        } else if (!am && !aw && !bm && !bw && !cm && cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwt_cmt_cwa);
        } else if (!am && !aw && !bm && !bw && !cm && !cw) {
            setToiletMapImageView(R.drawable.map_toilet_8th_floor_amt_awt_bmt_bwt_cmt_cwt);
        }
    }
}
