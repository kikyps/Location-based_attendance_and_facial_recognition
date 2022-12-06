package com.absensi.inuraini.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.mytools.TamperingProtection;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        // Make call to execute AsycTasks<> here
        // This helps avoid the extra step of clicking on a button
        // to take you to the MainActivity
        boolean restart = getIntent().getBooleanExtra("relog", false);
        if (restart) {
            Preferences.doRestart(this);
        } else {
            new StartMainActivity().execute(this);
        }

//        new Handler().postDelayed(this::finish, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private class StartMainActivity extends AsyncTask<Context, Void, Intent> {

        Context ctx;

        @Override
        protected Intent doInBackground(Context... params) {
            ctx = params[0];
            try {
                String ourcrc = String.valueOf(TamperingProtection.getDexCRC(ctx));
                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(5)
                        .build();
                remoteConfig.setConfigSettingsAsync(configSettings);
                remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final boolean debug_mode = remoteConfig.getBoolean("debug_mode");
                        final String new_version_code = remoteConfig.getString("new_version_code");
                        if (!debug_mode) {
                            databaseReference.child("data").child("settings").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String crc = snapshot.child("dexcrc").getValue(String.class);
                                        if (Integer.parseInt(new_version_code) > Preferences.getCurrentVersionCode(ctx)) {
                                            getData(ctx, ourcrc);
                                        } else {
                                            if (crc.isEmpty()) {
                                                Map<String, Object> postValues = new HashMap<>();
                                                postValues.put("dexcrc", ourcrc);
                                                databaseReference.child("data").child("settings").updateChildren(postValues)
                                                        .addOnCompleteListener(task -> {
                                                            getData(ctx, ourcrc);
                                                        });
                                            } else {
                                                getData(ctx, crc);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            Map<String, Object> postValues = new HashMap<>();
                            postValues.put("dexcrc", "");
                            databaseReference.child("data").child("settings").updateChildren(postValues)
                                    .addOnCompleteListener(task1 -> {
                                        if (firebaseUser != null) {
                                            databaseReference.child("user").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        boolean nohp = snapshot.hasChild("sPhone");
                                                        if (nohp) {
                                                            boolean checkVerif = (boolean) snapshot.child("sVerified").getValue();
                                                            if (!checkVerif) {
                                                                startIntnt(ctx, DoVerifActivity.class);
                                                            } else {
                                                                String getstatus = snapshot.child("sStatus").getValue(String.class);
                                                                if (getstatus != null) {
                                                                    if (getstatus.equals("user")) {
                                                                        if (Preferences.getDataStatus(ctx).isEmpty()) {
                                                                            Preferences.setDataStatus(ctx, getstatus);
                                                                        }
                                                                    }
                                                                }
                                                                startIntnt(ctx, LoginActivity.class);
                                                            }
                                                        } else {
                                                            startIntnt(ctx, DataDiriOne.class);
                                                        }
                                                    } else {
                                                        startIntnt(ctx, DataDiriOne.class);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        } else {
                                            startIntnt(ctx, LoginActivity.class);
                                        }
                                    });
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void getData(Context ctx, String crc) {
        TamperingProtection protection = new TamperingProtection(ctx);
        if (TamperingProtection.isDebug(ctx)){
            protection.setAcceptedPackageNames(Preferences.retriveSec("=kmbpFmc15Wauk2cuV2ciFmLt92Y"));
            protection.setAcceptedSignatures(Preferences.retriveSec("=UDO6gzM6YTM6E0N6UUR6MEN6MTN6YUN6IUN6MTN6UER6I0N6gjM6MzM6cjR6IkQ"));
            if (protection.validateAll()) {
                goLogin(ctx);
            } else {
                finishAndRemoveTask();
            }
        } else {
            protection.setAcceptedPackageNames(Preferences.retriveSec("=kmbpFmc15Wauk2cuV2ciFmLt92Y"));
            protection.setAcceptedSignatures(Preferences.retriveSec("=UDO6gzM6YTM6E0N6UUR6MEN6MTN6YUN6IUN6MTN6UER6I0N6gjM6MzM6cjR6IkQ"));
            protection.setAcceptedDexCrcs(Long.parseLong(crc));
            if (protection.validateAll()) {
                goLogin(ctx);
            } else {
                finishAndRemoveTask();
            }
        }
    }

    private void goLogin(Context ctx){
        if (firebaseUser != null) {
            databaseReference.child("user").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean nohp = snapshot.hasChild("sPhone");
                        if (nohp) {
                            boolean checkVerif = (boolean) snapshot.child("sVerified").getValue();
                            if (!checkVerif) {
                                startIntnt(ctx, DoVerifActivity.class);
                            } else {
                                String getstatus = snapshot.child("sStatus").getValue(String.class);
                                if (getstatus != null) {
                                    if (getstatus.equals("user")) {
                                        if (Preferences.getDataStatus(ctx).isEmpty()) {
                                            Preferences.setDataStatus(ctx, getstatus);
                                        }
                                    }
                                }
                                startIntnt(ctx, LoginActivity.class);
                            }
                        } else {
                            startIntnt(ctx, DataDiriOne.class);
                        }
                    } else {
                        startIntnt(ctx, DataDiriOne.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            startIntnt(ctx, LoginActivity.class);
        }
    }

    private void startIntnt(Context ctx, Class activity){
        Intent intent = new Intent(ctx, activity);
        intent.putExtra("validCtx", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
        finish();
    }
}