package com.absensi.inuraini.common;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.user.UserActivity;
import com.absensi.inuraini.user.ui.HomeActivityUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout emailValid, passwordValid;
    Button login, register, resetPass;
    boolean doubleBackToExitPressedOnce, getValidity;
    SignInButton signinGoogle;
    FirebaseAuth firebaseAuth;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        contentlisteners();
    }

    private void contentlisteners() {
        getValidity = getIntent().getBooleanExtra("validCtx", false);
        if (!getValidity){
            finishAndRemoveTask();
        }
        Preferences.progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        firebaseAuth = Preferences.mAuth;
        emailValid = findViewById(R.id.login_email);
        passwordValid = findViewById(R.id.login_password);
        login = findViewById(R.id.login_button);
        register = findViewById(R.id.register_akun);
        signinGoogle = findViewById(R.id.login_google);
        resetPass = findViewById(R.id.reset_password);

        register.setOnClickListener(v -> startActivity(new Intent(context, RegisterActivity.class)));

        login.setOnClickListener(v -> {
            if (!Preferences.isConnected(context)) {
                Preferences.dialogNetwork(context);
            } else {
                if (!validateEmail() || !validatePassword()) {
                } else {
                    if (!Preferences.isConnected(this)) {
                        Preferences.dialogNetwork(this);
                    } else {
                        String input1 = emailValid.getEditText().getText().toString();
                        String input2 = passwordValid.getEditText().getText().toString();
                        if (lastXChars(input1, 10).equals("@gmail.com")) {
                            Preferences.emailAndPasswordLogin(context, input1, input2, SplashScreenActivity.class);
                        } else {
                            String getEmail = input1 + "@gmail.com";
                            Preferences.emailAndPasswordLogin(context, getEmail, input2, SplashScreenActivity.class);
                        }
                    }
                }
            }
        });

        signinGoogle.setOnClickListener(v -> {
            if (!Preferences.isConnected(context)) {
                Preferences.dialogNetwork(context);
            } else {
                if (!(context instanceof Activity && ((Activity) context).isFinishing())) {
                    Preferences.setProgressDialog();
                }
                turnLoginGoogle();
            }
        });

        resetPass.setOnClickListener(v -> {
            startActivity(new Intent(context, ResetPasswordActivity.class));
        });

        //Google Signin Initialized
        Preferences.googleInitialize(context);
    }

    public static String lastXChars(String v, int x) {
        return v.length() <= x ? v : v.substring(v.length() - x);
    }

    private void turnLoginGoogle() {
        Intent intent = Preferences.gsc.getSignInIntent();
        startActivityForResult(intent, Preferences.RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onFirst();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onFirst();
    }

    private void onFirst(){
        FirebaseUser currentUser = Preferences.mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                if (Preferences.getDataStatus(this).equals("user")) {
                    Intent intent = new Intent(context, HomeActivityUser.class);
                    intent.putExtra("validCtx", getValidity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra("validCtx", getValidity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
//                Intent intent = new Intent(context, UserActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                finish();
            }
        } else {
            boolean restart = getIntent().getBooleanExtra("relog", false);
            if (restart) {
                Preferences.doRestart(context);
            } else {
                if (!Preferences.getUpdateDialog(context)) {
                    Preferences.checkUpdate(context, this);
                }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Preferences.REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
//                    Toast.makeText(this, "Permission granted in android 11 and above", Toast.LENGTH_SHORT).show();
                    Preferences.downloadUpdate(context);
                }
            }
        }

        if (requestCode == Preferences.RC_SIGN_IN) {
            Preferences.progressDialog.dismiss();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Preferences.firebaseAuthWithGoogle(account.getIdToken(), context, SplashScreenActivity.class);
            } catch (ApiException ignored) {
            }
        }
    }

    private boolean validateEmail() {
        String val = emailValid.getEditText().getText().toString().trim();
        String email = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";  //email validate

        if (val.isEmpty()) {
            emailValid.setError("Email tidak boleh kosong!");
            return false;
        } else {
            emailValid.setError(null);
            emailValid.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = passwordValid.getEditText().getText().toString().trim();
        String checkPassword = "^" +
                "(?=.*[0-9])" +          //at least 1 digit
                //"(?=.*[a-z])" +          //at least 1 lower case letter
                //"(?=.*[A-Z])" +          //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +       //any letter
                //"(?=.*[@#$%^&+=-])" +    //at least 1 special character
                "(?=\\S+$)" +            //no white spaces
                //".{4,}" +                //at least 4 characters
                "$";

        if (val.isEmpty()) {
            passwordValid.setError("Username tidak boleh kosong!");
            return false;
        } else if (val.matches(checkPassword)) {
            passwordValid.setError("Kata sandi harus memiliki 1 angka atau lebih mis:(katasandi12)");
            return false;
        } else {
            passwordValid.setError(null);
            passwordValid.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Preferences.clearDataUpdateDialog(context);
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}