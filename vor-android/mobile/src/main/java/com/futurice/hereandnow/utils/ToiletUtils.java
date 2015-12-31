package com.futurice.hereandnow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;
import static com.futurice.hereandnow.Constants.*;
import com.futurice.hereandnow.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Luís Ramalho on 23/12/15.
 * <luis.ramalho@futurice.com>
 */
public class ToiletUtils {
    /**
     * Implementation of the click listener to show methane level
     *
     * @param json the json string
     */
    public static void setClickListener(String json, Context context) {
        if (json != null) {
            try {
                JSONObject jsonData = new JSONObject(json);
                Integer methane = jsonData.getInt(METHANE_KEY);
                showMethaneToast(methane, context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateView(SharedPreferences sharedPreferences, String key, View view, Context context) {
        String json = sharedPreferences.getString(key, null);
        if (json != null) {
            try {
                JSONObject jsonData = new JSONObject(json);
                updateToiletBackground(view, jsonData.getBoolean(RESERVED_KEY), context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the background of the view depending on toilet availability
     *
     * @param view the view
     * @param reserved the state of the toilet
     */
    public static void updateToiletBackground(View view, Boolean reserved, Context context) {
        Drawable freeBg = ContextCompat.getDrawable(context, R.drawable.toilet_free_bg);
        Drawable takenBg = ContextCompat.getDrawable(context, R.drawable.toilet_taken_bg);
        view.setBackground(reserved ? takenBg : freeBg);
    }

    /**
     * Shows a toast with the methane level
     *
     * @param methane the methane level
     *
     * @param context the context
     */
    public static void showMethaneToast(Integer methane, Context context) {
        String message = "Methane level: " + Integer.toString(methane);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
