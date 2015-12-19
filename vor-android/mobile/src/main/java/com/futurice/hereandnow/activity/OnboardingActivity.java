package com.futurice.hereandnow.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.utils.HereAndNowUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OnboardingActivity extends BaseActivity {
    @Bind(R.id.onboardingEmailEditText) EditText mOnboardingEmailEditText;
    @Bind(R.id.onboardingButton) Button mOnboardingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences emailSP = PreferenceManager.getDefaultSharedPreferences(this);
        if (emailSP.contains(SettingsActivity.EMAIL_KEY)) {
            startThings();
        } else {
            setContentView(R.layout.activity_onboarding);
            ButterKnife.bind(this);
            mOnboardingButton.setOnClickListener(v -> {
                String email = String.valueOf(mOnboardingEmailEditText.getText());
                if (email != null && !email.trim().isEmpty()) {
                    if (HereAndNowUtils.isEmailValid(email)) {
                        SharedPreferences.Editor editor = emailSP.edit();
                        editor.putString(SettingsActivity.EMAIL_KEY, email);
                        editor.apply();
                        startThings();
                    } else {
                        Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.please_add_email, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startThings() {
        Intent intent = new Intent(this, DrawerActivity.class);
        startActivity(intent);
    }
}
