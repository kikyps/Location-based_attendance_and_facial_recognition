package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

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
        contentListeners();
    }

    private void contentListeners() {
        myemail = findViewById(R.id.my_email);
        sent = findViewById(R.id.sent_email);
        sent.setOnClickListener(v -> {
            Preferences.resetLoginPassword(context, myemail.getEditText().getText().toString());
            new Handler().postDelayed(this::finish, 3000);
        });
    }
}