package com.futurice.hereandnow.fragment;

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
import com.futurice.hereandnow.view.MapView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ToiletMapFragment extends Fragment {
    @Bind(R.id.toiletMap) MapView mToiletMapImageView;

    private static final int FLOOR_7_DRAWABLE = R.drawable.map_general_7th_floor;
    private static final int FLOOR_8_DRAWABLE = R.drawable.map_general_8th_floor;

    private int mCurrentFloor;

    public ToiletMapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCurrentFloor = 7;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_toilet, container, false);
        ButterKnife.bind(this, view);

        Drawable drawable = ContextCompat.getDrawable(getContext(), FLOOR_7_DRAWABLE);
        mToiletMapImageView.setImageDrawable(drawable);
        return view;
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
                    setToiletMapImageView(FLOOR_7_DRAWABLE);
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
}
