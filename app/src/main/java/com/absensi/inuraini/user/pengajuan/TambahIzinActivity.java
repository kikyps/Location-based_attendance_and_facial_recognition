package com.absensi.inuraini.user.pengajuan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;

import com.absensi.inuraini.R;
import com.google.android.material.textfield.TextInputLayout;

public class TambahIzinActivity extends AppCompatActivity {

    TextInputLayout tgglIzin, ketIzin;
    Button ijin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_izin);
        contentListeners();
    }

    private void contentListeners() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tgglIzin = findViewById(R.id.id_tggl_izin);
        ketIzin = findViewById(R.id.id_ket_izin);
        ijin = findViewById(R.id.tambah_izin);

        ijin.setOnClickListener(v -> {

        });

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}