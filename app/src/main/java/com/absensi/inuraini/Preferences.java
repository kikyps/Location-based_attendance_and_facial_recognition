package com.absensi.inuraini;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.admin.location.SettingsLocation;
import com.absensi.inuraini.admin.location.maps.MapsActivity;
import com.absensi.inuraini.camera.SimilarityClassifier;
import com.absensi.inuraini.common.AntiMockActivity;
import com.absensi.inuraini.common.EmailVerificationActivity;
import com.absensi.inuraini.user.absen.AbsenFragment;
import com.github.clans.fab.BuildConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preferences {
    public static FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static ProgressDialog progressDialog;
    public static StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    public static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    public static GoogleSignInOptions gso;
    public static GoogleSignInClient gsc;
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static final int RC_SIGN_IN = 1001;
    private static final String
            FACE_ID = "face_id",
            DATA_STATUS = "status",
            DATA_DIALOG = "dialog_show",
            MAPS_GUIDE = "maps_guide";
    public static final int REQUEST_PERMISSION_CODE = 111;
    private static long mLastClickTime = 0;
    public static AlertDialog myAlertDialog;
    public static boolean start = true;
    public static LocationRequest locationRequest;
    public static double latitude;
    public static double longitude;
    public static String[] myAddress = new String[13];
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setDataStatus(Context context, String data){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(DATA_STATUS,data);
        editor.apply();
    }

    public static String getDataStatus(Context context){
        return getSharedPreferences(context).getString(DATA_STATUS,"");
    }

    public static void setFaceId(Context context, String data){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(FACE_ID, data);
        editor.apply();
    }

    public static String getFaceId(Context context){
        String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        return getSharedPreferences(context).getString(FACE_ID, defValue);
    }

    public static void setUpdateDialog(Context context, boolean status){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(DATA_DIALOG,status);
        editor.apply();
    }

    public static boolean getUpdateDialog(Context context){
        return getSharedPreferences(context).getBoolean(DATA_DIALOG,false);
    }

    public static void setMapsGuide(Context context, boolean data){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(MAPS_GUIDE, data);
        editor.apply();
    }

    public static boolean getMapsGuide(Context context){
        return getSharedPreferences(context).getBoolean(MAPS_GUIDE,false);
    }

    public static void clearData(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(FACE_ID);
        editor.remove(DATA_STATUS);
        editor.remove(DATA_DIALOG);
        editor.remove(MAPS_GUIDE);
        editor.apply();
    }

    public static void clearDataUpdateDialog(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(DATA_DIALOG);
        editor.apply();
    }

    public static String retriveSec(String str) {
        try {
            return new String(Base64.decode(new StringBuffer(str).reverse().toString(), 0), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException unused) {
            return "This is not a base64 data";
        }
    }

    public static void emailAndPasswordLogin(Context context, String email, String password, Class activity) {
        setProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(context, activity);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Email anda belum di verifikasi silahkan verifikasi email anda", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, EmailVerificationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("emailReg", email);
                            intent.putExtra("reVerif", true);
                            context.startActivity(intent);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(context, "Email atau password salah", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                });
    }

    public static void emailAndPasswordRegister(Context context, String email, String password, Class activity){
        setProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       mAuth.getCurrentUser().sendEmailVerification()
                               .addOnCompleteListener(task1 -> {
                                   if (task1.isSuccessful()){
                                       Intent intent = new Intent(context, activity);
                                       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                               Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                               Intent.FLAG_ACTIVITY_NEW_TASK);
                                       intent.putExtra("emailReg", email);
                                       intent.putExtra("reVerif", false);
                                       context.startActivity(intent);
                                   }
                               });
                   } else {
                       Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                   }
                    progressDialog.dismiss();
                });
    }

    public static void tryChangePassword(Context context, FirebaseUser user, String passLama, String passBaru){
        setProgressDialog();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), passLama);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        user.updatePassword(passBaru)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(context, "Password anda berhasil di ubah", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Password lama anda salah!", Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                });
    }

    public static void resetLoginPassword(Context context, String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Email reset password sudah di kirim, check email anda segera!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static void googleInitialize(Context context){
        String web_id = "t92YuQnblRnbvNmclNXdlx2Zv92ZuMHcwFmLx0mZuFDMhhmMmdmYuVWdvBTcjJTO2QjYx52ZvdDc0ATLzgDNwMDM4QDNzYjM";
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(retriveSec(web_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(context, gso);
    }

    public static void firebaseAuthWithGoogle(String idToken, Context context, Class activity) {
        setProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(context, activity);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Login Gagal!, Periksa koneksi internet dan coba lagi.", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                });
    }

    public static void signOut(Context context, Boolean start, Class activity){
        mAuth.signOut();
        gsc.signOut();
        clearData(context);
        if (start) {
            Intent intent = new Intent(context, activity);
            intent.putExtra("validCtx", true);
            intent.putExtra("relog", true);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, activity);
            intent.putExtra("validCtx", true);
            context.startActivity(intent);
        }
    }

    public static void doRestart(Context c) {
        try {
            // check if the context is given
            if (c != null) {
                // fetch the package manager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                // check if we got the PackageManager
                if (pm != null) {
                    // create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(c.getPackageName());
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        c.getApplicationContext().startActivity(mStartActivity);
                        // kill the application
                        System.exit(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isPermissionGranted(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // For Android 11 (R)
            return Environment.isExternalStorageManager();
        } else {
            // For Below
            int readExternalStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoreagePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readExternalStoragePermission == PackageManager.PERMISSION_GRANTED && writeExternalStoreagePermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void takePermissions(Context context, Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
                activity.startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            } catch (Exception exception){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    public static void checkUpdate(Context context, Activity activity){
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                final String new_version_code = remoteConfig.getString("new_version_code");
                if (Integer.parseInt(new_version_code) > getCurrentVersionCode(context)){
                    showUpdateDialog(context, activity);
                }
            }
        });
    }

    public static void setProgressDialog(){
        progressDialog.setMessage("Sedang Memproses...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static Dialog customProgresBar(Context context){
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.custom_progress_bar);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if ((context instanceof AppCompatActivity && !((AppCompatActivity) context).isFinishing()) && !dialog.isShowing()) {
            dialog.show();
        }
        return dialog;
    }

    public static String tgglFormatId (String str)
    {
        // Check if the string has only
        // one character then return
        // the string
        if (str.length() < 2)
            return str;

        // Concatenate last character
        // and first character between
        // middle characters of string
        return (str.substring(3, 5)
                + str.substring(0, 2)
                + str.substring(6, 10));
    }

    public static String getOnlyDigits(String s) {
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("");
    }

    public static String getOnlyStrings(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z]");
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("");
    }

    public static void dialogNetwork(Context context) {
        showDialog(context,
                null,
                "No Internet!",
                "Aplikasi ini membutuhkan akses internet\nAktifkan wifi atau mobile data anda terlebih dahulu!!",
                "Okey",
                null,
                null,
                (dialog, which) -> {
                    // Positive Button
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        try {
                            context.startActivity(new Intent(Settings.ACTION_DATA_USAGE_SETTINGS));
                        } catch (Exception e){
                            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }
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
                true);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
    }

    public static void showUpdateDialog(Context context, Activity activity){
        databaseReference.child("data").child("updateURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deskripsi = snapshot.child("sDescription").getValue().toString();

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if (start) {
                        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                                .setMinimumFetchIntervalInSeconds(5)
                                .build();
                        remoteConfig.setConfigSettingsAsync(configSettings);
                        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                final boolean dialog_update_cancelable = remoteConfig.getBoolean("dialog_update_cancelable");
                                if (dialog_update_cancelable) {
                                    showDialog(context,
                                            null,
                                            "Pembaruan Aplikasi",
                                            deskripsi,
                                            "Update",
                                            null,
                                            "Ingat nanti",
                                            (dialog, which) -> {
                                                start = false;
                                                if (!isPermissionGranted(context)) {
                                                    takePermissions(context, activity);
                                                } else {
                                                    downloadUpdate(context);
                                                }
                                            },
                                            (dialog, which) -> dialog.cancel(),
                                            (dialog, which) -> setUpdateDialog(context, true),
                                            true,
                                            true);
                                } else {
                                    showDialog(context,
                                            null,
                                            "Pembaruan Aplikasi",
                                            deskripsi,
                                            "Update",
                                            null,
                                            null,
                                            (dialog, which) -> {
                                                start = false;
                                                if (!isPermissionGranted(context)) {
                                                    takePermissions(context, activity);
                                                } else {
                                                    downloadUpdate(context);
                                                }
                                            },
                                            (dialog, which) -> dialog.cancel(),
                                            (dialog, which) -> setUpdateDialog(context, true),
                                            false,
                                            true);
                                }
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static Object[][] getMyLocation(Context context, Activity activity) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled(context)) {
                    LocationServices.getFusedLocationProviderClient(context)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(context)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() > 0){

                                        int index = locationResult.getLocations().size() - 1;
                                        latitude = locationResult.getLocations().get(index).getLatitude();
                                        longitude = locationResult.getLocations().get(index).getLongitude();

                                        myAddress = getAddressFromLocation(context, latitude, longitude);

                                        Location location = locationResult.getLastLocation();

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            if (location.isMock()){
                                                Intent intent = new Intent(context, AntiMockActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent);
                                            } else {
                                                if (AbsenFragment.doAbsen) {
                                                    AbsenFragment.checkAbsenKantor();
                                                } else if (AbsenFragment.doAbsenKeluar){
                                                    AbsenFragment.checkAbsenKeluar();
                                                } else if (SettingsLocation.setloc){
                                                    SettingsLocation.updateLatLong();
                                                } else if (SettingsLocation.getMaps){
                                                    Intent intent = new Intent(context, MapsActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    context.startActivity(intent);
                                                }
                                                AbsenFragment.doAbsen = false;
                                                AbsenFragment.doAbsenKeluar = false;
                                                SettingsLocation.setloc = false;
                                                SettingsLocation.getMaps = false;
                                            }
                                        } else {
                                            if (location.isFromMockProvider()){
                                                Intent intent = new Intent(context, AntiMockActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent);
                                            } else {
                                                if (AbsenFragment.doAbsen) {
                                                    AbsenFragment.checkAbsenKantor();
                                                } else if (AbsenFragment.doAbsenKeluar){
                                                    AbsenFragment.checkAbsenKeluar();
                                                } else if (SettingsLocation.setloc){
                                                    SettingsLocation.updateLatLong();
                                                } else if (SettingsLocation.getMaps){
                                                    Intent intent = new Intent(context, MapsActivity.class);
                                                    context.startActivity(intent);
                                                }
                                                AbsenFragment.doAbsen = false;
                                                AbsenFragment.doAbsenKeluar = false;
                                                SettingsLocation.setloc = false;
                                                SettingsLocation.getMaps = false;
                                            }
                                        }
                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    turnOnGPS(context, activity);
                }
            } else {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            }
        } else {
            /// for android lollipop and below
            if (isGPSEnabled(context)) {
                LocationServices.getFusedLocationProviderClient(context)
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(context)
                                        .removeLocationUpdates(this);

                                if (locationResult != null && locationResult.getLocations().size() > 0){

                                    int index = locationResult.getLocations().size() - 1;
                                    latitude = locationResult.getLocations().get(index).getLatitude();
                                    longitude = locationResult.getLocations().get(index).getLongitude();

                                    myAddress = getAddressFromLocation(context, latitude, longitude);

                                    Location location = locationResult.getLastLocation();

                                    if (location.isFromMockProvider()){
                                        Intent intent = new Intent(context, AntiMockActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    } else {
                                        if (AbsenFragment.doAbsen) {
                                            AbsenFragment.checkAbsenKantor();
                                        } else if (AbsenFragment.doAbsenKeluar){
                                            AbsenFragment.checkAbsenKeluar();
                                        } else if (SettingsLocation.setloc){
                                            SettingsLocation.updateLatLong();
                                        }
                                        AbsenFragment.doAbsen = false;
                                        AbsenFragment.doAbsenKeluar = false;
                                        SettingsLocation.setloc = false;
                                    }
                                }
                            }
                        }, Looper.getMainLooper());
            } else {
                turnOnGPS(context, activity);
            }
        }
        return new Object[][] {{latitude, longitude}, {myAddress[0], myAddress[1], myAddress[2], myAddress[3], myAddress[4], myAddress[5], myAddress[6], myAddress[7], myAddress[8], myAddress[9], myAddress[10], myAddress[11], myAddress[12]}};
    }

    public static String[] getAddressFromLocation(Context context, final double latitude, final double longitude) {
        String getAddressLine = "",         // index 0
                getAdminArea = "",          // index 1
                getCuntryCode = "",         // index 2
                getCountryName = "",        // index 3
                getFeatureName = "",        // index 4
                getLocality = "",           // index 5
                getPostalCode = "",         // index 6
                getPremises = "",           // index 7
                getSubAdminArea = "",       // index 8
                getSubLocality = "",        // index 9
                getSubThorughfare = "",     // index 10
                getThoroughfare = "",       // index 11
                getSpecificAddress = "";    // index 12

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                if(address.getAddressLine(0) != null && address.getAddressLine(0).length() > 0 && !address.getAddressLine(0).contentEquals("null")) {
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        getAddressLine = address.getAddressLine(i);
                    }
                    getAdminArea = address.getAdminArea();
                    getCuntryCode = address.getCountryCode();
                    getCountryName = address.getCountryName();
                    getFeatureName = address.getFeatureName();
                    getLocality = address.getLocality();
                    getPostalCode =  address.getPostalCode();
                    getPremises = address.getPremises();
                    getSubAdminArea = address.getSubAdminArea();
                    getSubLocality = address.getSubLocality();
                    getSubThorughfare = address.getSubThoroughfare();
                    getThoroughfare = address.getThoroughfare();
                    if (getThoroughfare != null) {
                        getSpecificAddress = replaceCharIfContains(getThoroughfare, "Jalan", "JL.") + ", " + getSubAdminArea + ", Prov. " + getAdminArea + ", " + getCountryName;
                    } else {
                        getSpecificAddress = "Nama jalan tidak di ketahui!" + ", " + getSubAdminArea + ", Prov. " + getAdminArea + ", " + getCountryName;
                    }
                } else {
                    sb.append(address.getLocality()).append(", ");
                    sb.append(address.getPostalCode()).append(", ");
                    sb.append(address.getCountryName());
                    getAddressLine = sb.toString();
                }
                //Log.e("leaddress","@"+straddress);
            }
        } catch (IOException e) {
            //Log.e(TAG, "Unable connect to Geocoder", e);
        }
        return new String[]{getAddressLine, getAdminArea, getCuntryCode, getCountryName, getFeatureName, getLocality,
                getPostalCode, getPremises, getSubAdminArea, getSubLocality, getSubThorughfare, getThoroughfare, getSpecificAddress};
    }

    public static String replaceCharIfContains(String args, String target, String replacement) {
        String someValue = args;
        if (args != null && args.contains(target)) {
            someValue = args.replace(target, replacement);
        }
        return someValue;
    }

    public static String replaceCharWhileContains(String args, String target, String replacement) {
        String someValue = args;
        while (args.contains(target)) {
            someValue = args.replace(target, replacement);
        }
        return someValue;
    }

    public static void turnOnGPS(Context context, Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                Toast.makeText(context, "GPS sudah aktif", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(activity, REQUEST_CODE_LOCATION_PERMISSION);
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

    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void showDialog(Context context,
                                  Drawable icon,
                                  String title,
                                  String message,
                                  String positivText,
                                  String negativeText,
                                  String neutralText,
                                  DialogInterface.OnClickListener posisitvClick,
                                  DialogInterface.OnClickListener negativClick,
                                  DialogInterface.OnClickListener neutralClick,
                                  boolean cancelable,
                                  boolean returnValue) {
        if(myAlertDialog != null && myAlertDialog.isShowing()) if (returnValue) return;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.my_dialog_theme);
        dialog.setIcon(icon);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(positivText, posisitvClick);
        dialog.setNegativeButton(negativeText, negativClick);
        dialog.setNeutralButton(neutralText, neutralClick);
        dialog.setCancelable(cancelable);
        myAlertDialog = dialog.create();
        if ((context instanceof AppCompatActivity && !((AppCompatActivity) context).isFinishing()) && !myAlertDialog.isShowing()) {
            myAlertDialog.show();
        }
    }

    public static void downloadUpdate(Context context) {
        StorageReference reference = storageReference.child("app-release.apk");
        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            String urlUpdate = uri.toString();
            new DownloadTask(context).execute(urlUpdate);
        }).addOnFailureListener(e -> {
            Toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public static int getCurrentVersionCode(Context context){
        PackageInfo packageInfo = new PackageInfo();
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e){
            e.printStackTrace();
        }
        return packageInfo.versionCode;
    }

    public static boolean isMockLocationEnabled(Context context) {
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID)== AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(context.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }
        return isMockLocation;
    }

    public static long getApkSize(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        return new File(context.getPackageManager().getApplicationInfo(
                packageName, 0).publicSourceDir).length();
    }

    public static boolean areThereMockPermissionApps(Context context, boolean enable) {
        if (enable) {
            int count = 0;
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> packages =
                        pm.getInstalledApplications(PackageManager.GET_META_DATA);

                for (ApplicationInfo applicationInfo : packages) {
                    try {
                        PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                                PackageManager.GET_PERMISSIONS);
                        // Get Permissions
                        String[] requestedPermissions = packageInfo.requestedPermissions;

                        if (requestedPermissions != null) {
                            for (String requestedPermission : requestedPermissions) {
                                if (requestedPermission
                                        .equals("android.permission.ACCESS_MOCK_LOCATION")
                                        && !applicationInfo.packageName.equals(context.getPackageName())) {
                                    count++;
                                }
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
//                    Log.e("MockDeductionAgilanbu", "Got exception --- " + e.getMessage());
                    }
                }
            } catch (Exception w) {
                w.printStackTrace();
            }
            return count > 0;
        } else {
            return false;
        }
    }
}

