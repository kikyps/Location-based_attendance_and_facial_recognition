package com.absensi.inuraini.admin.datapegawai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailPegawai extends AppCompatActivity {

    TextView nama, ttl, email, gender, jabatan, alamat, phone, status;
    String idPegawai;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pegawai);
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
        nama = findViewById(R.id.id_nama);
        ttl = findViewById(R.id.id_ttl);
        email = findViewById(R.id.id_email);
        gender = findViewById(R.id.id_gender);
        jabatan = findViewById(R.id.id_jabatan);
        alamat = findViewById(R.id.id_alamat);
        phone = findViewById(R.id.id_phone);
        status = findViewById(R.id.id_status);

        showDataPegawai();
    }

    private void showDataPegawai() {
        idPegawai = getIntent().getStringExtra("idPegawai");
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