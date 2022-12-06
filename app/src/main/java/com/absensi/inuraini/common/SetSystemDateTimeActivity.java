package com.absensi.inuraini.common;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.absensi.inuraini.R;

public class SetSystemDateTimeActivity extends AppCompatActivity {

    Button goSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_system_date_time);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        contentListeners();
    }

    private void contentListeners() {
        goSettings = findViewById(R.id.go_setting_time);

        goSettings.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
            finish();
        });
    }
}