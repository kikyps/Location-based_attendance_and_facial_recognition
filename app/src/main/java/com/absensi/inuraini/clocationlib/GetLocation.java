package com.absensi.inuraini.clocationlib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.absensi.inuraini.common.AntiMockActivity;
import com.absensi.inuraini.dexter.Dexter;
import com.absensi.inuraini.dexter.MultiplePermissionsReport;
import com.absensi.inuraini.dexter.PermissionToken;
import com.absensi.inuraini.dexter.listener.PermissionRequest;
import com.absensi.inuraini.dexter.listener.multi.MultiplePermissionsListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

/**
 * Created by KiKy_ps on 2022/11/09.
 * psrifki12@gmail.com
 */
public class GetLocation {

    private OnMyLocation onMyLocation;
    LocationRequest locationRequest;
    int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private boolean isMockProvider = true;
    Context context;
    Activity activity;

    static Location setLocation;
    static Address setAddress;

    boolean broadcastTriggered = true;

    public GetLocation(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void setAcceptMockProvider(boolean isMockProvider) {
        this.isMockProvider = isMockProvider;
    }

    public void getMyLocation(com.absensi.inuraini.clocationlib.OnMyLocation onMyLocation) {
        this.onMyLocation = onMyLocation;
        setMyLocation();
    }

    // GetLocation
    private void setMyLocation() {
        try {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setWaitForAccurateLocation(true)
                    .setMinUpdateIntervalMillis(2000)
                    .setMaxUpdateDelayMillis(5000)
                    .build();
            Dexter.withContext(context)
                    .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                getLokasi();
                            } else {
                                Snackbar.make(activity.findViewById(android.R.id.content), "Akses lokasi di perlukan", Snackbar.LENGTH_LONG).setDuration(10000).setAction("Pengaturan", v -> {
                                    goToAppSettings();
                                }).show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } catch (Exception e) {
            if (onMyLocation != null) {
                onMyLocation.onFailed(e.hashCode(), e.getMessage());
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLokasi() {
        if (isGPSEnabled()) {
            LocationServices.getFusedLocationProviderClient(context)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(context)
                                    .removeLocationUpdates(this);

                            if (locationResult.getLocations().size() > 0) {

                                Location location = locationResult.getLastLocation();

                                if (!isMockProvider) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        if (location.isMock()) {
                                            Intent intent = new Intent(context, AntiMockActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        } else {
                                            fetchLocation(location);
                                        }
                                    } else {
                                        if (location.isFromMockProvider()) {
                                            Intent intent = new Intent(context, AntiMockActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        } else {
                                            fetchLocation(location);
                                        }
                                    }
                                } else {
                                    fetchLocation(location);
                                }
                            }
                        }
                    }, Looper.getMainLooper());
        } else {
            turnOnGPS();
        }
    }

    private void turnOnGPS() {
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        activity.registerReceiver(locationSwitchStateReceiver, filter);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
//                Toast.makeText(context, "GPS sudah aktif", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(activity, REQUEST_CODE_LOCATION_PERMISSION);
                            Intent intent = activity.getIntent();
                            activity.setResult(Activity.RESULT_OK, intent);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Device does not have location
                        break;
                }
            }
        });
    }

    public boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void fetchLocation(Location location) {
        if (onMyLocation != null) {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    onMyLocation.onCurrentLocation(location, address);
                    setLocation = location;
                    setAddress = address;
                    activity.unregisterReceiver(locationSwitchStateReceiver);
                }
            } catch (Exception e) {
                if (onMyLocation != null) {
                    onMyLocation.onFailed(e.hashCode(), e.getMessage());
                }
            }
        }
    }

    private void goToAppSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myAppSettings);
    }

    private final BroadcastReceiver locationSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (broadcastTriggered) {
                broadcastTriggered = false;
                if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (isGpsEnabled || isNetworkEnabled) {
                        //Gps is enabled
                        setMyLocation();
                    } else {
                        //Gps is disabled
                        setMyLocation();
//                        activity.unregisterReceiver(locationSwitchStateReceiver);
                    }
                }
            }
        }
    };

    public static Location MyLocation() {
        return setLocation;
    }

    public static Address MyAddress() {
        return setAddress;
    }
}
