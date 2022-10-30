package com.absensi.inuraini.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.TamperingProtection;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {
    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("data");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make call to execute AsycTasks<> here
        // This helps avoid the extra step of clicking on a button
        // to take you to the MainActivity
        new StartMainActivity().execute(this);

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
                            databaseReference.child("settings").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String crc = snapshot.child("dexcrc").getValue(String.class);
                                        if (Integer.parseInt(new_version_code) > Preferences.getCurrentVersionCode(ctx)){
                                            getData(ctx, ourcrc);
                                        } else {
                                            if (crc.isEmpty()) {
                                                Map<String, Object> postValues = new HashMap<>();
                                                postValues.put("dexcrc", ourcrc);
                                                databaseReference.child("settings").updateChildren(postValues)
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
                            databaseReference.child("settings").updateChildren(postValues)
                                    .addOnCompleteListener(task1 -> {
                                        Intent intent = new Intent(ctx, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        ctx.startActivity(intent);
                                        finish();
                                    });
                        }
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private void getData(Context ctx, String crc) {
        TamperingProtection protection = new TamperingProtection(ctx);
        protection.setAcceptedPackageNames(Preferences.retriveSec("=kmbpFmc15Wauk2cuV2ciFmLt92Y"));
        protection.setAcceptedSignatures(Preferences.retriveSec("=UDO6gzM6YTM6E0N6UUR6MEN6MTN6YUN6IUN6MTN6UER6I0N6gjM6MzM6cjR6IkQ"));
        protection.setAcceptedDexCrcs(Long.parseLong(crc));
        if (protection.validateAll()){
            Intent intent = new Intent(ctx, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(intent);
            finish();
        } else {
            finishAffinity();
        }
    }
}