package com.absensi.inuraini.clocationlib;

import android.location.Address;
import android.location.Location;

public interface OnMyLocation {
    void onCurrentLocation(Location location, Address address);

    void onFailed(int errorCode, String msg);
}
