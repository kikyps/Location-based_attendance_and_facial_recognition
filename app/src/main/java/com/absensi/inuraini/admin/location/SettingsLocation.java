package com.absensi.inuraini.admin.location;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsLocation extends Fragment {
    TextInputLayout address, distance;
    TextInputEditText alamat, distanceText;
    Button changeLocations, saveDistance;

    private Context mContext;

    LocationManager locationManager;
    boolean GpsStatus;

    double titikLatitude;
    double titikLongitude;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    FusedLocationProviderClient locationProviderClient;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings_location, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        actionlisteners();
        return root;
    }

    private void layoutBinding(View root) {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        address = root.findViewById(R.id.address_input);
        distance = root.findViewById(R.id.distance);
        changeLocations = root.findViewById(R.id.ubah_kordinat);
        saveDistance = root.findViewById(R.id.simpan_jarak);
        alamat = root.findViewById(R.id.address_text);
        distanceText = root.findViewById(R.id.distance_text);
        distanceText.addTextChangedListener(textDistance);
        showAddressAndDistance();
        GPSStatus();
    }

    private void actionlisteners(){
        changeLocations.setOnClickListener(v -> {
            if (!GpsStatus){
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
                builder.setTitle("Location Manager")
                        .setMessage("Aktifkan lokasi untuk melihat titik lokasi anda!")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        })
                        .setCancelable(true)
                        .show();
            } else {
                setLatLong();
            }
        });

        saveDistance.setOnClickListener(v -> updateJarak());
    }

    private void showAddressAndDistance(){
        databaseReference.child("data").child("latlong").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String addressValue = snapshot.child("sAddress").getValue().toString();
                    String jarak = snapshot.child("sDistance").getValue().toString();

                    address.getEditText().setText(addressValue);
                    distance.getEditText().setText(jarak);

                    address.getEditText().clearFocus();
                    distance.getEditText().clearFocus();
                } else {
                    Toast.makeText(mContext, "Data kosong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateJarak(){
        String getDistance = distance.getEditText().getText().toString();
        Map<String, Object> updatesDistance = new HashMap<>();
        updatesDistance.put("sDistance", getDistance);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin mengedit data ini?")
                .setPositiveButton("ya", (dialogInterface, i) -> {
                    databaseReference.child("data").child("latlong").updateChildren(updatesDistance).addOnSuccessListener(unused -> {
                        Toast.makeText(mContext, "Jarak Berhasil Di Simpan", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });
        builder.setCancelable(true);
        builder.show();
    }

    public void GPSStatus(){
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void setLatLong(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Konfirmasi")
                .setMessage("Set titik lokasi anda saat ini sebagai lokasi absensi?")
                .setPositiveButton("ya", (dialogInterface, i) -> {
                    getCurrentLocation();
                })
                .setNegativeButton("cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });
        builder.setCancelable(true);
        builder.show();
    }

    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            locationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null){
                    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        titikLatitude = addresses.get(0).getLatitude();
                        titikLongitude = addresses.get(0).getLongitude();
                        String jalan = addresses.get(0).getAddressLine(0);

                        DataKordinat dataKordinat = new DataKordinat(String.valueOf(titikLatitude), String.valueOf(titikLongitude), jalan);
                        Map<String, Object> postValues = dataKordinat.toMap();

                        databaseReference.child("data").child("latlong").updateChildren(postValues).addOnSuccessListener(unused -> {
                            Toast.makeText(mContext, "Lokasi anda saat ini di set sebagai lokasi absensi karyawan", Toast.LENGTH_SHORT).show();
                            address.getEditText().setText(jalan);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private final TextWatcher textDistance = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String distanceText = distance.getEditText().getText().toString();

            saveDistance.setEnabled(!distanceText.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        GPSStatus();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}