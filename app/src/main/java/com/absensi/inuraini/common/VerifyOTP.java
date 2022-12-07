package com.absensi.inuraini.common;

import static com.absensi.inuraini.Preferences.mAuth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.chaos.view.PinView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    PinView pinView;
    boolean updatePhone;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        nomorku = findViewById(R.id.getour_nomor);
        done = findViewById(R.id.icon_done);
        progressBar = findViewById(R.id.progresbar);
        changeNumber = findViewById(R.id.change_number);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        String nohh = getIntent().getStringExtra("nomor");
        nomorku.setText(nohh);
        pinView = findViewById(R.id.firstPinView);
        pinView.addTextChangedListener(validateOtp);
        sendOTP(nohh);
        updatePhone = getIntent().getBooleanExtra("updatePhone", false);

        contentListeners();
    }

    private void contentListeners() {
        progressBar.setVisibility(View.VISIBLE);

        changeNumber.setOnClickListener(v -> {
            finish();
        });
    }

    private void sendOTP(String phoneNumber){
        try {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pemberitahuan")
                    .setMessage(e.getMessage())
                    .setPositiveButton("Oke", (dialogInterface, i) -> {
                       dialogInterface.dismiss();
                    });
            builder.setCancelable(false);
            builder.show();
        }
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
                pinView.setText(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(VerifyOTP.this);
            builder.setTitle("Pemberitahuan")
                    .setMessage(e.getMessage())
                    .setPositiveButton("Oke", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            builder.setCancelable(false);
            builder.show();
        }
    };

    private TextWatcher validateOtp = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.length() == 6) {
                PhoneAuthCredential authProvider = PhoneAuthProvider.getCredential(verificationCodeBySystem, charSequence.toString());
                if (charSequence.toString().equals(authProvider.getSmsCode())){
                    otpListeners();
                } else {
                    Toast.makeText(context, "OTP not valid!", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

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
                        otpListeners();
                    } else {
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void otpListeners(){
        if (updatePhone) {
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sPhone", nomorku.getText().toString());
            databaseReference.child(firebaseUser.getUid()).updateChildren(postValues)
                    .addOnSuccessListener(unused -> {
                        finish();
                        Toast.makeText(context, "Nomor Hp berhasil di ubah", Toast.LENGTH_LONG).show();
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
            postValues.put("sTrial", 0);
            databaseReference.child(firebaseUser.getUid()).setValue(postValues).addOnSuccessListener(unused -> {
                Intent i = new Intent(VerifyOTP.this, SplashScreenActivity.class);
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