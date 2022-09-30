package com.absensi.inuraini.user.absen;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.absensi.inuraini.MyLongClickListener;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.camera.CameraActivity;
import com.absensi.inuraini.common.LoginActivity;
import com.absensi.inuraini.user.DataKordinat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class AbsenFragment extends Fragment {

    TextInputLayout ket;
    TextView inhere, tanggal, jam, waktuAbsen, myAddresss;
    Button hadir, izin;
    ProgressBar progressBar;
    ImageButton nxt, prev;
    ImageView done;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    FusedLocationProviderClient locationProviderClient;

    private Context mContext;

    String eventDate;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    LocationManager locationManager;
    boolean GpsStatus;

//    double aoiLat = 0.4524095;
//    double aoiLong = 101.4141706;

    double aoiLat;
    double aoiLong;

    int distance;

    double latitude;
    double longitude;

    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    DateFormat jamFormat = new SimpleDateFormat("HH:mm:ss");
    DateFormat jamAbsen = new SimpleDateFormat("HH:mm");
    Calendar calendar = Calendar.getInstance();

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private Handler handler = new Handler();
    private Runnable runnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_absen, container, false);
        layoutBinding(root);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        setJam();
        setTanggal();
        buttonOncreate();
        progressBar.setVisibility(View.INVISIBLE);
        return root;
    }

    private void layoutBinding(View root){
        inhere = root.findViewById(R.id.ditempat);
        tanggal = root.findViewById(R.id.tanggal);
        jam = root.findViewById(R.id.jam);
        hadir = root.findViewById(R.id.hadir);
        izin = root.findViewById(R.id.izin);
        nxt = root.findViewById(R.id.next);
        myAddresss = root.findViewById(R.id.myAddress);

        prev = root.findViewById(R.id.previous);
        done = root.findViewById(R.id.icon_done);
        waktuAbsen = root.findViewById(R.id.jam_absen);
        progressBar = root.findViewById(R.id.progresbar);
    }

    private void buttonOncreate(){
        myAddresss.setVisibility(View.GONE);

        hadir.setOnClickListener(v -> {
            if (!GpsStatus){
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Location Manager")
                        .setMessage("Aktifkan lokasi untuk melihat titik lokasi anda!")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        })
                        .setCancelable(true)
                        .show();
            } else {
                getCurrentLocation();
            }
        });

        if (Preferences.getDataStatus(mContext).equals("admin")){
            hadir.setOnTouchListener(new MyLongClickListener(4000) {
                @Override
                public void onLongClick() {
                    Toast.makeText(mContext, "Masuk ke Pengaturan Lokasi untuk merubah lokasi absen", Toast.LENGTH_LONG).show();
                }
            });
        }

        izin.setOnClickListener(v -> {
            dialogKeterangan();
//            throw new RuntimeException("Boom!");
        });

        nxt.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, 1);
            setTanggal();
        });

        prev.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, -1);
            setTanggal();
        });

        prev.setOnTouchListener(new MyLongClickListener(4000) {
            @Override
            public void onLongClick() {
                throw new RuntimeException("Boom!");
            }
        });

        DatePickerDialog.OnDateSetListener date = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setTanggal();
        };

        tanggal.setOnClickListener(v -> {
            calendar.setTime(Calendar.getInstance().getTime());
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.my_dialog_theme, date,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void updateLatLong(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            locationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null){
                    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                    try {
                        DatabaseReference dataLatlong = FirebaseDatabase.getInstance().getReference();
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();
                        String jalan = addresses.get(0).getAddressLine(0);

                        String getDistance = "100";

                        DataKordinat dataKordinat = new DataKordinat(String.valueOf(latitude), String.valueOf(longitude), getDistance);
                        dataLatlong.child("data").child("latlong").setValue(dataKordinat).addOnSuccessListener(unused -> {
                            Toast.makeText(mContext, "Lokasi anda saat ini di set sebagai lokasi absensi karyawan", Toast.LENGTH_SHORT).show();
                            myAddresss.setText(jalan);
                            myAddresss.setVisibility(View.VISIBLE);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getLatlong();
        setTanggal();
    }

    private void getLatlong(){
        DatabaseReference dataLatlong = FirebaseDatabase.getInstance().getReference();
        dataLatlong.child("data").child("latlong").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    comeBack();
                } else if (snapshot.child("sLatitude").getValue().toString().isEmpty() && snapshot.child("sLongitude").getValue().toString().isEmpty() && !snapshot.child("sDistance").getValue().toString().isEmpty()){
                    comeBack();
                } else {
                    String latitudeValue = snapshot.child("sLatitude").getValue().toString();
                    String longitudeValue = snapshot.child("sLongitude").getValue().toString();
                    String distanceValue = snapshot.child("sDistance").getValue().toString();

                    aoiLat = Double.parseDouble(latitudeValue);
                    aoiLong = Double.parseDouble(longitudeValue);
                    distance = Integer.parseInt(distanceValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void comeBack(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Warning")
                .setMessage("Data kordinat kosong, isikan kordinat lokasi absen untuk menggunakan aplikasi ini!\n\nAtau anda bisa menghubungi admin.")
                .setPositiveButton("Oke", (dialogInterface, i) -> {
                    Preferences.signOut(mContext, LoginActivity.class);
                });
        builder.setCancelable(false);
        builder.show();
    }

    private void dialogKeterangan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.form_izin, null);
        builder.setView(dialogView)
                .setTitle("Izin")
                .setCancelable(false);
        ket = dialogView.findViewById(R.id.edit_izin);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            progressBar.setVisibility(View.INVISIBLE);
            String keterangan = ket.getEditText().getText().toString();
            String jamAbsen = AbsenFragment.this.jamAbsen.format(new Date().getTime());
            String stathadir = "izin";

            if (keterangan.isEmpty()){
                Toast.makeText(mContext, "Isi Keterangan Terlebih Dahulu!", Toast.LENGTH_SHORT).show();
            } else {
                AbsenData absenData = new AbsenData(stathadir, jamAbsen, keterangan);
                databaseReference.child(Preferences.currentUser.getUid()).child("sAbsensi").child(eventDate).setValue(absenData).addOnSuccessListener(unused -> validIzin()).addOnFailureListener(e -> Toast.makeText(requireContext(), "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setTanggal(){
        String curentDate = dateFormat.format(calendar.getTime());
        eventDate = dateRekap.format(calendar.getTime());
        tanggal.setText(curentDate);
        showAbsenToday();
        seleksiAbsen();
    }

    private void setJam(){
        //this method is used to refresh Time every Second
//        Timer timer = new Timer();
//        TimerTask timerTask = new TimerTask(){
//            @Override
//            public void run(){
//                jam.setText(jamFormat.format(new Date().getTime()));
//            }
//        };
//        timer.schedule(timerTask, 0, 1000);

        runnable = () -> {
            jam.setText(jamFormat.format(new Date().getTime()));
            handler.postDelayed(runnable, 1000);
        };
        handler.postDelayed(runnable, 1000);
    }


    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            locationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null){
                    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();

                        if (!inLocation()) {
                            izin.setEnabled(true);
                            hadir.setClickable(true);
                            hadir.setBackgroundColor(Color.RED);
                            Toast.makeText(requireContext(), "Anda tidak berada di lokasi!", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(mContext, CameraActivity.class);
                            intent.putExtra("faceid", false);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            });
        }
    }

    public void GPSStatus(){
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLatlong();
        GPSStatus();
        setTanggal();
    }

    private boolean inLocation(){
        float[] results = new float[1];
        Location.distanceBetween(aoiLat, aoiLong, latitude, longitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters < distance;
    }

    private void showAbsenToday(){
        databaseReference.child(Preferences.currentUser.getUid()).child("sAbsensi").child(eventDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String kehadiran = snapshot.child("sKehadiran").getValue().toString();
                    String jamAbsen = snapshot.child("sJam").getValue().toString();
                    String ketHadir = snapshot.child("sKet").getValue().toString();

                    if (kehadiran.equals("hadir")){
                        waktuAbsen.setText(jamAbsen);
                        inhere.setText(ketHadir);
                        validHadir();
                    } else if (kehadiran.equals("izin")){
                        waktuAbsen.setText(jamAbsen);
                        inhere.setText(ketHadir);
                        validIzin();
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

    private void validHadir(){
        done.setVisibility(View.VISIBLE);
        Drawable drawable = done.getDrawable();

        if (drawable instanceof AnimatedVectorDrawableCompat){
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable){
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }

        izin.setEnabled(false);
        hadir.setClickable(false);
        hadir.setEnabled(true);
        hadir.setBackgroundColor(Color.GREEN);
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void validIzin(){
        done.setVisibility(View.INVISIBLE);
        hadir.setEnabled(false);
        izin.setEnabled(true);
        izin.setClickable(false);
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
        hadir.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void validNoData(){
        done.setVisibility(View.INVISIBLE);
        izin.setEnabled(false);
        hadir.setEnabled(false);
        hadir.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void belumAbsen(){
        done.setVisibility(View.INVISIBLE);
        izin.setEnabled(true);
        izin.setClickable(true);
        hadir.setEnabled(true);
        hadir.setClickable(true);
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_500));
        hadir.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_500));
    }

    private void seleksiAbsen(){
        String curentDate = dateFormat.format(calendar.getTime());
        String tgglNow = dateFormat.format(new Date().getTime());
        if (curentDate.equals(tgglNow)){
            nxt.setEnabled(false);
            nxt.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_next_disabled));
            belumAbsen();
            waktuAbsen.setText("-");
            inhere.setText("Anda belum absen hari ini!");
        } else {
            nxt.setEnabled(true);
            nxt.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_next));
            validNoData();
            waktuAbsen.setText("-");
            inhere.setText("Tidak ada data absen!");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}