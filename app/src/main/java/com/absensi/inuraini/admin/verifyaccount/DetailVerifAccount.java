package com.absensi.inuraini.admin.verifyaccount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetailVerifAccount extends AppCompatActivity {

    TextView nama, ttl, email, gender, jabatan, alamat, phone, status;
    Button verifNow;
    LinearLayout updateStatus;
    String idPegawai;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
    Context context = this;
    private Spinner statusUserSpinner;
    String getSelectedRekap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_verif_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contentListeners();

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void contentListeners() {
        firebaseUser = Preferences.mAuth.getCurrentUser();
        idPegawai = getIntent().getStringExtra("idPegawai");
        nama = findViewById(R.id.id_nama);
        ttl = findViewById(R.id.id_ttl);
        email = findViewById(R.id.id_email);
        gender = findViewById(R.id.id_gender);
        jabatan = findViewById(R.id.id_jabatan);
        alamat = findViewById(R.id.id_alamat);
        phone = findViewById(R.id.id_phone);
        status = findViewById(R.id.id_status);
        updateStatus = findViewById(R.id.layout_status);
        verifNow = findViewById(R.id.verif_now);

        updateStatus.setOnClickListener(v -> {
            updateStatususerToDb();
        });

        verifNow.setOnClickListener(v -> {
            Preferences.showDialog(context, null, "Verifikasi akun", "Apakah anda yakin ingin memverifikasi akun " + nama.getText().toString() + " ?", "Iya", "Tidak", null,
                    (dialog, which) -> {
                        // Positive Button
                        Map<String, Object> postValues = new HashMap<>();
                        postValues.put("sStatus", Preferences.getOnlyStrings(status.getText().toString()));
                        postValues.put("sVerified", true);
                        databaseReference.child(idPegawai).updateChildren(postValues)
                                        .addOnCompleteListener(task -> {
                                           finish();
                                            Toast.makeText(context, nama.getText().toString() + " diverifikasi", Toast.LENGTH_SHORT).show();
                                        });
                    },
                    (dialog, which) -> {
                        // Negative Button
                        dialog.cancel();
                    },
                    (dialog, which) -> {
                        // Neutral Button
                        dialog.cancel();
                    },
                    true);
        });
        showDataPegawai();
    }

    private void updateStatususerToDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_user_status, null);
        builder.setView(dialogView);
        builder.setTitle("Update Jabatan");
        statusUserSpinner = dialogView.findViewById(R.id.spin_status);
        String[] rekapData = getResources().getStringArray(R.array.pilih_rekap);
        ArrayAdapter<String> pilihRekap = new ArrayAdapter<>(context, R.layout.spinner_data, rekapData);
        statusUserSpinner.setAdapter(pilihRekap);
        String myStatus = status.getText().toString().substring(0, 1).toUpperCase() + status.getText().toString().substring(1).toLowerCase();;
        int spinnerPosition = pilihRekap.getPosition(myStatus);
        statusUserSpinner.setSelection(spinnerPosition);

        statusUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSelectedRekap = statusUserSpinner.getSelectedItem().toString().toLowerCase(Locale.ROOT);
                showDataPegawai();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setPositiveButton("Update", (dialog, which) -> {
            status.setText(getSelectedRekap);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void showDataPegawai() {
        databaseReference.child(idPegawai).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String mynama = snapshot.child("sNama").getValue(String.class);
                    String myttl = snapshot.child("sTtl").getValue(String.class);
                    String myemail = snapshot.child("sEmail").getValue(String.class);
                    String mygender = snapshot.child("sGender").getValue(String.class);
                    String myjabatan = snapshot.child("sJabatan").getValue(String.class);
                    String myalamat = snapshot.child("sAlamat").getValue(String.class);
                    String myphone = snapshot.child("sPhone").getValue(String.class);
                    String mystatus = snapshot.child("sStatus").getValue(String.class);
                    DatabaseReference dataJabatan = FirebaseDatabase.getInstance().getReference().child("DataJabatan").child(myjabatan);
                    dataJabatan.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String jabatanku = snapshot.child("sJabatan").getValue(String.class);
                                jabatan.setText(jabatanku);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    nama.setText(mynama);
                    email.setText(myemail);
                    ttl.setText(myttl);
                    gender.setText(mygender);
                    alamat.setText(myalamat);
                    phone.setText(myphone);
                    status.setText(mystatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}