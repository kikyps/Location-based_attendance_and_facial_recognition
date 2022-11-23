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
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.app.AppCompatDelegate;
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

public class DetailAbsen extends AppCompatActivity implements MapsViewFragment.SendDataInterface {
    String eventDate, idkaryawan, jabatan, latitudeTxt, longitudeTxt, titikAbsen, getOfficeName, getAddress,
            nama, getNumber, getTTl, jamMasuk, jamKeluar, ketHadir;
    Context context = this;
    TextView namaKar, ketId, absenMasuk, absenKeluar, ketAbsen, tanggalRek, lokAbsen, wktAbsenId, lemburId, titikLok;
    ImageButton nxt, prev;
    Menu mMenu;
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
    Bitmap bmpMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_absen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("seeLocation", true);
            intent.putExtra("getAbsenLatitude", latitudeTxt);
            intent.putExtra("getAbsenLongitude", longitudeTxt);
            intent.putExtra("getAbsenLokasi", titikAbsen);
            startActivity(intent);
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

                        if(mMenu != null) {
                            mMenu.findItem(R.id.print_absen).setVisible(true);
                        }

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
                        if(mMenu != null) {
                            mMenu.findItem(R.id.print_absen).setVisible(true);
                        }
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
                    if(mMenu != null) {
                        mMenu.findItem(R.id.print_absen).setVisible(false);
                    }
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
        mMenu = menu;
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

    public Bitmap captureScreenShot(View view) {
        /*
         * Creating a Bitmap of view with ARGB_4444.
         * */
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Drawable backgroundDrawable = view.getBackground();

        if (backgroundDrawable != null) {
            backgroundDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#80000000"));
        }
        view.draw(canvas);
        return bitmap;
    }

    private void getPaintModel(Paint paint, Paint tableContext, Paint tableField, Paint line, Paint linePDAM, Paint titlePaint, Paint jam, Paint officeName, Paint addressOffice, Paint tableField1, Paint identity, Paint time, Paint drawReact) {
        // paint
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40f);

        // tableContext
        tableContext.setTextAlign(Paint.Align.LEFT);
        tableContext.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tableContext.setColor(Color.BLACK);
        tableContext.setTextSize(45);

        // linePDAM
        linePDAM.setStyle(Paint.Style.STROKE);
        linePDAM.setStrokeWidth(6);

        // tittlePaint
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(70);

        // jam
        jam.setTextAlign(Paint.Align.CENTER);
        jam.setTextSize(60);

        // officeName
        officeName.setTextAlign(Paint.Align.CENTER);
        officeName.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        officeName.setColor(Color.BLACK);
        officeName.setTextSize(47);

        // addressOffice
        addressOffice.setTextAlign(Paint.Align.CENTER);
        addressOffice.setTextSize(22f);

        // tableField
        tableField.setTextAlign(Paint.Align.LEFT);
        tableField.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//        tableField.setStyle(Paint.Style.FILL);
        tableField.setTextSize(40f);

        // identity
        identity.setTextAlign(Paint.Align.LEFT);
        identity.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        identity.setColor(Color.BLACK);
        identity.setTextSize(40f);

        // time
        time.setTextAlign(Paint.Align.RIGHT);
        time.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        time.setColor(Color.BLACK);
        time.setTextSize(40f);

        // line
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(3);

        // drawReact
        drawReact.setStyle(Paint.Style.STROKE);
        drawReact.setStrokeWidth(3);
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
        Paint tableContext = new Paint();
        Paint lineOffice = new Paint();
        Paint titlePaint = new Paint();
        Paint jam = new Paint();
        Paint officeName = new Paint();
        Paint addressOffice = new Paint();
        Paint tableField = new Paint();
        Paint identity = new Paint();
        Paint time = new Paint();
        Paint line = new Paint();
        Paint drawReact = new Paint();

        getPaintModel(paint, tableContext, tableField, line, lineOffice, titlePaint, jam, officeName, addressOffice, tableField, identity, time, drawReact);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create(); //A4 Portrait
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(scaleBitmap, 20, 20, paint);

        canvas.drawText(getOfficeName, pageWidth / 2 + 120, 100, officeName);

        canvas.drawText(getAddress, pageWidth / 2 + 120, 160, addressOffice);

        canvas.drawLine(295, 180, pageWidth - 40, 180, lineOffice);

        canvas.drawText("ABSENSI", pageWidth / 2, 320, titlePaint);

