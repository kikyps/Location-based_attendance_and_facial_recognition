package com.absensi.inuraini.admin.rekap;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.datapengajuan.ApprovalActivity;
import com.absensi.inuraini.admin.location.maps.MapsActivity;
import com.absensi.inuraini.admin.location.maps.MapsViewFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetailAbsen extends AppCompatActivity {
    String eventDate, idkaryawan, jabatan, latitudeTxt, longitudeTxt, titikAbsen;
    Context context = this;
    TextView namaKar, ketId, absenMasuk, absenKeluar, ketAbsen, tanggalRek, lokAbsen, wktAbsenId, lemburId, titikLok;
    ImageButton nxt, prev;

    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    Calendar calendar = Calendar.getInstance();
    CardView cardLokasi;
    FrameLayout viewLokasi;
    ImageView showMore;
    int trialCount;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_absen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        idkaryawan = getIntent().getStringExtra("idKaryawan");

        firebaseUser = Preferences.mAuth.getCurrentUser();
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
        titikLok = findViewById(R.id.titik_lokasi);
        cardLokasi = findViewById(R.id.card_lokasi);
        viewLokasi = findViewById(R.id.map_layout);
        showMore = findViewById(R.id.more_actions);

        viewLokasi.setVisibility(View.GONE);
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

        cardLokasi.setOnClickListener(v -> {
            if (firebaseUser.getUid().equals(Preferences.retriveSec("==gM240Sl92Uvd1UJtmdYd3RlRmeJpFU5QlRXhlc"))){
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("seeLocation", true);
                intent.putExtra("getAbsenLatitude", latitudeTxt);
                intent.putExtra("getAbsenLongitude", longitudeTxt);
                intent.putExtra("getAbsenLokasi", titikAbsen);
                startActivity(intent);
            } else {
                if (trialCount < 3) {
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("sTrial", trialCount + 1);
                    databaseReference.child(firebaseUser.getUid()).updateChildren(postValues);
                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra("seeLocation", true);
                    intent.putExtra("getAbsenLatitude", latitudeTxt);
                    intent.putExtra("getAbsenLongitude", longitudeTxt);
                    intent.putExtra("getAbsenLokasi", titikAbsen);
                    startActivity(intent);
                    if (trialCount == 0){
                        Toast.makeText(context, "Anda hanya dapat menggunakan fitur ini 2x Lagi", Toast.LENGTH_LONG).show();
                    } else if (trialCount == 1) {
                        Toast.makeText(context, "Anda hanya dapat menggunakan fitur ini 1x Lagi", Toast.LENGTH_LONG).show();
                    } else if (trialCount == 2) {
                        Toast.makeText(context, "Ini adalah penggunaan terakhir anda untuk dapat menggunakan fitur maps", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Preferences.showDialog(context,
                            null,
                            "Trial Limit",
                            "Masa penggunaan trial anda telah mencapai batas anda tidak dapat menggunakan fitur google maps!, untuk dapat menggunakan fitur ini kembali anda dapat menambah billing pada (Google Maps Api)",
                            "Mengerti",
                            null,
                            null,
                            (dialog, which) -> dialog.dismiss(),
                            (dialog, which) -> dialog.dismiss(),
                            (dialog, which) -> dialog.dismiss(),
                            false,
                            true);
                }
            }
        });
    }

    private void showAbsen(){
        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trialCount = snapshot.child("sTrial").getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child(idkaryawan).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String nama = snapshot.child("sNama").getValue(String.class);
                    String jabatanData = snapshot.child("sJabatan").getValue(String.class);

                    DatabaseReference dataJabatan = FirebaseDatabase.getInstance().getReference().child("DataJabatan").child(jabatanData);
                    dataJabatan.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                jabatan = snapshot.child("sJabatan").getValue(String.class);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

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
                    String jamMasuk = snapshot.child("sJamMasuk").getValue(String.class);
                    String jamKeluar = snapshot.child("sJamKeluar").getValue(String.class);
                    String ketHadir = snapshot.child("sKet").getValue(String.class);
                    boolean absenKantor = (boolean) snapshot.child("sKantor").getValue();
                    boolean kehadiran = (boolean) snapshot.child("sKehadiran").getValue();
                    boolean terlambatMasuk = (boolean) snapshot.child("sTerlambat").getValue();
                    boolean jamLembur = (boolean) snapshot.child("sLembur").getValue();
                    titikAbsen = snapshot.child("sLokasi").getValue(String.class);
                    latitudeTxt = snapshot.child("sLatitude").getValue(String.class);
                    longitudeTxt = snapshot.child("sLongitude").getValue(String.class);

                    if (kehadiran) {

                        if (!isFinishing()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("viewMyLatitude", latitudeTxt);
                            bundle.putString("viewMyLongitude", longitudeTxt);
                            bundle.putString("viewMyLokasi", titikAbsen);
                            // set Fragmentclass Arguments
                            MapsViewFragment fragobj = new MapsViewFragment();
                            fragobj.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.map_layout, fragobj).commit();
                        }

                        showMore.setVisibility(View.VISIBLE);
                        viewLokasi.setVisibility(View.VISIBLE);
                        cardLokasi.setClickable(true);
                        cardLokasi.setFocusable(true);

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
                        if (absenKantor) {
                            ketId.setText("Hadir");
                            absenMasuk.setText(jamMasuk);
                            ketAbsen.setText("-");
                            ketId.setTextColor(Color.GREEN);
                            lokAbsen.setText("Absen di kantor");
                            titikLok.setText(titikAbsen);
                        } else {
                            ketId.setText("Hadir");
                            absenMasuk.setText(jamMasuk);
                            ketAbsen.setText("-");
                            ketId.setTextColor(Color.GREEN);
                            lokAbsen.setText("Absen di luar kantor");
                            titikLok.setText(titikAbsen);
                        }
                    } else {
                        showMore.setVisibility(View.GONE);
                        cardLokasi.setClickable(false);
                        cardLokasi.setFocusable(false);
                        viewLokasi.setVisibility(View.GONE);
                        ketId.setText("Izin");
                        absenMasuk.setText("-");
                        absenKeluar.setText("-");
                        ketAbsen.setText(ketHadir);
                        ketId.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        lokAbsen.setText("-");
                        titikLok.setText("-");
                        titikLok.setTextSize(15);
                    }
                } else {
                    showMore.setVisibility(View.GONE);
                    cardLokasi.setClickable(false);
                    cardLokasi.setFocusable(false);
                    viewLokasi.setVisibility(View.GONE);
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
            titikLok.setText("-");
            titikLok.setTextSize(15);
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
            titikLok.setText("-");
            titikLok.setTextSize(15);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rekap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.data_izin:
                Intent intent = new Intent(context, ApprovalActivity.class);
                intent.putExtra("idIzin", idkaryawan);
                intent.putExtra("getNama", namaKar.getText().toString());
                intent.putExtra("getJabatan", jabatan);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}