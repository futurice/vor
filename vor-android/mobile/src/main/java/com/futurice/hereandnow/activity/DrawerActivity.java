package com.futurice.hereandnow.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.i.IActionOne;
import com.futurice.cascade.i.IAsyncOrigin;
import com.futurice.cascade.reactive.ReactiveValue;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.singleton.ModelSingleton;
import com.futurice.hereandnow.singleton.ServiceSingleton;
import com.futurice.hereandnow.utils.HereAndNowUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

public class DrawerActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        IAsyncOrigin,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int PLAY_VIDEO_INTENT_RESULT = 12345;
    public static final int PUBLISH_MEDIA_INTENT_RESULT = 12346;
    public static final int SETTINGS_INTENT_RESULT = 12348;
    private static final String TAG = DrawerActivity.class.getSimpleName();
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static IActionOne<Uri> actionAfterSelectVideo;

    private final SparseArray<TextWatcher> mSearchWatchers = new SparseArray<>();
    //    private static IActionOne<Uri> actionAfterCaptureVideo;
    // A View Model object, we will bind the ReactiveTextView to this and similar objects
    ReactiveValue<String> chatReactiveValue;
    private final ImmutableValue<String> origin = RCLog.originAsync();

    private boolean bluetoothNotificationShowed, wifiNotificationShowed;

    TextView mNameTextView;
    TextView mEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServiceSingleton.create(this.getApplicationContext());
        ModelSingleton.create(this.getApplicationContext());

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
        String email = prefs.getString(SettingsActivity.EMAIL_KEY, Constants.DUMMY_EMAIL);

        bluetoothNotificationShowed = false;
        wifiNotificationShowed = false;

        mNameTextView.setText(HereAndNowUtils.getName(email));
        mEmailTextView.setText(email);

        bluetoothNotificationShowed = false;
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

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled() && !bluetoothNotificationShowed && !saveBluetoothState) {
            View notificationView = getLayoutInflater().inflate(R.layout.checkbox_bluetooth, null);
            final CheckBox doNotShowAgain = (CheckBox) notificationView.findViewById(R.id.checkbox_bluetooth);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(notificationView);
            alert.setTitle(getString(R.string.notification_bluetooth_title));
            alert.setMessage(getString(R.string.notification_bluetooth_message));
            alert.setCancelable(false);
            alert.setPositiveButton(getString(R.string.notification_bluetooth_button), (dialog, which) -> {
                bluetoothNotificationShowed = true;

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

        // Display a notification if the user is not connected to the correct wifi.
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (!wifiInfo.getSSID().equals(Constants.NETWORK_SSID) && !savedWifiState && !wifiNotificationShowed) {
            View notificationView = getLayoutInflater().inflate(R.layout.checkbox_bluetooth, null);
            final CheckBox doNotShowAgain = (CheckBox) notificationView.findViewById(R.id.checkbox_bluetooth);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(notificationView);
            alert.setTitle(getString(R.string.notification_wifi_title));
            alert.setMessage(String.format(getString(R.string.notification_wifi_message), Constants.NETWORK_SSID));
            alert.setCancelable(false);
            alert.setPositiveButton(getString(R.string.notification_bluetooth_button), (dialog, which) -> {
                wifiNotificationShowed = true;

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
    }

    private void initReactiveValues() {
        // There are all per app run- not initialized from flash memory like the persistent values
        if (chatReactiveValue == null) {
            chatReactiveValue = new ReactiveValue<>("ChatValue", ""); // Bindings to this View Model will fire on the UI thread by default, but they can specify something else if they prefer
        }
    }

    private void disconnectScampiServices() {
        ServiceSingleton.instance().scampiHandler().cancelReconnect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        if (id == R.id.nav_my_cards) {
            startActivity(new Intent(this, CardsActivity.class));
        } else if (id == R.id.nav_people) {
            startActivity(new Intent(this, PeopleActivity.class));
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, MapActivity.class));
        } else if (id == R.id.nav_space) {
            startActivity(new Intent(this, SpaceActivity.class));
        } else if (id == R.id.nav_toilets) {
            startActivity(new Intent(this, ToiletActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_INTENT_RESULT);
        } else if (id == R.id.nav_logout) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        if (id == R.id.action_settings) {
//            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_INTENT_RESULT);
            return true;
        }

        if (id == R.id.action_close_service) {
            final List<SCAMPIMessage> unpublishedSCAMPIMessages = ServiceSingleton.instance().scampiHandler().stop(1000);

            disconnectScampiServices();
            HereAndNowApplication.stopServiceNow(this);
            HereAndNowApplication app = (HereAndNowApplication) getApplication();
            app.removeCallback();
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(
            final int requestCode,
            final int resultCode,
            @NonNull final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLAY_VIDEO_INTENT_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    actionAfterSelectVideo.call(data.getData());
                } catch (Exception e) {
                    RCLog.e(this, "Problem executing action after content selection", e);
                }
            } else {
                RCLog.i(this, "Problem with externally selected video play intent result: " + resultCode);
            }
        }
        if (requestCode == PUBLISH_MEDIA_INTENT_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    actionAfterSelectVideo.call(data.getData());
                } catch (Exception e) {
                    RCLog.e(this, "Problem executing action after content selection", e);
                }
            } else {
                RCLog.i(this, "Problem with externally selected video publish intent result: " + resultCode);
            }
        }
        if (requestCode == SETTINGS_INTENT_RESULT) {
            RCLog.d(this, "Copying over local reactive persistent settings with the latest values from the SettingsActivity");
            final String preexistingIdTag = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.ID_TAG_KEY, UUID.randomUUID().toString());
//            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
//            editor.putString(SettingsActivity.ID_TAG_KEY, preexistingIdTag);
//            editor.apply();
//            ModelSingleton.instance().myTag.set(data.getStringExtra(SettingsActivity.TAG_KEY));
//            ModelSingleton.instance().myIdTag.set(preexistingIdTag);
//            ModelSingleton.instance().myAboutMe.set(data.getStringExtra(SettingsActivity.ABOUT_KEY));
//
//            // XXX: Not sure if the reactive stuff works, so I'll just push
//            // the changes old fashioned way.
//            // TODO: Should check if there actually was a change to optimize away useless calls.
//            final Peer localPeer = new Peer(ModelSingleton.instance().myTag.get(),
//                    ModelSingleton.instance().myIdTag.get(),
//                    ModelSingleton.instance().myAboutMe.get(),
//                    ModelSingleton.instance().myLikes.get(),
//                    ModelSingleton.instance().deletedCards.get(),
//                    ModelSingleton.instance().flaggedCards.get(),
//                    ModelSingleton.instance().myComments.get(),
//                    System.currentTimeMillis());
//
//            serviceSingleton.peerDiscoveryService().updateLocalUser(localPeer);
//            serviceSingleton.peerDiscoveryService().refreshLocalAdvert();
        }
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // TODO filter according to the query
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    @NonNull
    @Override // IAsyncOrigin
    public ImmutableValue<String> getOrigin() {
        return origin;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.EMAIL_KEY)) {
            String email = sharedPreferences.getString(key, null);
            if (email != null && !email.trim().isEmpty()) {
                if (HereAndNowUtils.isEmailValid(email)) {
                    mNameTextView.setText(HereAndNowUtils.getName(email));
                    mEmailTextView.setText(email);
                } else {
                    Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.please_add_email, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
