package com.absensi.inuraini.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;

public class DoVerifActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce;
    Context context = this;
    Button signout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_verif);
        cotentListeners();
    }

    private void cotentListeners() {
        signout = findViewById(R.id.go_sign_out);
        signout.setOnClickListener(v -> {
            Preferences.showDialog(context,
                    null,
                    "Sign Out",
                    "Apakah anda yakin ingin sign out?",
                    "Ya",
                    "Tidak",
                    null,
                    (dialog, which) -> {
                        // Positive Button
                        Preferences.signOut(context, true, LoginActivity.class);
                        finish();
                    },
                    (dialog, which) -> {
                        // Negative Button
                        dialog.cancel();
                    },
                    (dialog, which) -> {
                        // Neutral Button
                        dialog.cancel();
                    },
                    false,
                    false);
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Preferences.clearDataUpdateDialog(context);
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}