package com.futurice.hereandnow.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

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
        Intent intent = getIntent();
        if (intent.hasExtra(IMAGE_URI)) {
            Uri uri = Uri.parse(intent.getExtras().getString(IMAGE_URI));
            Picasso.with(this)
                    .load(uri)
                    .fit()
                    .centerInside()
                    .into(imageView);
        } else if (intent.hasExtra(Constants.TYPE_KEY)){
            String type = intent.getExtras().getString(Constants.TYPE_KEY);
            try {
                String json = getSharedPreferences(type, Context.MODE_PRIVATE).getString(type, null);
                String base64 = (new JSONObject(json)).getString(Constants.IMAGE_KEY);
                imageView.setImageBitmap(FileUtils.base64ToBitmap(base64));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
