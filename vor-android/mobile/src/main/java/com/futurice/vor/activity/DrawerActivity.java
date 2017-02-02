package com.futurice.vor.activity;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.reactivecascade.functional.ImmutableValue;
import com.reactivecascade.i.IAsyncOrigin;
import com.reactivecascade.reactive.ReactiveValue;
import com.reactivecascade.util.RCLog;
import static com.futurice.vor.Constants.*;

import com.futurice.vor.R;
import com.futurice.vor.Toilet;
import com.futurice.vor.utils.VorUtils;

import java.lang.reflect.Field;

public class DrawerActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        IAsyncOrigin,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int SETTINGS_INTENT_RESULT = 12348;

    private final SparseArray<TextWatcher> mSearchWatchers = new SparseArray<>();

    // A View Model object, we will bind the ReactiveTextView to this and similar objects
    private ReactiveValue<String> mChatReactiveValue;

    private final ImmutableValue<String> mOriginAsync = RCLog.originAsync();

    private boolean mBluetoothNotificationShowed;
    private boolean mWifiNotificationShowed;

    TextView mNameTextView;
    TextView mEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initReactiveValues();
        setContentView(R.layout.activity_drawer);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        mNameTextView = (TextView) headerLayout.findViewById(R.id.drawerLayoutName);
        mEmailTextView = (TextView) headerLayout.findViewById(R.id.drawerLayoutEmail);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        String email = prefs.getString(SettingsActivity.EMAIL_KEY, DUMMY_EMAIL);

        mBluetoothNotificationShowed = false;
        mWifiNotificationShowed = false;

        mNameTextView.setText(VorUtils.getName(email));
        mEmailTextView.setText(email);

        mBluetoothNotificationShowed = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Previous states of do not show again.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean saveBluetoothState = preferences.getBoolean(
                getString(R.string.preferences_notification_sate),
                false);
        Boolean savedWifiState = preferences.getBoolean(
                getString(R.string.preferences_wifi_notification_state),
                false);

        // Display a notification if Bluetooth isn't enabled.
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled() && !mBluetoothNotificationShowed && !saveBluetoothState) {
            notifyBluetoothNotEnabled(preferences);
        }

        // Display a notification if the user is not connected to the correct wifi.
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!wifiInfo.getSSID().equals(NETWORK_SSID) && !savedWifiState && !mWifiNotificationShowed) {
            notifyUserNotConnectedToWifi(preferences);
        }
    }

    private void initReactiveValues() {
        // There are all per app run- not initialized from flash memory like the persistent values
        if (mChatReactiveValue == null) {
            mChatReactiveValue = new ReactiveValue<>("ChatValue", ""); // Bindings to this View Model will fire on the UI thread by default, but they can specify something else if they prefer
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // super.onBackPressed();
            VorUtils.minimizeApp(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(
                    new Intent(this, SettingsActivity.class),
                    SETTINGS_INTENT_RESULT);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Set cursor color to match the text color
        final EditText mSearchTextView = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            final Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(mSearchTextView, 0);
        } catch (Exception e) {
            RCLog.i(this, "Search cursor issue: " + e);
        }

        // Init the search field watcher if it's available
        if (mSearchWatchers.get(0) != null) {
            mSearchTextView.addTextChangedListener(mSearchWatchers.get(0));
        }

        super.onCreateOptionsMenu(menu);

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.nav_people) {
            startActivity(new Intent(this, PeopleMapActivity.class));
        } else if (id == R.id.nav_heat_map) {
            startActivity(new Intent(this, HeatMapActivity.class));
        } else if (id == R.id.nav_space) {
            startActivity(new Intent(this, SpaceActivity.class));
        } else if (id == R.id.nav_toilets) {
            startActivity(new Intent(this, ToiletActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivityForResult(
                    new Intent(this, SettingsActivity.class),
                    SETTINGS_INTENT_RESULT);
        } else if (id == R.id.nav_logout) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);

            for(Toilet toilet : Toilet.values()) {
                sp = getSharedPreferences(toilet.getId(), Context.MODE_PRIVATE);
                editor = sp.edit();
                editor.clear();
                editor.apply();
            }
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // TODO filter according to the query
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private void notifyUserNotConnectedToWifi(SharedPreferences preferences) {
        View notificationView = getLayoutInflater().inflate(R.layout.checkbox_bluetooth, null);
        final CheckBox doNotShowAgain = (CheckBox) notificationView.findViewById(R.id.checkbox_bluetooth);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(notificationView);
        alert.setTitle(getString(R.string.notification_wifi_title));
        alert.setMessage(String.format(getString(R.string.notification_wifi_message), NETWORK_SSID));
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.notification_close), (dialog, which) -> {
            mWifiNotificationShowed = true;

            if (doNotShowAgain.isChecked()) {
                // Do not show this dialog again.
                preferences.edit()
                        .putBoolean(getString(R.string.preferences_wifi_notification_state), true)
                        .apply();
            }
            dialog.dismiss();
        });

        alert.create().show();
    }

    private void notifyBluetoothNotEnabled(SharedPreferences preferences) {
        View notificationView = getLayoutInflater().inflate(R.layout.checkbox_bluetooth, null);
        final CheckBox doNotShowAgain = (CheckBox) notificationView.findViewById(R.id.checkbox_bluetooth);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(notificationView);
        alert.setTitle(getString(R.string.notification_bluetooth_title));
        alert.setMessage(getString(R.string.notification_bluetooth_message));
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.notification_close), (dialog, which) -> {
            mBluetoothNotificationShowed = true;

            if (doNotShowAgain.isChecked()) {
                // Do not show this dialog again.
                preferences.edit()
                        .putBoolean(getString(R.string.preferences_notification_sate), true)
                        .apply();
            }
            dialog.dismiss();
        });

        alert.create().show();
    }

    @NonNull
    @Override // IAsyncOrigin
    public ImmutableValue<String> getOrigin() {
        return mOriginAsync;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.EMAIL_KEY)) {
            String email = sharedPreferences.getString(key, null);
            mNameTextView.setText(VorUtils.getName(email));
            mEmailTextView.setText(email);
        }
    }
}
