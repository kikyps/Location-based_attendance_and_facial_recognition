package com.absensi.inuraini.admin.location;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SettingsLocation extends Fragment {
    @SuppressLint("StaticFieldLeak")
    static TextInputLayout address;
    TextInputLayout distance;
    TextInputEditText alamat, distanceText;
    Button changeLocations, saveDistance;

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    public static boolean setloc, getMaps;
    FirebaseUser firebaseUser;
    static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    int trialCount;

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
        firebaseUser = Preferences.mAuth.getCurrentUser();
        mContext = root.getContext();
        address = root.findViewById(R.id.address_input);
        distance = root.findViewById(R.id.distance);
        changeLocations = root.findViewById(R.id.ubah_kordinat);
        saveDistance = root.findViewById(R.id.simpan_jarak);
        alamat = root.findViewById(R.id.address_text);
        distanceText = root.findViewById(R.id.distance_text);
        distanceText.addTextChangedListener(textDistance);
        showAddressAndDistance();
    }

    private void actionlisteners(){
        changeLocations.setOnClickListener(v -> {
            setLatLong();
        });

        changeLocations.setOnLongClickListener(v -> {
            if (firebaseUser.getUid().equals(Preferences.retriveSec("==gM240Sl92Uvd1UJtmdYd3RlRmeJpFU5QlRXhlc"))){
                getMaps = true;
                Preferences.getMyLocation(mContext, getActivity());
            } else {
                if (trialCount < 3) {
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("sTrial", trialCount + 1);
                    databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues);
                    getMaps = true;
                    Preferences.getMyLocation(mContext, getActivity());
                    if (trialCount == 0){
                        Toast.makeText(mContext, "Anda hanya dapat menggunakan fitur ini 2x Lagi", Toast.LENGTH_LONG).show();
                    } else if (trialCount == 1) {
                        Toast.makeText(mContext, "Anda hanya dapat menggunakan fitur ini 1x Lagi", Toast.LENGTH_LONG).show();
                    } else if (trialCount == 2) {
                        Toast.makeText(mContext, "Ini adalah penggunaan terakhir anda untuk dapat menggunakan fitur maps", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Preferences.showDialog(mContext,
                            null,
                            "Trial limit",
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
            return true;
        });

        saveDistance.setOnClickListener(v -> updateJarak());
    }

    private void showAddressAndDistance(){
        databaseReference.child("user").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trialCount = snapshot.child("sTrial").getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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

    private void setLatLong(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Konfirmasi")
                .setMessage("Set titik lokasi anda saat ini sebagai lokasi absensi?")
                .setPositiveButton("ya", (dialogInterface, i) -> {
                    setloc = true;
                    Preferences.getMyLocation(mContext, getActivity());
                })
                .setNegativeButton("cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });
        builder.setCancelable(true);
        builder.show();
    }

    public static void updateLatLong(){
        DataKordinat dataKordinat = new DataKordinat(String.valueOf(Preferences.latitude), String.valueOf(Preferences.longitude), Preferences.myAddress);
        Map<String, Object> postValues = dataKordinat.toMap();

        databaseReference.child("data").child("latlong").updateChildren(postValues).addOnSuccessListener(unused -> {
            Toast.makeText(mContext, "Lokasi anda saat ini di set sebagai lokasi absensi karyawan", Toast.LENGTH_SHORT).show();
            address.getEditText().setText(Preferences.myAddress);
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show();
        });
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}