        canvas.drawText("Nama : " + nama, 20, 420, identity);
        canvas.drawText("No.Telp : " + getNumber, 20, 480, identity);
        canvas.drawText("Tgl Lahir : " + getTTl, 20, 540, identity);
        canvas.drawText("Jabatan : " + jabatan, 20, 600, identity);

        canvas.drawText("Tanggal : " + eventDate.substring(0, 2) + "/" + eventDate.substring(2, 4) + "/" + eventDate.substring(4, 8), pageWidth - 20, 420, time);

        dateFormat = new SimpleDateFormat("HH:mm");
        canvas.drawText("Pukul : " + dateFormat.format(new Date().getTime()), pageWidth - 20, 480, time);

        canvas.drawText("KETERANGAN ABSENSI", 20, 700, tableContext);

        canvas.drawLine(20, 710, 504, 710, line);

        canvas.drawRect(20, 730, pageWidth - 20, 1060, drawReact);

        canvas.drawText("No.", 40, 780, tableField);
        canvas.drawText("Keterangan", 160, 780, tableField);
        canvas.drawText("Data Absen", pageWidth / 2 + 30, 780, tableField);

        canvas.drawLine(20, 810, pageWidth - 20, 810, line);
        canvas.drawLine(130, 740, 130, 800, line);
        canvas.drawLine(pageWidth / 2, 740, pageWidth / 2, 800, line);
        canvas.drawLine(pageWidth / 2, 810, pageWidth / 2, 1060, line);
        canvas.drawLine(130, 810, 130, 1060, line);

        canvas.drawText("1.", 40, 860, paint);
        canvas.drawText("Kehadiran", 160, 860, paint);
        canvas.drawText(kehadiran ? "Hadir" : "Izin", pageWidth / 2 + 30, 860, paint);

        canvas.drawText("2.", 40, 920, paint);
        canvas.drawText("Lokasi Absen", 160, 920, paint);

        canvas.drawText("3.", 40, 980, paint);
        canvas.drawText("Waktu Absen", 160, 980, paint);

        canvas.drawText("4.", 40, 1040, paint);
        canvas.drawText("Jam lembur", 160, 1040, paint);

        if (kehadiran) {
            canvas.drawText(absenKantor ? "Absen di kantor" : "Absen di luar kantor", pageWidth / 2 + 30, 920, paint);

            canvas.drawText(terlambatMasuk ? "Terlambat Absen" : "Absen tepat waktu", pageWidth / 2 + 30, 980, paint);

            canvas.drawText(jamLembur ? "Lembur" : "-", pageWidth / 2 + 30, 1040, paint);

            canvas.drawRect(20, 1100, pageWidth / 2 - 120, 1300, drawReact);
            canvas.drawRect(pageWidth / 2 + 120, 1100, pageWidth - 20, 1300, drawReact);
            canvas.drawLine(20, 1180, pageWidth / 2 - 120, 1180, line);
            canvas.drawLine(pageWidth / 2 + 120, 1180, pageWidth - 20, 1180, line);

            canvas.drawText("Jam Masuk", 140, 1150, identity);
            canvas.drawText("Jam Keluar", pageWidth / 2 + 250, 1150, identity);

            canvas.drawText(jamMasuk, 250, 1260, jam);
            canvas.drawText(jamKeluar, pageWidth / 2 + 350, 1260, jam);

            canvas.drawBitmap(bmpMap, 20, 1350, paint);

            canvas.drawText("Titik Lokasi Absensi", 20, 1720, identity);

            if (titikAbsen.length() > 50){
                canvas.drawText(titikAbsen.substring(0, 51), 20, 1770, paint);
                canvas.drawText(titikAbsen.substring(51), 20, 1810, paint);
            } else {
                canvas.drawText(titikAbsen, 20, 1760, paint);
            }
        } else {
            canvas.drawText("-", pageWidth / 2 + 30, 920, paint);

            canvas.drawText("-", pageWidth / 2 + 30, 980, paint);

            canvas.drawText("-", pageWidth / 2 + 30, 1040, paint);

            canvas.drawRect(20, 1100, pageWidth - 20, 1300, drawReact);
            canvas.drawLine(20, 1180, pageWidth - 20, 1180, line);

            canvas.drawText("Keterangan Izin", 40, 1150, identity);

            canvas.drawText(ketHadir, 40, 1280, paint);
        }

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
                    .setMessage("Absensi berhasil di print, anda dapat melihat dan membagikan absensi")
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

            cardLokasi.setDrawingCacheEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error!! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void sendData(Bitmap dataBmp) {
        bmpMap = dataBmp;
    }
}