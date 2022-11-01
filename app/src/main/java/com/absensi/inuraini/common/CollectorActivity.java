package com.absensi.inuraini.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.absensi.inuraini.MyLongClickListener;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CollectorActivity extends AppCompatActivity {

    TextView error;
    Button restart, update;
    boolean doubleBackToExitPressedOnce;
    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        error = findViewById(R.id.error_text);
        restart = findViewById(R.id.restart_app);
        update = findViewById(R.id.update_app);
        error.setText(CustomActivityOnCrash.getAllErrorDetailsFromIntent(getApplicationContext(), getIntent()));
        onClick();
    }

    private void onClick() {
        restart.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());
            CustomActivityOnCrash.restartApplicationWithIntent(this, intent, config);
        });

        restart.setOnTouchListener(new MyLongClickListener(6000) {
            @Override
            public void onLongClick() {
                Preferences.signOut(context, true, SplashScreenActivity.class);
                finish();
            }
        });

        update.setOnClickListener(v -> {
            if (!Preferences.isConnected(context)){
                Preferences.dialogNetwork(context);
            } else if (!Preferences.getUpdateDialog(context)){
                checkUpdate(context);
            }
        });
    }

    private void checkUpdate(Context context){
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                final String new_version_code = remoteConfig.getString("new_version_code");
                if (Integer.parseInt(new_version_code) > Preferences.getCurrentVersionCode(context)){
                    Preferences.showUpdateDialog(context, this);
                } else {
                    Toast.makeText(this, "Sudah versi terbaru!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.error_copy, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.copy_log) {
            copyErrorLog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyErrorLog() {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, error.getText().toString());
        if (cm == null) return;
        cm.setPrimaryClip(clip);
//        try {
//            CharSequence textToPaste = cm.getPrimaryClip().getItemAt(0).getText();
//            Toast.makeText(context, textToPaste, Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            return;
//        }
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Preferences.clearDataUpdateDialog(this);
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Double-click untuk keluar", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}