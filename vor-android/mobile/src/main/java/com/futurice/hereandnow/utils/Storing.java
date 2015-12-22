package com.futurice.hereandnow.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.futurice.hereandnow.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Luís Ramalho on 22/12/15.
 * <luis.ramalho@futurice.com>
 */
public class Storing {

    public static void saveToiletToSharedPreferences(JSONObject jsonObject, Context context)
            throws JSONException {
        String toiletId = jsonObject.getString(Constants.ID_KEY);
        SharedPreferences toilets = context.getSharedPreferences(toiletId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = toilets.edit();
        editor.putString(Constants.ID_KEY, jsonObject.getString(Constants.ID_KEY));
        editor.putBoolean(Constants.RESERVED_KEY, jsonObject.getBoolean(Constants.RESERVED_KEY));
        editor.putInt(Constants.METHANE_KEY, jsonObject.getInt(Constants.METHANE_KEY));
        editor.apply();
    }

    public static void saveTestToSharedPreferences(String message, Context context)
            throws JSONException {
        SharedPreferences testSP = context.getSharedPreferences(Constants.TEST_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = testSP.edit();
        editor.putString(Constants.MESSAGE_KEY, message);
        editor.apply();
    }

    public static void savePoolToSharedPreferences(String file, Context context)
            throws JSONException {
        SharedPreferences poolSP = context.getSharedPreferences(Constants.POOL_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = poolSP.edit();
        editor.putString(Constants.IMAGE_KEY, file);
        editor.apply();
    }

    public static void saveFoodToSharedPreferences(String file, Context context)
            throws JSONException {
        SharedPreferences foodSP = context.getSharedPreferences(Constants.FOOD_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = foodSP.edit();
        editor.putString(Constants.IMAGE_KEY, file);
        editor.apply();
    }
}
