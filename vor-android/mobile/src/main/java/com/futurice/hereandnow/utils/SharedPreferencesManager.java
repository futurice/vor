package com.futurice.hereandnow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static com.futurice.hereandnow.Constants.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Luís Ramalho on 22/12/15.
 * <luis.ramalho@futurice.com>
 */
public class SharedPreferencesManager {

    /**
     * Saves the jsonObject to SharedPreferences as a string
     *
     * @param jsonObject the json object
     * @param context the context
     */
    public static void saveToSharedPreferences(JSONObject jsonObject, Context context) {
        try {
            String jsonObjectId = jsonObject.getString(ID_KEY);
            SharedPreferences sp = context.getSharedPreferences(jsonObjectId, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(jsonObjectId, jsonObject.toString());
            editor.apply();
        } catch (JSONException jsonException) {
            Log.e("JSON Parser", "Error parsing data: " + jsonException.getMessage());
        }
    }
}
