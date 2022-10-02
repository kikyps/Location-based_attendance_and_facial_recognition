package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;

public class EmailVerificationActivity extends AppCompatActivity {

    Button backLogin;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    ImageView done;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        backLogin = findViewById(R.id.back_login);
        done = findViewById(R.id.icon_done);
        validOtp();
        backLogin.setOnClickListener(v -> {
            Preferences.signOut(context, LoginActivity.class);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Preferences.signOut(context, LoginActivity.class);
        finish();
    }

    private void validOtp(){
        done.setVisibility(View.VISIBLE);
        Drawable drawable = done.getDrawable();

        if (drawable instanceof AnimatedVectorDrawableCompat){
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable){
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
    }
}