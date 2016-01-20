package com.futurice.vor.activity;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.futurice.vor.R;
import com.futurice.vor.view.MapView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HeatMapActivity extends BaseActivity {
    @Bind(R.id.futumap) MapView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.map_general_8th_floor);
        mImageView.setImageDrawable(drawable);
    }
}
