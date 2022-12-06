package com.absensi.inuraini.user.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.absensi.inuraini.GetServerTime;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.camera.CameraActivity;
import com.absensi.inuraini.common.DataDiriOne;
import com.absensi.inuraini.common.DoVerifActivity;
import com.absensi.inuraini.common.LoginActivity;
import com.absensi.inuraini.common.SetSystemDateTimeActivity;
import com.absensi.inuraini.meowbottomnavigation.MeowBottomNavigation;
import com.absensi.inuraini.user.absen.AbsenFragment;
import com.absensi.inuraini.user.account.AccountInfo;
import com.absensi.inuraini.user.pengajuan.PengajuanLibur;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeActivityUser extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce;
    private final int ID_LIBUR = 1;
    private final int ID_ABSEN = 2;
    private final int ID_AKUN = 3;
    public static boolean firstExit = false;

    Context context = this;
    Dialog progressDialog;
    DateFormat dateNow = new SimpleDateFormat("MM/dd/yyyy");
    DateFormat jamNow = new SimpleDateFormat("HH:mm");
    DateFormat jamSec = new SimpleDateFormat("HH:mm:ss");

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        firebaseUser = Preferences.mAuth.getCurrentUser();

        boolean getValidity = getIntent().getBooleanExtra("validCtx", false);
        if (!getValidity){
            finishAndRemoveTask();
        }

        MeowBottomNavigation bottomNavigation = findViewById(R.id.myNavigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_LIBUR, R.drawable.ic_pengajuan_libur));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ABSEN, R.drawable.ic_absen));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_AKUN, R.drawable.ic_baseline_account_circle_24));

        bottomNavigation.setOnClickMenuListener(item -> {

        });

        bottomNavigation.setOnShowListener(item -> {
            switch (item.getId()){
                case ID_LIBUR:
                    replaceFragment(new PengajuanLibur());
                    toolbar.setTitle("Pengajuan Libur");
                    break;
                case ID_ABSEN:
                    replaceFragment(new AbsenFragment());
                    toolbar.setTitle("Absensi");
                    break;
                case ID_AKUN:
                    replaceFragment(new AccountInfo());
                    toolbar.setTitle("Akun Saya");
                    break;
            }
        });

        bottomNavigation.setOnReselectListener(item -> {

        });

        bottomNavigation.show(ID_ABSEN, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user, menu);
        return super.onCreateOptionsMenu(menu);
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
                    String getstatus = snapshot.child("sStatus").getValue(String.class);
                    boolean wajahid = snapshot.hasChild("faceID");
                    boolean trial = snapshot.hasChild("sTrial");

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
                                        Preferences.checkUpdate(context, HomeActivityUser.this);
                                    }
                                    hideMyProgresDialog();
                                }
                            }
                        } else {
                            Intent intent = new Intent(HomeActivityUser.this, SetSystemDateTimeActivity.class);
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

                    if (!trial){
                        Map<String, Object> postValues = new HashMap<>();
                        postValues.put("sTrial", 0);
                        databaseReference.child(firebaseUser.getUid()).updateChildren(postValues);
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

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.user_frame, fragment);
        fragmentTransaction.commit();
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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}