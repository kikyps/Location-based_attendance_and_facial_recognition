package com.absensi.inuraini.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {

    TextInputLayout myemail;
    Button sent;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        contentListeners();
    }

    private void contentListeners() {
        myemail = findViewById(R.id.my_email);
        sent = findViewById(R.id.sent_email);
        sent.setOnClickListener(v -> {
            if (!Preferences.isConnected(context)){
                Preferences.dialogNetwork(context);
            } else {
                if (!validateEmail()){
                } else {
                    Preferences.resetLoginPassword(context, myemail.getEditText().getText().toString());
                    new Handler().postDelayed(this::finish, 3000);
                }
            }
        });
    }

    private boolean validateEmail(){
        String val = myemail.getEditText().getText().toString().trim();
        String email = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";  //email validate

        if (val.isEmpty()){
            myemail.setError("Email tidak boleh kosong!");
            return false;
        } else if (!val.matches(email)){
            myemail.setError("Format email tidak sesuai");
            return false;
        } else {
            myemail.setError(null);
            myemail.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}