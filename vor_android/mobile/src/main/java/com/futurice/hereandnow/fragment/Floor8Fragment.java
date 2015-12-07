package com.futurice.hereandnow.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futurice.hereandnow.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class Floor8Fragment extends Fragment {
    @Bind(R.id.toilet8AM) RelativeLayout mToilet8AM;
    @Bind(R.id.toilet8AW) RelativeLayout mToilet8AW;
    @Bind(R.id.toilet8BM) RelativeLayout mToilet8BM;
    @Bind(R.id.toilet8BW) RelativeLayout mToilet8BW;
    @Bind(R.id.toilet8CM) RelativeLayout mToilet8CM;
    @Bind(R.id.toilet8CW) RelativeLayout mToilet8CW;

    public Floor8Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_floor_8_toilet, container, false);
        ButterKnife.bind(this, view);

        updateView();

        return view;
    }

    private void updateView() {
        Drawable freeBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_free_bg);
        Drawable takenBg = ContextCompat.getDrawable(getContext(), R.drawable.toilet_taken_bg);

        SharedPreferences toilet8am = getActivity().getSharedPreferences("toilet8am", Context.MODE_PRIVATE);
        SharedPreferences toilet8aw = getActivity().getSharedPreferences("toilet8aw", Context.MODE_PRIVATE);
        SharedPreferences toilet8bm = getActivity().getSharedPreferences("toilet8bm", Context.MODE_PRIVATE);
        SharedPreferences toilet8bw = getActivity().getSharedPreferences("toilet8bw", Context.MODE_PRIVATE);
        SharedPreferences toilet8cm = getActivity().getSharedPreferences("toilet8cm", Context.MODE_PRIVATE);
        SharedPreferences toilet8cw = getActivity().getSharedPreferences("toilet8cw", Context.MODE_PRIVATE);

        mToilet8AM.setBackground(toilet8am.getBoolean("reserved", false) ? takenBg : freeBg);
        mToilet8AW.setBackground(toilet8aw.getBoolean("reserved", false) ? takenBg : freeBg);
        mToilet8BM.setBackground(toilet8bm.getBoolean("reserved", false) ? takenBg : freeBg);
        mToilet8BW.setBackground(toilet8bw.getBoolean("reserved", false) ? takenBg : freeBg);
        mToilet8CM.setBackground(toilet8cm.getBoolean("reserved", false) ? takenBg : freeBg);
        mToilet8CW.setBackground(toilet8cw.getBoolean("reserved", false) ? takenBg : freeBg);
    }
}
