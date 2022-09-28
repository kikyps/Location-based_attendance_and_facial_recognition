package com.absensi.inuraini;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.camera.SimilarityClassifier;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Preferences {
    public static int currentVersionCode;
    public static FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    public static ProgressDialog progressDialog;
    public static StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    public static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    public static GoogleSignInOptions gso;
    public static GoogleSignInClient gsc;
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static final int RC_SIGN_IN = 1001;
    private static final String FACE_ID = "face_id",
            DATA_STATUS = "status", DATA_DIALOG = "dialog_show";
    public static final int REQUEST_PERMISSION_CODE = 111;
    public static FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private static long mLastClickTime = 0;
    public static AlertDialog myAlertDialog;

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

    public static void clearData(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(FACE_ID);
        editor.remove(DATA_STATUS);
        editor.remove(DATA_DIALOG);
        editor.apply();
    }

    public static void clearDataUpdateDialog(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(DATA_DIALOG);
        editor.apply();
    }

    public static String base64decode(String str) {
        try {
            return new String(Base64.decode(new StringBuffer(str).reverse().toString(), 0), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException unused) {
            return "This is not a base64 data";
        }
    }

    public static void googleInitialize(Context context){
        String web_id = "t92YuQnblRnbvNmclNXdlx2Zv92ZuMHcwFmLx0mZuFDMhhmMmdmYuVWdvBTcjJTO2QjYx52ZvdDc0ATLzgDNwMDM4QDNzYjM";
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(base64decode(web_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(context, gso);
    }

    public static void firebaseAuthWithGoogle(String idToken, Context context, Class activity) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(context, activity);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Login Gagal!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void signOut(Context context, Class activity){
        mAuth.signOut();
        gsc.signOut();
        clearData(context);
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
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
        currentVersionCode = getCurrentVersionCode(context);
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

    public static void customProgresBar(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.cutom_progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private static void showUpdateDialog(Context context, Activity activity){
        databaseReference.child("data").child("updateURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deskripsi = snapshot.child("sDescription").getValue().toString();

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    showDialog(context, null, "Pembaruan Aplikasi", deskripsi, "Update", null, "Ingat nanti",
                            (dialog, which) -> {
                                if (!isPermissionGranted(context)) {
                                    takePermissions(context, activity);
                                } else {
                                    downloadUpdate(context);
                                }
                            },
                            (dialog, which) -> dialog.cancel(),
                            (dialog, which) -> setUpdateDialog(context, true),
                            true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                                  boolean cancelable) {

        if(myAlertDialog != null && myAlertDialog.isShowing()) return;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.my_dialog_theme);
        dialog.setIcon(icon);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(positivText, posisitvClick);
        dialog.setNegativeButton(negativeText, negativClick);
        dialog.setNeutralButton(neutralText, neutralClick);
        dialog.setCancelable(cancelable);
        myAlertDialog = dialog.create();
        myAlertDialog.show();
    }

    public static void downloadUpdate(Context context) {
        currentVersionCode = getCurrentVersionCode(context);
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        StorageReference reference = storageReference.child("app-release.apk");
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String urlUpdate = uri.toString();
                    new DownloadTask(context).execute(urlUpdate);
                }).addOnFailureListener(e -> {
                    Toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
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
}
