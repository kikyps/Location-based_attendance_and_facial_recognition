package com.absensi.inuraini.user.account;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.jabatan.StoreJabatan;
import com.absensi.inuraini.common.ChangePasswordActivity;
import com.absensi.inuraini.common.VerifyOTP;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class AccountInfo extends Fragment {
    TextInputLayout ket;
    TextView nama, ttl, email, gender, jabatan, alamat, phone;
    LinearLayout updateNama, updateTtl, updateGender, updateJabatan, updateAlamat, updatePhone, updatePassword;
    String mynama, upgender, keyJabatan, myalamat, myphone;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context mContext;
    Calendar calendar = Calendar.getInstance();
    final Calendar myCalendar = Calendar.getInstance();
    int mYear = calendar.get(Calendar.YEAR);
    int mDay = calendar.get(Calendar.DATE);
    int mMonth = calendar.get(Calendar.MONTH);
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    RadioGroup jk;
    RadioButton pria, wanita;
    private Spinner jabatanSpinner;
    private ArrayList<String> jabatanSpin = new ArrayList<>();
    CountryCodePicker ccp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_info, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        return root;
    }

    private void layoutBinding(View root) {
        firebaseUser = Preferences.mAuth.getCurrentUser();
        nama = root.findViewById(R.id.id_nama);
        ttl = root.findViewById(R.id.id_ttl);
        email = root.findViewById(R.id.id_email);
        gender = root.findViewById(R.id.id_gender);
        jabatan = root.findViewById(R.id.id_jabatan);
        alamat = root.findViewById(R.id.id_alamat);
        phone = root.findViewById(R.id.id_phone);
        updateNama = root.findViewById(R.id.layout_nama);
        updateTtl = root.findViewById(R.id.layout_ttl);
        updateGender = root.findViewById(R.id.layout_gender);
        updateJabatan = root.findViewById(R.id.layout_jabatan);
        updateAlamat = root.findViewById(R.id.layout_alamat);
        updatePhone = root.findViewById(R.id.layout_phone);
        updatePassword = root.findViewById(R.id.change_password);

        contentListeners();
        showMyIdentity();
    }

    private void contentListeners() {
        myCalendar.get(Calendar.YEAR);
        myCalendar.get(Calendar.MONTH);
        myCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener date = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateTtlToDb();
        };

        updateNama.setOnClickListener(v -> {
            setUpdateNama();
        });

        updateTtl.setOnClickListener(v -> {
            myCalendar.set(Calendar.DAY_OF_MONTH, mDay);
            myCalendar.set(Calendar.MONTH, mMonth);
            myCalendar.set(Calendar.YEAR, mYear - 18);
            String myyear = ttl.getText().toString().substring(6, 10);
            String mymonth = ttl.getText().toString().substring(3, 5);
            String mydate = ttl.getText().toString().substring(0, 2);
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.MySpinnerDatePickerStyle, date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setTitle("Update Tanggal Lahir");
            datePickerDialog.setMessage("Umur tidak dapat diubah di bawah 18 tahun, sesuaikan tanggal lahir anda.");
            datePickerDialog.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
            datePickerDialog.updateDate(Integer.parseInt(myyear), Integer.parseInt(mymonth) - 1, Integer.parseInt(mydate));
            datePickerDialog.setCancelable(true);
            datePickerDialog.show();
        });

        updateGender.setOnClickListener(v -> {
            updateGenderToDb();
        });

        updateJabatan.setOnClickListener(v -> {
            updateJabatanToDb();
        });

        updateAlamat.setOnClickListener(v -> {
            updateAlamatToDb();
        });

        updatePhone.setOnClickListener(v -> {
            updatePhoneToDb();
        });

        updatePassword.setOnClickListener(v -> {
            startActivity(new Intent(mContext, ChangePasswordActivity.class));
        });
    }

    private void updatePhoneToDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_update_phone, null);
        builder.setView(dialogView);
        builder.setTitle("Update No Hp");
        ket = dialogView.findViewById(R.id.update_nohp);
        ccp = dialogView.findViewById(R.id.id_number);
        ccp.registerCarrierNumberEditText(ket.getEditText());
        String pNumber = myphone.substring(3);
        ket.getEditText().setText(pNumber);
        builder.setPositiveButton("Update", (dialog, which) -> {
            Intent intent = new Intent(mContext, VerifyOTP.class);
            intent.putExtra("nomor", ccp.getFullNumberWithPlus());
            intent.putExtra("updatePhone", true);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
        Button buttonDialog = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        ket.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keterangan = ket.getEditText().getText().toString();
                buttonDialog.setEnabled(!keterangan.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() > 0 && input.charAt(0) == '0') {
                    s.replace(0, 1, "");
                }
            }
        });
    }

    private void updateAlamatToDb() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.view_update_alamat, null);
            builder.setView(dialogView);
            builder.setTitle("Update Alamat");
            ket = dialogView.findViewById(R.id.update_alamat);
            ket.getEditText().setText(myalamat);
            builder.setPositiveButton("Update", (dialog, which) -> {
                String alamatsaya = ket.getEditText().getText().toString();
                Map<String, Object> postValues = new HashMap<>();
                postValues.put("sAlamat", alamatsaya);
                databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues)
                        .addOnSuccessListener(unused -> Toast.makeText(mContext, "Alamat berhasil di ubah", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(true);
            alertDialog.show();
            Button buttonDialog = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            ket.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String keterangan = ket.getEditText().getText().toString();
                    buttonDialog.setEnabled(!keterangan.isEmpty());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
    }

    private void updateJabatanToDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_update_jabatan, null);
        builder.setView(dialogView);
        builder.setTitle("Update Jabatan");
        jabatanSpinner = dialogView.findViewById(R.id.jabatan);
        showSpinnerJabatan();
        builder.setPositiveButton("Update", (dialog, which) -> {
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sJabatan", keyJabatan);
            databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues)
                    .addOnSuccessListener(unused -> Toast.makeText(mContext, "Jabatan anda berhasil di ubah", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void updateGenderToDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_update_gender, null);
        builder.setView(dialogView);
        builder.setTitle("Update Jenis Kelamin");
        jk = dialogView.findViewById(R.id.gender);
        pria = dialogView.findViewById(R.id.male);
        wanita = dialogView.findViewById(R.id.famale);
        if (gender.getText().equals("Pria")){
            pria.setChecked(true);
        } else {
            wanita.setChecked(true);
        }
        builder.setPositiveButton("Update", (dialog, which) -> {
            if (pria.isChecked()){
                upgender = pria.getText().toString();
            } else {
                upgender = wanita.getText().toString();
            }
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sGender", upgender);
            databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues)
                    .addOnSuccessListener(unused -> Toast.makeText(mContext, "Jenis kelamin berhasil di ubah", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void updateTtlToDb() {
        String curentDate = dateFormat.format(calendar.getTime());
        Map<String, Object> postValues = new HashMap<>();
        postValues.put("sTtl", curentDate);
        databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues)
                .addOnSuccessListener(unused -> Toast.makeText(mContext, "Tanggal lahir berhasil di ubah", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
    }

    private void showMyIdentity() {
        databaseReference.child("user").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    mynama = snapshot.child("sNama").getValue(String.class);
                    String myttl = snapshot.child("sTtl").getValue(String.class);
                    String myemail = snapshot.child("sEmail").getValue(String.class);
                    String mygender = snapshot.child("sGender").getValue(String.class);
                    String myjabatan = snapshot.child("sJabatan").getValue(String.class);
                    myalamat = snapshot.child("sAlamat").getValue(String.class);
                    myphone = snapshot.child("sPhone").getValue(String.class);
                    DatabaseReference dataJabatan = FirebaseDatabase.getInstance().getReference().child("DataJabatan").child(myjabatan);
                    dataJabatan.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String jabatanku = snapshot.child("sJabatan").getValue(String.class);
                                jabatan.setText(jabatanku);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    nama.setText(mynama);
                    ttl.setText(myttl);
                    email.setText(myemail);
                    gender.setText(mygender);
                    alamat.setText(myalamat);
                    phone.setText(myphone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUpdateNama() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_update_nama, null);
        builder.setView(dialogView);
        builder.setTitle("Update Nama");
        ket = dialogView.findViewById(R.id.update_nama);
        ket.getEditText().setText(mynama);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String namasaya = ket.getEditText().getText().toString();
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sNama", namasaya);
            databaseReference.child("user").child(firebaseUser.getUid()).updateChildren(postValues)
                    .addOnSuccessListener(unused -> Toast.makeText(mContext, "Nama berhasil di ubah", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(mContext, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
        Button buttonDialog = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        ket.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keterangan = ket.getEditText().getText().toString();
                buttonDialog.setEnabled(!keterangan.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void showSpinnerJabatan(){
        databaseReference.child("DataJabatan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jabatanSpin.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    StoreJabatan storeJabatan = item.getValue(StoreJabatan.class);
                    jabatanSpin.add(storeJabatan.getsJabatan());
//                    jabatanData.add(storeJabatan);
                    //jabatanSpin.add(item.child("sJabatan").getValue(String.class));
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_data, jabatanSpin);
                jabatanSpinner.setAdapter(arrayAdapter);
                String myJabatan = jabatan.getText().toString();
                int spinnerPosition = arrayAdapter.getPosition(myJabatan);
                jabatanSpinner.setSelection(spinnerPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        jabatanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedJabatan = jabatanSpinner.getSelectedItem().toString();

                databaseReference.child("DataJabatan").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                            String nameJabatan = childSnapshot.child("sJabatan").getValue().toString();

                            if (nameJabatan.equals(selectedJabatan)){
                                keyJabatan = childSnapshot.getKey();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}