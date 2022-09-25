package com.absensi.inuraini.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.absensi.inuraini.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

public class DataDiriThree extends AppCompatActivity {

    ImageView back;
    Button next;
    TextInputLayout phoneLayout;
    TextInputEditText phone;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_diri_three);
        back = findViewById(R.id.back_button2);
        phone = findViewById(R.id.nohp);
        phoneLayout = findViewById(R.id.nolayout);
        next = findViewById(R.id.send);
        ccp = findViewById(R.id.id_number);
        ccp.registerCarrierNumberEditText(phone);

        contentListeners();
    }

    private void contentListeners() {
        phone.addTextChangedListener(textWatcher);

        next.setOnClickListener(v -> {
            if (validatePhone()){
                Intent intent = new Intent(this, VerifyOTP.class);
                intent.putExtra("nomor", ccp.getFullNumberWithPlus());
                startActivity(intent);
            }
        });

        back.setOnClickListener(v -> {
            startActivity(new Intent(this, DataDiriTwo.class));
        });
    }



    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String val = phone.getText().toString();
            if (!val.isEmpty()) {
                phoneLayout.setError(null);
                phoneLayout.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString();
            if (input.length() > 0 && input.charAt(0) == '0') {
                s.replace(0, 1, "");
            }
        }
    };

    private boolean validatePhone(){
        String val = phone.getText().toString();

        String checkspace = "\\A\\w{1,20}\\z";      //white spaces validate

        if (val.isEmpty()){
            phoneLayout.setError("Nomor tidak Boleh kosong");
            return false;
        } else if (val.length() <= 10){
            phoneLayout.setError("Masukkan nomor dengan benar");
            return false;
        } else {
            phoneLayout.setError(null);
            phoneLayout.setErrorEnabled(false);
            return true;
        }
    }
}