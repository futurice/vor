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
import com.futurice.hereandnow.pojo.Toilet;
import com.futurice.hereandnow.view.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ToiletMapFragment extends Fragment {

    @Bind(R.id.toiletMap) MapView mToiletMapImageView;

    Activity mActivity;

    ArrayList<Toilet> mToilets = new ArrayList<>();
    Toilet m7am, m7bm, m8am, m8aw, m8bm, m8bw, m8cm, m8cw;
    ArrayList<OnSharedPreferenceChangeListener> mOnSharedPreferenceChangeListeners = new ArrayList<>();

    private int mCurrentFloor;

    public ToiletMapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCurrentFloor = 7;

        mActivity = getActivity();

        m7am = new Toilet(7, "toilet7am");
        m7bm = new Toilet(7, "toilet7bm");

        m8am = new Toilet(8, "toilet8am");
        m8aw = new Toilet(8, "toilet8aw");
        m8bm = new Toilet(8, "toilet8bm");
        m8bw = new Toilet(8, "toilet8bw");
        m8cm = new Toilet(8, "toilet8cm");
        m8cw = new Toilet(8, "toilet8cw");

        mToilets.add(m7am);
        mToilets.add(m7bm);
        mToilets.add(m8am);
        mToilets.add(m8aw);
        mToilets.add(m8bm);
        mToilets.add(m8bw);
        mToilets.add(m8cm);
        mToilets.add(m8cw);

        for (Toilet toilet : mToilets) {
            SharedPreferences sp = mActivity.getSharedPreferences(toilet.getId(), Context.MODE_PRIVATE);
            toilet.setSharedPreferences(sp);
            updateStatus(toilet);
            mOnSharedPreferenceChangeListeners.add((sharedPreferences, key) -> {
                updateStatus(toilet);
                int floor = toilet.getFloor();
                if (floor == 7) {
                    updateView7th();
                } else if (floor == 8) {
                    updateView8th();
                }
            });
        }
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
        Boolean am = !m7am.getStatus();
        Boolean bm = !m7bm.getStatus();

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
        Boolean am = !m8am.getStatus();
        Boolean bm = !m8bm.getStatus();
        Boolean cm = !m8cm.getStatus();

        Boolean aw = !m8aw.getStatus();
        Boolean bw = !m8bw.getStatus();
        Boolean cw = !m8cw.getStatus();

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
        for(int i = 0; i < mToilets.size(); i++) {
            SharedPreferences sharedPreferences = mToilets.get(i).getSharedPreferences();
            OnSharedPreferenceChangeListener listener = mOnSharedPreferenceChangeListeners.get(i);
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private void unregisterListeners() {
        for(int i = 0; i < mToilets.size(); i++) {
            SharedPreferences sharedPreferences = mToilets.get(i).getSharedPreferences();
            OnSharedPreferenceChangeListener listener = mOnSharedPreferenceChangeListeners.get(i);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    private void updateStatus(Toilet toilet) {
        SharedPreferences sharedPreferences = toilet.getSharedPreferences();
        String json = sharedPreferences.getString(toilet.getId(), null);
        if (json != null) {
            try {
                JSONObject jsonData = new JSONObject(json);
                toilet.setStatus(jsonData.getBoolean(Constants.RESERVED_KEY));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
