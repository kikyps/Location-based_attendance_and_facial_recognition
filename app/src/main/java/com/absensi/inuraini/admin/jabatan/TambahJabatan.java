package com.absensi.inuraini.admin.jabatan;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class TambahJabatan extends AppCompatActivity {

    TextInputEditText jabatan;
    Button BtnTambahJabatan;
    String idjabatan , jabatanData;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_jabatan);
        contentListeners();
    }

    private void contentListeners() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        jabatan = findViewById(R.id.id_jabatan);
        BtnTambahJabatan = findViewById(R.id.tambah_jabatan);
        jabatan.addTextChangedListener(tambahJabatan);
        boolean updateData = getIntent().getBooleanExtra("update", false);
        idjabatan = getIntent().getStringExtra("idJabatan");

        if (updateData){
            BtnTambahJabatan.setText("Ubah Jabatan");
            showJabatan();
            BtnTambahJabatan.setOnClickListener(v -> {
                updateJabatan();
            });
        } else {
            BtnTambahJabatan.setText("Tambah Jabatan");
            BtnTambahJabatan.setOnClickListener(view -> {
                String sJabatan = jabatan.getText().toString().trim();

                StoreJabatan storeJabatan = new StoreJabatan(sJabatan);
                databaseReference.child("DataJabatan").push().setValue(storeJabatan).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Gagal upload data!", Toast.LENGTH_SHORT).show());
            });
        }

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void showJabatan(){
        databaseReference.child("DataJabatan").child(idjabatan).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    jabatanData = snapshot.child("sJabatan").getValue().toString();

                    jabatan.setText(jabatanData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateJabatan() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update jabatan")
                    .setMessage("Apakah anda ingin mengedit jabatan \n(" + jabatanData + ")")
                    .setPositiveButton("ya", (dialogInterface, i) -> {
                        String sJabatan = jabatan.getText().toString();
                        Map<String, Object> updatesJabatan = new HashMap<>();
                        updatesJabatan.put("sJabatan", sJabatan);

                        databaseReference.child("DataJabatan").child(idjabatan).updateChildren(updatesJabatan).addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Data berhasil di edit", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Terjadi kesalahan saat menedit data, periksa koneksi internet dan coba lagi!", Toast.LENGTH_LONG).show());

                    }).setNegativeButton("batal", (dialogInterface, i) -> dialogInterface.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(true);
            alertDialog.show();
    }

    private final TextWatcher tambahJabatan = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String divisi = jabatan.getText().toString().trim();

            BtnTambahJabatan.setEnabled(!divisi.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        BtnTambahJabatan.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean tambahData = getIntent().getBooleanExtra("tambah", false);
        if (!tambahData){
            getMenuInflater().inflate(R.menu.update_jabatan_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.delete_jabatan:
                deleteDivisi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDivisi(){
        databaseReference.child("user").orderByChild("sJabatan").equalTo(idjabatan).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Preferences.showDialog(context,
                            null, "Pemberitahuan!",
                            "Data jabatan (" + jabatanData + ") sudah digunakan oleh beberapa karyawan, opsi ini tidak dapat menghapus data silahkan update data jika ingin mengubahnya",
                            "oke",
                            null,
                            null,
                            (dialog, which) -> dialog.dismiss(),
                            (dialog, which) -> dialog.dismiss(),
                            (dialog, which) -> dialog.dismiss(),
                            true,
                            false);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Hapus Jabatan")
                            .setMessage("Apakah anda yakin ingin menghapus jabatan \n(" + jabatan.getText() + ")")
                            .setPositiveButton("Ya", (dialogInterface, i) -> databaseReference.child("DataJabatan").child(idjabatan).removeValue().addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(),"Data berhasil di hapus", Toast.LENGTH_SHORT).show();
                                finish();
                            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),"Terjadi kesalahan saat menghapus data, periksa koneksi internet dan coba lagi!", Toast.LENGTH_LONG).show())).setNegativeButton("Tidak", (dialogInterface, i) -> dialogInterface.cancel());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(true);
                    alertDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}