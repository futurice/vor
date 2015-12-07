package com.futurice.hereandnow.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

public class BaseActivity extends AppCompatActivity {
    protected boolean hasExtra(@NonNull final String name) {
        return getIntent().getExtras() != null && getIntent().getExtras().get(name) != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        checkForCrashes();
        checkForUpdates();
    }

    @Nullable
    protected Object getExtra(@NonNull final String name) {
        if (hasExtra(name)) {
            return getIntent().getExtras().get(name);
        }
        return null;
    }

    private void checkForCrashes() {
        CrashManager.register(this, "cc7d62e53c364c109b5ac0e36313bdbc");
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, "cc7d62e53c364c109b5ac0e36313bdbc");
    }
}
