package com.futurice.hereandnow.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.services.LocationService;
import com.futurice.hereandnow.utils.HereAndNowUtils;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    public static String TAG_KEY = "settings_my_tag";
    public static String ID_TAG_KEY = "settings_my_id_tag";
    public static String ABOUT_KEY = "settings_about_me";
    public static String EMAIL_KEY = "settings_my_email";
    public static String BACKGROUND_SERVICE_KEY = "settings_background_service";
    private static Intent resultIntent;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        if (preference.getKey().equals(SettingsActivity.EMAIL_KEY)) {
            Context ctx = preference.getContext();
            if (HereAndNowUtils.isEmailValid((String) value)) {
                preference.setSummary((String) value);
                resultIntent.putExtra(preference.getKey(), (String) value);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
                String oldValue = sp.getString(preference.getKey(), "");
                if (!oldValue.equals(value)) {
                    Toast.makeText(ctx, R.string.email_saved, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else {
                Toast.makeText(ctx, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    };

    /**
     * Start LocationService if the value is set to true, stop it otherwise.
     */
    private OnPreferenceChangeListener runLocationServiceByValueListener = ((preference, newValue) -> {
        final Intent intent = new Intent(this, LocationService.class);

        if (Boolean.valueOf(newValue.toString())) {
            getApplicationContext().startService(intent);
        } else {
            getApplicationContext().stopService(intent);
        }

       return true;
    });

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(@NonNull final Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(@NonNull final Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(@NonNull final Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    private void bindBackgroundServiceToPreference(@NonNull final Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(runLocationServiceByValueListener);
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Already set the result as OK
        resultIntent = this.getIntent();
        this.setResult(RESULT_OK, resultIntent);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle(R.string.action_settings);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        //TODO The better behaviour would be to have a preferences Fragment instead of an Activity
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));

        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_general);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_general);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        //bindPreferenceSummaryToValue(findPreference(TAG_KEY));
        //bindPreferenceSummaryToValue(findPreference(ABOUT_KEY));
        bindPreferenceSummaryToValue(findPreference(EMAIL_KEY));
        bindBackgroundServiceToPreference(findPreference(BACKGROUND_SERVICE_KEY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(@NonNull final List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    protected boolean isValidFragment(@NonNull final String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            //bindPreferenceSummaryToValue(findPreference(TAG_KEY));
            //bindPreferenceSummaryToValue(findPreference(ABOUT_KEY));
            bindPreferenceSummaryToValue(findPreference(EMAIL_KEY));
        }
    }
}
