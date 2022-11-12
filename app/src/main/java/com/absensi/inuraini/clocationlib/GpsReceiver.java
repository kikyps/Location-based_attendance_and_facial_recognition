package com.absensi.inuraini.clocationlib;

import com.absensi.inuraini.dexter.MultiplePermissionsReport;
import com.absensi.inuraini.dexter.PermissionToken;
import com.absensi.inuraini.dexter.listener.PermissionRequest;
import com.absensi.inuraini.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class GpsReceiver implements MultiplePermissionsListener {

    private final GetLocation activity;

    public GpsReceiver(GetLocation activity) {
        this.activity = activity;
    }

    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

    }

    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                             PermissionToken token) {

    }
}