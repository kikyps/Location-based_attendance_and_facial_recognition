package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    TextInputLayout passLama, passBaru, confirmPass;
    Button changePass, resetPass;
    FirebaseUser user;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        contentListeners();
    }

    private void contentListeners() {
        user = Preferences.mAuth.getCurrentUser();
        passLama = findViewById(R.id.old_password);
        passBaru = findViewById(R.id.new_password);
        confirmPass = findViewById(R.id.confirm_password_baru);
        changePass = findViewById(R.id.try_change_password);
        resetPass = findViewById(R.id.reset_passEmail);

        passLama.getEditText().addTextChangedListener(passwordValidate);
        passBaru.getEditText().addTextChangedListener(passwordValidate);
        confirmPass.getEditText().addTextChangedListener(passwordValidate);

        changePass.setOnClickListener(v -> {
            if (!validatePassword()){

            } else {
                Preferences.showDialog(context, null, "Ubah Password", "Apakah anda yakin ingin merubah password?", "Ubah", "Batal", null,
                        (dialog, which) -> {
                            // Positive Button
                            Preferences.tryChangePassword(context, user, passLama.getEditText().getText().toString(), confirmPass.getEditText().getText().toString());
                        },
                        (dialog, which) -> {
                            // Negative Button
                            dialog.cancel();
                        },
                        (dialog, which) -> {
                            // Neutral Button
                            dialog.cancel();
                        },
                        true,
                        false);
            }
        });

        resetPass.setOnClickListener(v -> {
            Preferences.showDialog(context, null, "Reset Password", "Jika anda login melalui akun gmail pada ponsel anda dan belum membuat password untuk login menggunakan email dan password secara manual,\nATAU benar-benar lupa dengan password anda, anda dapat meminta reset password anda melalui email verifikasi yang kami kirimkan dengan klik tombol kirim", "Kirim", "Batal", null,
                    (dialog, which) -> {
                        // Positive Button
                        Preferences.resetLoginPassword(context, user.getEmail());
                    },
                    (dialog, which) -> {
                        // Negative Button
                        dialog.cancel();
                    },
                    (dialog, which) -> {
                        // Neutral Button
                        dialog.cancel();
                    },
                    true,
                    false);
        });
    }

    private TextWatcher passwordValidate = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String val = passLama.getEditText().getText().toString().trim();
            String val2 = passBaru.getEditText().getText().toString().trim();
            String val3 = confirmPass.getEditText().getText().toString().trim();
            String checkPassword = "(?=.*[A-Z])" + "(.*[0-9].*)";

            if (val.isEmpty() && val2.isEmpty() && val3.isEmpty()){
                passLama.setError(null);
                passLama.setErrorEnabled(false);
                passBaru.setError(null);
                passBaru.setErrorEnabled(false);
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            } else if (!val.isEmpty() && val2.isEmpty() && val3.isEmpty()){
                passLama.setError(null);
                passLama.setErrorEnabled(false);
                passBaru.setError(null);
                passBaru.setErrorEnabled(false);
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            } else if (!val.isEmpty() && !val2.matches(checkPassword) && val3.isEmpty()){
                passLama.setError(null);
                passLama.setErrorEnabled(false);
                passBaru.setError("Password harus memiliki huruf besar dan angka!");
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            } else if (!val.isEmpty() && val2.matches(checkPassword) && val2.length() <= 7 && val3.isEmpty()){
                passLama.setError(null);
                passLama.setErrorEnabled(false);
                passBaru.setError("Password terlalu pendek!");
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            } else if (!val.isEmpty() && val2.matches(checkPassword) && val2.length() > 7 && val3.isEmpty()){
                passLama.setError(null);
                passLama.setErrorEnabled(false);
                passBaru.setError(null);
                passBaru.setErrorEnabled(false);
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            } else {
                passLama.setError(null);
                passLama.setErrorEnabled(false);
                passBaru.setError(null);
                passBaru.setErrorEnabled(false);
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private boolean validatePassword() {
        String val = passLama.getEditText().getText().toString().trim();
        String val2 = passBaru.getEditText().getText().toString().trim();
        String val3 = confirmPass.getEditText().getText().toString().trim();

        String checkPassword = "(?=.*[A-Z])" + "(.*[0-9].*)";
        //"(.*[0-9].*)" +           //at least 1 digit
        //"(?=.*[a-z])" +          //at least 1 lower case letter
        //"(?=.*[A-Z])" +          //at least 1 upper case letter
        //"(?=.*[a-zA-Z])" +       //any letter
        //"(?=.*[@#$%^&+=-])" +    //at least 1 special character
        //"(?=\\S+$)" +            //no white spaces
        //".{6,}" +                //at least 6 characters
        //"$";

        if (val.isEmpty() && val2.isEmpty() && val3.isEmpty()) {
            passLama.setError("Isi password lama anda!");
            passBaru.setError("Isi password baru anda!");
            confirmPass.setError("Form tidak boleh kosong!");
            return false;
        } else if (!val.isEmpty() && val2.isEmpty() && val3.isEmpty()) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError("Isi password baru anda!");
            confirmPass.setError("Form tidak boleh kosong!");
            return false;
        } else if (!val.isEmpty() && !val2.matches(checkPassword) && val2.length() <= 7 && val3.isEmpty()) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError("Password harus memiliki huruf besar dan angka!");
            confirmPass.setError("Form tidak boleh kosong!");
            return false;
        } else if (!val.isEmpty() && val2.matches(checkPassword) && val2.length() <= 7 && val3.isEmpty()) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError("Password terlalu pendek!");
            confirmPass.setError("Form tidak boleh kosong!");
            return false;
        } else if (!val.isEmpty() && val2.matches(checkPassword) && val2.length() > 7 && val3.isEmpty()) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError("Form tidak boleh kosong!");
            return false;
        } else if (!val.isEmpty() && !val2.matches(checkPassword) && val2.length() <= 7 && !val3.equals(val2)) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError("Password harus memiliki huruf besar dan angka!");
            confirmPass.setError("Password anda tidak sama dengan password sebelumnya!");
            return false;
        } else if (!val.isEmpty() && val2.matches(checkPassword) && val2.length() <= 7 && !val3.equals(val2)) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError("Password terlalu pendek!");
            confirmPass.setError("Password anda tidak sama dengan password sebelumnya!");
            return false;
        } else if (!val.isEmpty() && val2.matches(checkPassword) && val2.length() > 7 && !val3.equals(val2)) {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError("Password anda tidak sama dengan password sebelumnya!");
            return false;
        } else if (val.isEmpty() && !val2.matches(checkPassword) && val2.length() <= 7 && val3.isEmpty()) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && val2.matches(checkPassword) && val2.length() <= 7 && val3.isEmpty()) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && val2.matches(checkPassword) && val2.length() > 7 && val3.isEmpty()) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && !val2.matches(checkPassword) && val2.length() <= 7 && !val3.equals(val2)) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && val2.matches(checkPassword) && val2.length() <= 7 && !val3.equals(val2)) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && val2.matches(checkPassword) && val2.length() > 7 && !val3.equals(val2)) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && !val2.matches(checkPassword) && val2.length() <= 7 && val3.equals(val2)) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && val2.matches(checkPassword) && val2.length() <= 7 && val3.equals(val2)) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else if (val.isEmpty() && val2.matches(checkPassword) && val2.length() > 7 && val3.equals(val2)) {
            passLama.setError("Isi password lama anda terlebih dahulu!");
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return false;
        } else {
            passLama.setError(null);
            passLama.setErrorEnabled(false);
            passBaru.setError(null);
            passBaru.setErrorEnabled(false);
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}