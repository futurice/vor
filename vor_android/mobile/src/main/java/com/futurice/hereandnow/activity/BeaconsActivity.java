package com.futurice.hereandnow.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.futurice.hereandnow.R;

public class BeaconsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beacons);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
