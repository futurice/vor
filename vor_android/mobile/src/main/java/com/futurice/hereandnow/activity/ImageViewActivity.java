package com.futurice.hereandnow.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.ImageView;

import com.futurice.hereandnow.R;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends Activity {

    @NonNull

    public static final String IMAGE_URI = "imageUri";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_imageview);

        final String uri = getIntent().getExtras().getString(IMAGE_URI);
        Picasso.with(this)
                .load(Uri.parse(uri))
                .fit()
                .centerInside()
                .into((ImageView) findViewById(R.id.image_view));
    }
}
