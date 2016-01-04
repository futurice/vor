package com.futurice.hereandnow.fragment;

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

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.Toilet;
import static com.futurice.hereandnow.Constants.*;
import com.futurice.hereandnow.view.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ToiletMapFragment extends Fragment {

    @Bind(R.id.toiletMap) MapView mToiletMapImageView;

    ArrayList<String> mToiletIds = new ArrayList<>();
    ArrayList<OnSharedPreferenceChangeListener> mOnSharedPreferenceChangeListeners;

    private int mCurrentFloor;

    public ToiletMapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCurrentFloor = 7;
        init();
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
        registerListeners();
    }

    @Override
    public void onPause() {
        unregisterListeners();
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
        boolean am = !getStatus("toilet7am");
        boolean bm = !getStatus("toilet7bm");

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
        boolean am = !getStatus("toilet8am");
        boolean bm = !getStatus("toilet8bm");
        boolean cm = !getStatus("toilet8cm");

        boolean aw = !getStatus("toilet8aw");
        boolean bw = !getStatus("toilet8bw");
        boolean cw = !getStatus("toilet8cw");

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

    private void registerListeners() {
        for(int i = 0; i < mToiletIds.size(); i++) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                    mToiletIds.get(i),
                    Context.MODE_PRIVATE);
            OnSharedPreferenceChangeListener listener = mOnSharedPreferenceChangeListeners.get(i);
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private void unregisterListeners() {
        for(int i = 0; i < mToiletIds.size(); i++) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                    mToiletIds.get(i),
                    Context.MODE_PRIVATE);
            OnSharedPreferenceChangeListener listener = mOnSharedPreferenceChangeListeners.get(i);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    private void init() {
        mOnSharedPreferenceChangeListeners = new ArrayList<>();
        for (Toilet toilet : Toilet.values()) {
            mToiletIds.add(toilet.getId());
            mOnSharedPreferenceChangeListeners.add((sharedPreferences, key) -> {
                if (key.equals(Toilet.AM7.getId()) || key.equals(Toilet.BM7.getId())) {
                    updateView7th();
                } else {
                    updateView8th();
                }
            });
        }
    }

    private boolean getStatus(String id) {
        boolean status = false;
        SharedPreferences sp = getActivity().getSharedPreferences(id, Context.MODE_PRIVATE);
        try {
            if (sp.contains(id)) {
                JSONObject jsonData = new JSONObject(sp.getString(id, null));
                status = jsonData.getBoolean(RESERVED_KEY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return status;
    }
}
