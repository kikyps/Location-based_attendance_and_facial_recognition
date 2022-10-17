package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.absensi.inuraini.R;

public class AntiMockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_mock);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}