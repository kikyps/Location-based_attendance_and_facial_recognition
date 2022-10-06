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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.absensi.inuraini.common.EmailVerificationActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
    private static long mLastClickTime = 0;
    public static AlertDialog myAlertDialog;
    public static boolean start = true;

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

    public static void setProgressDialog(){
        progressDialog.setMessage("Proses...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void emailAndPasswordLogin(Context context, String email, String password, Class activity) {
        setProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser().isEmailVerified()){
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(context, activity);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Email anda belum di verifikasi silahkan verifikasi email anda", Toast.LENGTH_LONG).show();
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){
                                            Intent intent = new Intent(context, EmailVerificationActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        }
                                    });
                        }
                        progressDialog.dismiss();
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        Toast.makeText(context, "Email atau password salah", Toast.LENGTH_SHORT).show();
                    }
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
                                       context.startActivity(intent);
                                   }
                               });
                       progressDialog.dismiss();
                   } else {
                       progressDialog.dismiss();
                       Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                   }
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
                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Password lama anda salah!", Toast.LENGTH_LONG).show();
                    }
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
                .requestIdToken(base64decode(web_id))
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
                        progressDialog.dismiss();
                        Intent intent = new Intent(context, activity);
                        context.startActivity(intent);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Login Gagal!, Periksa koneksi internet dan coba lagi.", Toast.LENGTH_SHORT).show();
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
                        context.startActivity(new Intent(Settings.ACTION_DATA_USAGE_SETTINGS));
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
                false);
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
                        showDialog(context, null, "Pembaruan Aplikasi", deskripsi, "Update", null, "Ingat nanti",
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
                                true);
                    }
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
}
