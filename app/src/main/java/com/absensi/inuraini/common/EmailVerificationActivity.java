package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;

public class EmailVerificationActivity extends AppCompatActivity {

    Button backLogin, resend;
    TextView getEmail;
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
        getEmail = findViewById(R.id.getour_email);
        resend = findViewById(R.id.resend_emailverif);
        resend.setVisibility(View.INVISIBLE);
        String emailReg = getIntent().getStringExtra("emailReg");
        boolean reVer = getIntent().getBooleanExtra("reVerif", false);
        validOtp();
        getEmail.setText(emailReg);
        if (reVer) {
            new Handler().postDelayed(() -> {
                resend.setVisibility(View.VISIBLE);
                resend.setOnClickListener(v -> {
                    Preferences.mAuth.getCurrentUser().sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                Toast.makeText(context, "Email verifikasi berhasil di kirim silahkan check email anda", Toast.LENGTH_SHORT).show();
                            });
                });
            }, 30000);
        } else {
            resend.setVisibility(View.INVISIBLE);
        }

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
