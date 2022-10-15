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
    TextView namaKar, ketHadir, jamAbsen, ketAbsen, tanggalRek, lokAbsen;
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
        ketHadir = findViewById(R.id.ket_hadir);
        jamAbsen = findViewById(R.id.jam_masuk);
        ketAbsen = findViewById(R.id.ket_absen);
        nxt = findViewById(R.id.next);
        prev = findViewById(R.id.previous);
        tanggalRek = findViewById(R.id.tanggal_rekap);
        lokAbsen = findViewById(R.id.lokasi_absen);

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
                    boolean kehadiran = (boolean) snapshot.child("sKehadiran").getValue();
                    String jam = snapshot.child("sJam").getValue(String.class);
                    String Hadir = snapshot.child("sKet").getValue(String.class);
                    String lokasi = snapshot.child("sLokasi").getValue(String.class);

                    if (kehadiran) {
                        ketHadir.setText("Hadir");
                        jamAbsen.setText(jam);
                        ketAbsen.setText(Hadir);
                        ketHadir.setTextColor(Color.GREEN);
                        lokAbsen.setText(lokasi);
                    } else {
                        ketHadir.setText("Izin");
                        jamAbsen.setText(jam);
                        ketAbsen.setText(Hadir);
                        ketHadir.setTextColor(ContextCompat.getColor(context, R.color.orange));
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
            ketHadir.setText("Belum Absen");
            ketHadir.setTextColor(ContextCompat.getColor(context, R.color.purple_500));
            jamAbsen.setText("-");
            ketAbsen.setText("-");
            lokAbsen.setText("-");
        } else {
            nxt.setEnabled(true);
            nxt.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_next));
            ketHadir.setText("X");
            ketHadir.setTextColor(Color.RED);
            jamAbsen.setText("-");
            ketAbsen.setText("Tidak ada data absen!");
            lokAbsen.setText("-");
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