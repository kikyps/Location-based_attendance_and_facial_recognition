package com.absensi.inuraini.common;

import android.app.Application;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class Utils extends Application {

    public void onCreate() {
        super.onCreate();
        CaocConfig.Builder.create()
                .errorActivity(CollectorActivity.class)
                .apply();
    }
}
