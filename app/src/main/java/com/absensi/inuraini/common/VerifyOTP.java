package com.absensi.inuraini.common;

import static com.absensi.inuraini.Preferences.mAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.user.UserActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyOTP extends AppCompatActivity {

    TextView nomorku;
    String verificationCodeBySystem;
    ProgressBar progressBar;
    ImageView done;
    Button changeNumber;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    Context context = this;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        nomorku = findViewById(R.id.getour_nomor);
        done = findViewById(R.id.icon_done);
        progressBar = findViewById(R.id.progresbar);
        changeNumber = findViewById(R.id.change_number);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        String nohh = getIntent().getStringExtra("nomor");
        nomorku.setText(nohh);
        sendOTP(nohh);

        contentListeners();
    }

    private void contentListeners() {
        progressBar.setVisibility(View.VISIBLE);

        changeNumber.setOnClickListener(v -> {
            finish();
        });
    }

    private void sendOTP(String phoneNumber){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                boolean updatePhone = getIntent().getBooleanExtra("updatePhone", false);
                if (updatePhone) {
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("sPhone", nomorku.getText().toString());
                    databaseReference.child(firebaseUser.getUid()).updateChildren(postValues)
                            .addOnSuccessListener(unused -> {
                                finish();
                                Toast.makeText(context, "Nomor Hp berhasil di ubah", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
                } else {
                    String namasaya = DataDiriOne.getName;
                    String myemail = firebaseUser.getEmail();
                    String alamatku = DataDiriOne.getAlamat;
                    String gendersaya = DataDiriTwo.gender;
                    String ttlku = DataDiriTwo.ttlku;
                    String noku = nomorku.getText().toString();
                    String jabatan = DataDiriOne.keyJabatan;
                    validOtp();
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("sNama", namasaya);
                    postValues.put("sEmail", myemail);
                    postValues.put("sAlamat", alamatku);
                    postValues.put("sGender", gendersaya);
                    postValues.put("sTtl", ttlku);
                    postValues.put("sPhone", noku);
                    postValues.put("sJabatan", jabatan);
                    postValues.put("sStatus", "user");
                    postValues.put("sVerified", false);
                    databaseReference.child(firebaseUser.getUid()).setValue(postValues).addOnSuccessListener(unused -> {
                        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        Intent i = new Intent(VerifyOTP.this, DoVerifActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void validOtp(){
        progressBar.setVisibility(View.INVISIBLE);
        done.setVisibility(View.VISIBLE);
        Drawable drawable = done.getDrawable();

        if (drawable instanceof AnimatedVectorDrawableCompat){
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable){
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
        changeNumber.setEnabled(false);
    }

    private void verifyCode(String code){
        PhoneAuthCredential authProvider = PhoneAuthProvider.getCredential(verificationCodeBySystem, code);
        validOtpCredential(authProvider);
    }

    private void validOtpCredential(PhoneAuthCredential authProvider) {
        mAuth.signInWithCredential(authProvider)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        finish();
                    }
                });
    }
}