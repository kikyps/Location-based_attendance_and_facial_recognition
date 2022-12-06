package com.absensi.inuraini.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.absensi.inuraini.GetServerTime;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.camera.CameraActivity;
import com.absensi.inuraini.common.DataDiriOne;
import com.absensi.inuraini.common.DoVerifActivity;
import com.absensi.inuraini.common.LoginActivity;
import com.absensi.inuraini.common.SetSystemDateTimeActivity;
import com.absensi.inuraini.user.absen.AbsenFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce;
    private AppBarConfiguration mAppBarConfiguration;
    NavigationView navigationView;
    Dialog progressDialog;
    TextView nama;
    LinearLayout infouser;
    Context context = this;
    public static boolean firstExit = false;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
    DateFormat dateNow = new SimpleDateFormat("MM/dd/yyyy");
    DateFormat jamNow = new SimpleDateFormat("HH:mm");
    DateFormat jamSec = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Preferences.clearDataUpdateDialog(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_akun,
                R.id.nav_absen,
                R.id.nav_pengajuan_libur,
                R.id.nav_dashboard,
                R.id.nav_rekap_absen,
                R.id.nav_data_pengajuan,
                R.id.nav_lokasi,
                R.id.nav_jabatan,
                R.id.nav_data_pegawai,
                R.id.nav_verif_akun)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        contentListeners(headerView);
    }

    private void contentListeners(View view){
        nama = view.findViewById(R.id.name_txt);
        infouser = view.findViewById(R.id.info_user);
        boolean getValidity = getIntent().getBooleanExtra("validCtx", false);
        if (!getValidity){
            finishAndRemoveTask();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signout){
            onSignOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sign Out")
                        .setMessage("Apakah anda yakin ingin keluar?")
                                .setPositiveButton("Iya", (dialog, which) -> {
                                    Preferences.signOut(context, firstExit, LoginActivity.class);
                                    finish();
                                })
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readUserData();
    }

    private void showMyProgresDialog(){
        hideMyProgresDialog();
        progressDialog = Preferences.customProgresBar(context);
    }

    private void hideMyProgresDialog(){
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideMyProgresDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideMyProgresDialog();
    }

    private void readUserData() {
        if (!Preferences.isConnected(context)) {
            Preferences.dialogNetwork(context);
        } else {
            showMyProgresDialog();
            String curentSec = jamSec.format(new Date().getTime());
            String close = curentSec.substring(6, 8);

            if (close.equals("58") || close.equals("59")) {
                Runnable runnable = this::getData;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(runnable, 2500);
            } else {
                Runnable runnable = this::getData;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(runnable, 1000);
            }
        }
    }

    private void getData(){
        GetServerTime serverTime = new GetServerTime(this);
        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean nohp = snapshot.hasChild("sPhone");
                    boolean checkVerif = (boolean) snapshot.child("sVerified").getValue();
                    String myId = snapshot.child("faceID").getValue(String.class);
                    String namaku = snapshot.child("sNama").getValue(String.class);
                    boolean wajahid = snapshot.hasChild("faceID");
                    String getstatus = snapshot.child("sStatus").getValue(String.class);

                    nama.setText(namaku);

                    serverTime.getDateTime((date, time) -> {
                        String curentDate = dateNow.format(new Date().getTime());
                        String curentTime = jamNow.format(new Date().getTime());

                        if (date.equals(curentDate) && time.equals(curentTime)) {
                            if (!nohp) {
                                Intent intent = new Intent(context, DataDiriOne.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            } else {
                                if (!checkVerif) {
                                    startActivity(new Intent(context, DoVerifActivity.class));
                                    finish();
                                } else if (!wajahid) {
                                    Intent intent = new Intent(context, CameraActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("faceid", true);
                                    startActivity(intent);
                                } else {
                                    if (!Preferences.getUpdateDialog(context)) {
                                        Preferences.checkUpdate(context, UserActivity.this);
                                    }
                                    hideMyProgresDialog();
                                }
                            }
                        } else {
                            Intent intent = new Intent(UserActivity.this, SetSystemDateTimeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }
                    });

                    if (myId != null) {
                        Preferences.setFaceId(context, myId);
                    }

                    if (getstatus != null) {
                        if (Preferences.getDataStatus(context).isEmpty()) {
                            Preferences.setDataStatus(context, getstatus);
                        }
                    }

                    if (Preferences.getDataStatus(context).equals(Preferences.retriveSec("yVGdzFWb"))) {
                        Menu menu = navigationView.getMenu();
                        MenuItem nav_admin = menu.findItem(R.id.admin_akses);
                        MenuItem nav_user = menu.findItem(R.id.pengajuanUser);
                        nav_user.setVisible(false);
                        nav_admin.setVisible(true);
                    } else if (Preferences.getDataStatus(context).equals("admin")){
                        Menu menu = navigationView.getMenu();
                        MenuItem nav_admin = menu.findItem(R.id.admin_akses);
                        MenuItem nav_user = menu.findItem(R.id.pengajuanUser);
                        nav_user.setVisible(false);
                        nav_admin.setVisible(true);
                    } else {
                        Menu menu = navigationView.getMenu();
                        MenuItem nav_dashboard = menu.findItem(R.id.admin_akses);
                        MenuItem nav_user = menu.findItem(R.id.pengajuanUser);
                        nav_user.setVisible(true);
                        nav_dashboard.setVisible(false);
                    }

                } else {
                    Intent intent = new Intent(context, DataDiriOne.class);
                    intent.putExtra("faceid", true);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Preferences.REQUEST_PERMISSION_CODE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()){
//                    Toast.makeText(this, "Permission granted in android 11 and above", Toast.LENGTH_SHORT).show();
                    Preferences.downloadUpdate(context);
                }
            }
        }

        if (requestCode == Preferences.REQUEST_CODE_LOCATION_PERMISSION){
            if (resultCode == RESULT_OK){
                Preferences.getMyLocation(context, UserActivity.this);
            } else {
                AbsenFragment.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(context, "Aktifkan GPS untuk absen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Preferences.REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writeExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (readExternalStorage && writeExternalStorage) {
//                    Toast.makeText(this, "Permission granted in android 10 or below", Toast.LENGTH_SHORT).show();
                    Preferences.downloadUpdate(context);
                }
            }
        }

        if (requestCode == Preferences.REQUEST_CODE_LOCATION_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Preferences.getMyLocation(context, this);
            } else {
                AbsenFragment.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(context, "Izinkan akses lokasi untuk absen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            setResult(Activity.RESULT_CANCELED);
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}