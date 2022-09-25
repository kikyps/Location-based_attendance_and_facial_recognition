package com.absensi.inuraini.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.jabatan.StoreJabatan;
import com.absensi.inuraini.common.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DataDiriOne extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce;
    TextInputLayout nama, alamat;
    Button next;
    public static String getName, getAlamat, keyJabatan;
    private Spinner jabatanSpinner;
    private ArrayList<String> jabatanSpin = new ArrayList<>();
    private ArrayList<StoreJabatan> jabatanData = new ArrayList<>();
    Context context = this;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_diri_one);
        nama = findViewById(R.id.myname);
        alamat = findViewById(R.id.myalamat);
        next = findViewById(R.id.next2);
        jabatanSpinner = findViewById(R.id.jabatan);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        showSpinnerJabatan();
        contentListeners();
    }

    private void contentListeners() {
        nama.getEditText().setText(firebaseUser.getDisplayName());

        next.setOnClickListener(v -> {
            if (!validateName() | !validateAlamat()){
            } else {
                getName = nama.getEditText().getText().toString();
                getAlamat = alamat.getEditText().getText().toString();

                Intent intent = new Intent(getApplicationContext(), DataDiriTwo.class);
                startActivity(intent);
            }
        });
    }

    private void showSpinnerJabatan(){
        databaseReference.child("DataJabatan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jabatanSpin.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    StoreJabatan storeJabatan = item.getValue(StoreJabatan.class);
                    jabatanSpin.add(storeJabatan.getsJabatan());
//                    jabatanData.add(storeJabatan);
                    //jabatanSpin.add(item.child("sJabatan").getValue(String.class));
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.spinner_data, jabatanSpin);
                jabatanSpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        jabatanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedJabatan = jabatanSpinner.getSelectedItem().toString();

                databaseReference.child("DataJabatan").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                            String nameJabatan = childSnapshot.child("sJabatan").getValue().toString();

                            if (nameJabatan.equals(selectedJabatan)){
                                keyJabatan = childSnapshot.getKey();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Preferences.getUpdateDialog(context)){
            Preferences.checkUpdate(context, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Preferences.REQUEST_PERMISSION_CODE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()){
//                    Toast.makeText(this, "Permission granted in android 11 and above", Toast.LENGTH_SHORT).show();
                    Preferences.downloadUpdate(context);
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

    private boolean validateName(){
        String val = nama.getEditText().getText().toString();

        String checkspace = "\\A\\w{1,20}\\z";      //white spaces validate

        if (val.isEmpty()){
            nama.setError("Nama Tidak Boleh Kosong");
            return false;
        } else if (val.length() > 60){
            nama.setError("Nama terlalu panjang!");
            return false;
        } else {
            nama.setError(null);
            nama.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateAlamat(){
        String val = alamat.getEditText().getText().toString();

        String checkspace = "\\A\\w{1,20}\\z";      //white spaces validate

        if (val.isEmpty()){
            alamat.setError("Alamat Tidak Boleh Kosong");
            return false;
        } else {
            alamat.setError(null);
            alamat.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}