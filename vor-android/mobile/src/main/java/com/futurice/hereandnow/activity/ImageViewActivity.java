package com.futurice.hereandnow.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.ImageView;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.utils.FileUtils;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends Activity {

    @NonNull
    public static final String IMAGE_URI = "imageUri";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_imageview);

        final ImageView imageView = (ImageView) findViewById(R.id.image_view);
        if (getIntent().hasExtra(IMAGE_URI)) {
            Uri uri = Uri.parse(getIntent().getExtras().getString(IMAGE_URI));
            Picasso.with(this)
                    .load(uri)
                    .fit()
                    .centerInside()
                    .into(imageView);
        } else if (getIntent().hasExtra(Constants.TYPE_KEY)){
            String type = getIntent().getExtras().getString(Constants.TYPE_KEY);
            SharedPreferences sp = getSharedPreferences(type, Context.MODE_PRIVATE);
            String base64 = sp.getString(Constants.IMAGE_KEY, "Failed");
            imageView.setImageBitmap(FileUtils.base64ToBitmap(base64));
        }
    }
}
