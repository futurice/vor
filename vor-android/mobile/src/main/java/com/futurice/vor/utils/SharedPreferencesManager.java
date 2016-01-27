package com.futurice.vor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static com.futurice.vor.Constants.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

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
        if(jsonObject.has(TYPE_KEY)) {
            try {
                String type = jsonObject.getString(TYPE_KEY);
                if (isValidType(type) && jsonObject.has(ID_KEY)) {
                    String jsonObjectId = jsonObject.getString(ID_KEY);
                    SharedPreferences sp = context.getSharedPreferences(
                            jsonObjectId,
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(jsonObjectId, jsonObject.toString());
                    editor.apply();
                }
            } catch (JSONException jsonException) {
                Log.e("JSON Parser", "Error parsing data: " + jsonException.getMessage());
            }
        }
    }

    /**
     * Checks if a string is of an accepted type
     *
     * @param type the type
     * @return <code>true</code> if it's of a valid type; <code>false</code> otherwise.
     */
    private static boolean isValidType(String type) {
        String[] types = {LOCATION_KEY, PRINTER_3D_KEY, POOL_KEY, FOOD_KEY, TOILET_KEY, TEST_KEY};
        return (Arrays.asList(types).contains(type));
    }
}
