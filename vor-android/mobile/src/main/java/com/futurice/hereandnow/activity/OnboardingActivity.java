package com.futurice.hereandnow.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OnboardingActivity extends BaseActivity {
    @Bind(R.id.onboardingEmailEditText) EditText mOnboardingEmailEditText;
    @Bind(R.id.onboardingButton) Button mOnboardingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences emailSP = getSharedPreferences(Constants.USER_EMAIL, Context.MODE_PRIVATE);
        if (emailSP.contains(Constants.EMAIL)) {
            startThings();
        } else {
            setContentView(R.layout.activity_onboarding);
            ButterKnife.bind(this);
            mOnboardingButton.setOnClickListener(v -> {
                String email = String.valueOf(mOnboardingEmailEditText.getText());
                if (email != null && !email.trim().isEmpty()) {
                    if (isEmailValid(email)) {
                        SharedPreferences.Editor editor = emailSP.edit();
                        editor.putString(Constants.EMAIL, email);
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

    private boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("^(.+\\..+)@futurice.com$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
