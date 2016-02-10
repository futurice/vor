package com.futurice.vor.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.ImageView;
import static com.futurice.vor.Constants.*;
import com.futurice.vor.R;
import com.futurice.vor.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageViewActivity extends Activity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_imageview);

        final ImageView imageView = (ImageView) findViewById(R.id.image_view);
        String type = getIntent().getExtras().getString(TYPE_KEY);
        try {
            String json = getSharedPreferences(type, Context.MODE_PRIVATE).getString(type, null);
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(IMAGE_KEY)) {
                String base64 = jsonObject.getString(IMAGE_KEY);
                imageView.setImageBitmap(FileUtils.base64ToBitmap(base64));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
