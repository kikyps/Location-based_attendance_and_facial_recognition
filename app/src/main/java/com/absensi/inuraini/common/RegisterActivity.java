package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout emailValid, passwordValid, confirmPassword;
    Button register, backlogin;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        contentListeners();
    }

    private void contentListeners() {
        emailValid = findViewById(R.id.register_email);
        passwordValid = findViewById(R.id.register_password);
        confirmPassword = findViewById(R.id.register_retype_password);
        register = findViewById(R.id.register_myakun);
        backlogin = findViewById(R.id.login_button);

        register.setOnClickListener(v -> {
            if (!validateEmail() || !validatePassword()){

            } else {
                String sEmail = emailValid.getEditText().getText().toString().trim();
                String sPassword = confirmPassword.getEditText().getText().toString().trim();
                Preferences.emailAndPasswordRegister(context, sEmail, sPassword, EmailVerificationActivity.class);
            }
        });

        backlogin.setOnClickListener(v -> {
            startActivity(new Intent(context, LoginActivity.class));
            finish();
        });
    }

    private boolean validateEmail(){
        String val = emailValid.getEditText().getText().toString().trim();
        String email = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";  //email validate

        if (val.isEmpty()){
            emailValid.setError("Email tidak boleh kosong!");
            return false;
        } else if (!val.matches(email)){
            emailValid.setError("Format email tidak sesuai");
            return false;
        } else {
            emailValid.setError(null);
            emailValid.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword(){
        String val = passwordValid.getEditText().getText().toString().trim();
        String val2 = confirmPassword.getEditText().getText().toString().trim();

        String checkPassword = "(?=.*[A-Z])" + "(.*[0-9].*)";
        //"(.*[0-9].*)" +           //at least 1 digit
        //"(?=.*[a-z])" +          //at least 1 lower case letter
        //"(?=.*[A-Z])" +          //at least 1 upper case letter
        //"(?=.*[a-zA-Z])" +       //any letter
        //"(?=.*[@#$%^&+=-])" +    //at least 1 special character
        //"(?=\\S+$)" +            //no white spaces
        //".{6,}" +                //at least 6 characters
        //"$";

        if (val.isEmpty() & val2.isEmpty()){
            passwordValid.setError("Isi password anda!");
            confirmPassword.setError("Masukkan password yang sama!");
            return false;
        } else if (!val.matches(checkPassword)){
            passwordValid.setError("Password harus memiliki huruf besar dan angka!");
            return false;
        } else if (val.length() < 6){
            passwordValid.setError("Password terlalu pendek!");
            return false;
        } else if (!val.equals(val2)){
            confirmPassword.setError("Password anda tidak sama dengan password sebelumnya!");
            return false;
        } else  {
            passwordValid.setError(null);
            passwordValid.setErrorEnabled(false);
            confirmPassword.setError(null);
            confirmPassword.setErrorEnabled(false);
            return true;
        }
    }
}