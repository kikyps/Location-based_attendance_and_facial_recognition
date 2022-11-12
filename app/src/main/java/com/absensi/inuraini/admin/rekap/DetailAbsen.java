package com.absensi.inuraini.admin.rekap;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.datapengajuan.ApprovalActivity;
import com.absensi.inuraini.admin.location.maps.MapsActivity;
import com.absensi.inuraini.admin.location.maps.MapsViewFragment;
import com.absensi.inuraini.storage.Storage;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetailAbsen extends AppCompatActivity {
    String eventDate, idkaryawan, jabatan, latitudeTxt, longitudeTxt, titikAbsen, getOfficeName, getAddress,
            nama, getNumber, getTTl, jamMasuk, jamKeluar, ketHadir;
    Context context = this;
    TextView namaKar, ketId, absenMasuk, absenKeluar, ketAbsen, tanggalRek, lokAbsen, wktAbsenId, lemburId, titikLok;
    ImageButton nxt, prev;

    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    Calendar calendar = Calendar.getInstance();
    CardView cardLokasi;
    FrameLayout viewLokasi;
    ImageView showMore;
    boolean absenKantor, kehadiran, terlambatMasuk, jamLembur, hasData;
    int trialCount;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    String getStoragePath;

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

        if (getSupportActionBar() != null) {
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
            if (firebaseUser.getUid().equals(Preferences.retriveSec("==gM240Sl92Uvd1UJtmdYd3RlRmeJpFU5QlRXhlc"))) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("seeLocation", true);
                intent.putExtra("getAbsenLatitude", latitudeTxt);
                intent.putExtra("getAbsenLongitude", longitudeTxt);
                intent.putExtra("getAbsenLokasi", titikAbsen);
                startActivity(intent);
            } else {
                if (trialCount < 3) {
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("sTrial", trialCount++);
                    databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues);
                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra("seeLocation", true);
                    intent.putExtra("getAbsenLatitude", latitudeTxt);
                    intent.putExtra("getAbsenLongitude", longitudeTxt);
                    intent.putExtra("getAbsenLokasi", titikAbsen);
                    startActivity(intent);
                    if (trialCount == 0) {
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

    private void showAbsen() {
        databaseReference.child("data").child("latlong").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    getOfficeName = snapshot.child("sNamaKantor").getValue(String.class);
                    getAddress = snapshot.child("sAddress").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("user").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trialCount = snapshot.child("sTrial").getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("user").child(idkaryawan).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    nama = snapshot.child("sNama").getValue(String.class);
                    String jabatanData = snapshot.child("sJabatan").getValue(String.class);
                    getTTl = snapshot.child("sTtl").getValue(String.class);
                    getNumber = snapshot.child("sPhone").getValue(String.class);

                    DatabaseReference dataJabatan = FirebaseDatabase.getInstance().getReference().child("DataJabatan").child(jabatanData);
                    dataJabatan.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
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

        databaseReference.child("user").child(idkaryawan).child("sAbsensi").child(eventDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    hasData = true;
                    jamMasuk = snapshot.child("sJamMasuk").getValue(String.class);
                    jamKeluar = snapshot.child("sJamKeluar").getValue(String.class);
                    ketHadir = snapshot.child("sKet").getValue(String.class);
                    absenKantor = (boolean) snapshot.child("sKantor").getValue();
                    kehadiran = (boolean) snapshot.child("sKehadiran").getValue();
                    terlambatMasuk = (boolean) snapshot.child("sTerlambat").getValue();
                    jamLembur = (boolean) snapshot.child("sLembur").getValue();
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
                        if (jamKeluar.isEmpty()) {
                            absenKeluar.setText("-");
                        } else {
                            absenKeluar.setText(jamKeluar);
                        }

                        if (terlambatMasuk) {
                            wktAbsenId.setText("Terlambat absen");
                        } else {
                            wktAbsenId.setText("Absen tepat waktu");
                        }

                        if (jamLembur) {
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
                    hasData = false;
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

    private void setTanggal() {
        String curentDate = dateFormat.format(calendar.getTime());
        eventDate = dateRekap.format(calendar.getTime());
        tanggalRek.setText(curentDate);
        seleksiAbsen();
        showAbsen();
    }

    private void seleksiAbsen() {
        String curentDate = dateFormat.format(calendar.getTime());
        String tgglNow = dateFormat.format(new Date().getTime());
        if (curentDate.equals(tgglNow)) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Preferences.REQUEST_PERMISSION_CODE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()){
//                    Toast.makeText(this, "Permission granted in android 11 and above", Toast.LENGTH_SHORT).show();
                    printAbsensi();
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
                    printAbsensi();
                }
            }
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
                return true;
            case R.id.print_absen:
                if (!Preferences.isPermissionGranted(context)) {
                    Preferences.takePermissions(context, this);
                } else {
                    if (hasData) {
                        printAbsensi();
                    } else {
                        Preferences.showDialog(context,
                                null,
                                "Pemberitahuan",
                                "Fitur ini dalam tahap pengembangan!, untuk saat ini fitur cetak absensi belum tersedia",
                                "Mengerti",
                                null,
                                null,
                                (dialog, which) -> {
                                    dialog.dismiss();
                                },
                                (dialog, which) -> dialog.dismiss(),
                                (dialog, which) -> dialog.dismiss(),
                                false,
                                true);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void printAbsensi() {
        Bitmap bitmap, scaleBitmap;
        int pageWidth = 1200;
        int pageHeight = 2010;
        DateFormat dateFormat;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_absensi);
        scaleBitmap = Bitmap.createScaledBitmap(bitmap, 218, 218, false);

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint paint1 = new Paint();
        Paint linePDAM = new Paint();
        Paint titlePaint = new Paint();
        Paint jam = new Paint();

        PdfDocument.PageInfo pageInfo
                = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create(); //A4 Portrait
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(scaleBitmap, 20, 20, paint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(47);
        canvas.drawText(getOfficeName, pageWidth / 2 + 120, 100, titlePaint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(22f);
        canvas.drawText(getAddress, pageWidth / 2 + 120, 160, paint);

        linePDAM.setStyle(Paint.Style.STROKE);
        linePDAM.setStrokeWidth(6);
        canvas.drawLine(295, 180, pageWidth - 40, 180, linePDAM);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(70);
        canvas.drawText("ABSENSI", pageWidth / 2, 320, titlePaint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
//        canvas.drawText("NIK : " + Nik, 20, 420, paint);
        canvas.drawText("Nama : " + nama, 20, 420, paint);
        canvas.drawText("No.Telp : " + getNumber, 20, 480, paint);
        canvas.drawText("Tgl Lahir : " + getTTl, 20, 540, paint);
        canvas.drawText("Jabatan : " + jabatan, 20, 600, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        canvas.drawText("Tanggal : " + eventDate.substring(0, 2) + "/" + eventDate.substring(2, 4) + "/" + eventDate.substring(4, 8), pageWidth - 20, 420, paint);

        dateFormat = new SimpleDateFormat("HH:mm");
        canvas.drawText("Pukul : " + dateFormat.format(new Date().getTime()), pageWidth - 20, 480, paint);

        paint1.setTextAlign(Paint.Align.LEFT);
        paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint1.setColor(Color.BLACK);
        paint1.setTextSize(45);
        canvas.drawText("KETERANGAN ABSENSI", 20, 700, paint1);
//        canvas.drawText("POTONGAN", 20, 1360, paint1);

        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(3);
        canvas.drawLine(20, 710, 504, 710, paint1);
//        canvas.drawLine(20, 1370, 260, 1370, paint1);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(20, 730, pageWidth - 20, 1060, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("No.", 40, 780, paint);
        canvas.drawText("Keterangan", 160, 780, paint);
        canvas.drawText("Data Absen", pageWidth / 2 + 30, 780, paint);

        canvas.drawLine(20, 810, pageWidth - 20, 810, paint);
        canvas.drawLine(130, 740, 130, 800, paint);
        canvas.drawLine(pageWidth / 2, 740, pageWidth / 2, 800, paint);
        canvas.drawLine(pageWidth / 2, 810, pageWidth / 2, 1060, paint);
        canvas.drawLine(130, 810, 130, 1060, paint);
//        canvas.drawLine(20, 1200, pageWidth - 20, 1200, paint);

        canvas.drawText("1.", 40, 860, paint);
        canvas.drawText("Kehadiran", 160, 860, paint);
        canvas.drawText(kehadiran ? "Hadir" : "Izin", pageWidth / 2 + 30, 860, paint);

        canvas.drawText("2.", 40, 920, paint);
        canvas.drawText("Lokasi Absen", 160, 920, paint);
        canvas.drawText(absenKantor ? "Absen di kantor" : "Absen di luar kantor", pageWidth / 2 + 30, 920, paint);

        canvas.drawText("3.", 40, 980, paint);
        canvas.drawText("Waktu Absen", 160, 980, paint);
        canvas.drawText(terlambatMasuk ? "Terlambat Absen" : "Absen tepat waktu", pageWidth / 2 + 30, 980, paint);
//
        canvas.drawText("4.", 40, 1040, paint);
        canvas.drawText("Jam lembur", 160, 1040, paint);
        canvas.drawText(jamLembur ? "Lembur" : "-", pageWidth / 2 + 30, 1040, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(20, 1100, pageWidth / 2 - 120, 1300, paint);
        canvas.drawRect(pageWidth / 2 + 120, 1100, pageWidth - 20, 1300, paint);
        canvas.drawLine(20, 1180, pageWidth /2 - 120, 1180, paint);
        canvas.drawLine(pageWidth / 2 + 120, 1180, pageWidth - 20, 1180, paint);

        jam.setTextAlign(Paint.Align.LEFT);
        jam.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        jam.setColor(Color.BLACK);
        jam.setTextSize(40f);
        canvas.drawText("Jam Masuk", 140, 1150, jam);
        canvas.drawText("Jam Keluar", pageWidth / 2 + 250, 1150, jam);

        jam.setTextAlign(Paint.Align.CENTER);
        jam.setTextSize(60);
        canvas.drawText(jamMasuk, 250, 1260, jam);
        canvas.drawText(jamKeluar, pageWidth / 2 + 350, 1260, jam);
//
//        int gajiPe = Integer.parseInt(gaji);
//        int tunJab = Integer.parseInt(TunjanganJabatan);
//        int tunKel = Integer.parseInt(TunjanganKeluarga);
//        int tunBer = Integer.parseInt(TunjanganBeras);
//        int tunKin = Integer.parseInt(TunjanganKinerja);
//        int totalPenghasilan = gajiPe + tunJab + tunKel + tunBer + tunKin;
//
//        canvas.drawText("Total Penghasilan", 160, 1250, paint);
//        canvas.drawText(String.valueOf(totalPenghasilan), 780, 1250, paint);
//
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(2);
//        canvas.drawRect(20, 1390, pageWidth - 20, 1820, paint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setStyle(Paint.Style.FILL);
//        canvas.drawText("No.", 40, 1440, paint);
//        canvas.drawText("Keterangan", 160, 1440, paint);
//        canvas.drawText("Nominal", 780, 1440, paint);
//
//        canvas.drawLine(20, 1470, pageWidth - 20, 1470, paint);
//        canvas.drawLine(130, 1400, 130, 1460, paint);
//        canvas.drawLine(750, 1400, 750, 1460, paint);
//        canvas.drawLine(750, 1750, 750, 1810, paint);
//        canvas.drawLine(20, 1740, pageWidth - 20, 1740, paint);
//
//        canvas.drawText("1.", 40, 1530, paint);
//        canvas.drawText("Jumlah Kotor", 160, 1530, paint);
//        canvas.drawText(JumlahKotor, 780, 1530, paint);
//
//        canvas.drawText("2.", 40, 1590, paint);
//        canvas.drawText("Dapenma", 160, 1590, paint);
//        canvas.drawText(Dapenma, 780, 1590, paint);
//
//        canvas.drawText("3.", 40, 1650, paint);
//        canvas.drawText("Jamsostek", 160, 1650, paint);
//        canvas.drawText(JamSostek, 780, 1650, paint);
//
//        canvas.drawText("4.", 40, 1710, paint);
//        canvas.drawText("PPH 21", 160, 1710, paint);
//        canvas.drawText(JamSostek, 780, 1710, paint);
//
//        int jumKotor = Integer.parseInt(JumlahKotor);
//        int dapenma = Integer.parseInt(Dapenma);
//        int jamSostek = Integer.parseInt(JamSostek);
//        int pph = Integer.parseInt(PPH21);
//        int totalPotongan = jumKotor + dapenma + jamSostek + pph;
//
//        canvas.drawText("Total Potongan", 160, 1790, paint);
//        canvas.drawText(String.valueOf(totalPotongan), 780, 1790, paint);
//
//        double totalGaji = totalPenghasilan - totalPotongan;
//
//        String totalGajiPegawai = formatRupiah(totalGaji);
//
//        paint.setColor(Color.rgb(115, 204, 255));
//        canvas.drawRect(110, 1860, pageWidth - 120, 1970, paint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(60);
//        canvas.drawText("Total Gaji", 140, 1940, paint);
//        canvas.drawText("=", 480, 1940, paint);
//        canvas.drawText(String.valueOf(totalGajiPegawai), 580, 1940, paint);

        pdfDocument.finishPage(page);

        try {
            Storage storage = new Storage(context);

            String getDataPath = storage.getExternalStorageDirectory() + "/Absensi/pdf/";

            if (!storage.isDirectoryExists(getDataPath)){
                storage.createDirectory(getDataPath);
            }

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date now = new Date();
            String dateNow = "_" + formatter.format(now);
            String fileGenerate = getDataPath + nama + dateNow + ".pdf";

            if (storage.isFileExist(fileGenerate)){
                storage.deleteFile(fileGenerate);
            }

            pdfDocument.writeTo(new FileOutputStream(storage.getFile(fileGenerate)));

            pdfDocument.close();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Success")
                    .setMessage("Slip gaji berhasil di print, anda dapat melihat dan membagikan slip gaji")
                    .setPositiveButton("Buka", (dialog, which) -> {
                            storage.openFileWith(fileGenerate, Storage.FILE_PDF);
                        })
                    .setNegativeButton("Okay", (dialog, which) -> dialog.dismiss())
                    .setNeutralButton("Share", (dialog, which) -> {
                            storage.shareFile(fileGenerate, Storage.FILE_PDF);
                        });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(true);
            alertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error!! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}