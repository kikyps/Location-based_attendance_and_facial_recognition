package com.absensi.inuraini.admin.rekap;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailAbsen extends AppCompatActivity {

    String idkaryawan;
    String eventDate;
    Context context = this;
    TextView namaKar, ketId, absenMasuk, absenKeluar, ketAbsen, tanggalRek, lokAbsen, wktAbsenId, lemburId;
    ImageButton nxt, prev;

    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    Calendar calendar = Calendar.getInstance();

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_absen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        idkaryawan = getIntent().getStringExtra("idKaryawan");

        namaKar = findViewById(R.id.nama_karyawan);
        ketId = findViewById(R.id.ket_hadir);
        absenMasuk = findViewById(R.id.jam_masuk);
        ketAbsen = findViewById(R.id.keterangan_txt);
        nxt = findViewById(R.id.next);
        prev = findViewById(R.id.previous);
        tanggalRek = findViewById(R.id.tanggal_rekap);
        lokAbsen = findViewById(R.id.lokasi_absen);
        wktAbsenId = findViewById(R.id.wkt_absen);
        lemburId = findViewById(R.id.lembur_txt);
        absenKeluar = findViewById(R.id.jam_keluar);

        setTanggal();
        layoutListener();

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void layoutListener() {
        nxt.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, 1);
            setTanggal();
        });

        prev.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, -1);
            setTanggal();
        });

        DatePickerDialog.OnDateSetListener date = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setTanggal();
        };

        tanggalRek.setOnClickListener(v -> {
            calendar.setTime(Calendar.getInstance().getTime());
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.my_dialog_theme, date,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void showAbsen(){
        databaseReference.child(idkaryawan).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String nama = snapshot.child("sNama").getValue().toString();

                    namaKar.setText(nama);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child(idkaryawan).child("sAbsensi").child(eventDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String jamMasuk = snapshot.child("sJamMasuk").getValue().toString();
                    String jamKeluar = snapshot.child("sJamKeluar").getValue().toString();
                    String ketHadir = snapshot.child("sKet").getValue().toString();
                    boolean absenKantor = (boolean) snapshot.child("sKantor").getValue();
                    boolean kehadiran = (boolean) snapshot.child("sKehadiran").getValue();
                    boolean terlambatMasuk = (boolean) snapshot.child("sTerlambat").getValue();
                    boolean jamLembur = (boolean) snapshot.child("sLembur").getValue();

                    absenMasuk.setText(jamMasuk);
                    if (jamKeluar.isEmpty()){
                        absenKeluar.setText("-");
                    } else {
                        absenKeluar.setText(jamKeluar);
                    }

                    if (terlambatMasuk){
                        wktAbsenId.setText("Terlambat absen");
                    } else {
                        wktAbsenId.setText("Absen tepat waktu");
                    }

                    if (jamLembur){
                        lemburId.setText("Lembur");
                    } else {
                        lemburId.setText("-");
                    }

                    if (kehadiran) {
                        if (absenKantor) {
                            ketId.setText("Hadir");
                            absenMasuk.setText(jamMasuk);
                            ketAbsen.setText("-");
                            ketId.setTextColor(Color.GREEN);
                            lokAbsen.setText("Absen di kantor");
                        } else {
                            ketId.setText("Hadir");
                            absenMasuk.setText(jamMasuk);
                            ketAbsen.setText("-");
                            ketId.setTextColor(Color.GREEN);
                            lokAbsen.setText("Absen di luar kantor");
                        }
                    } else {
                        ketId.setText("Izin");
                        absenMasuk.setText("-");
                        absenKeluar.setText("-");
                        ketAbsen.setText(ketHadir);
                        ketId.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        lokAbsen.setText("-");
                    }
                } else {
                    seleksiAbsen();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setTanggal(){
        String curentDate = dateFormat.format(calendar.getTime());
        eventDate = dateRekap.format(calendar.getTime());
        tanggalRek.setText(curentDate);
        seleksiAbsen();
        showAbsen();
    }

    private void seleksiAbsen(){
        String curentDate = dateFormat.format(calendar.getTime());
        String tgglNow = dateFormat.format(new Date().getTime());
        if (curentDate.equals(tgglNow)){
            nxt.setEnabled(false);
            nxt.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_next_disabled));
            ketId.setText("Belum Absen");
            ketId.setTextColor(ContextCompat.getColor(context, R.color.purple_500));
            absenMasuk.setText("-");
            ketAbsen.setText("-");
            lokAbsen.setText("-");
            wktAbsenId.setText("-");
            absenKeluar.setText("-");
            lemburId.setText("-");
        } else {
            nxt.setEnabled(true);
            nxt.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_next));
            ketId.setText("X");
            ketId.setTextColor(Color.RED);
            absenMasuk.setText("-");
            ketAbsen.setText("-");
            lokAbsen.setText("-");
            wktAbsenId.setText("-");
            absenKeluar.setText("-");
            lemburId.setText("-");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}