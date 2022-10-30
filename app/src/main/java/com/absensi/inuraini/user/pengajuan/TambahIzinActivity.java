package com.absensi.inuraini.user.pengajuan;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.user.absen.AbsenData;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TambahIzinActivity extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    TextInputLayout tgglIzin, ketIzin;
    Button ijin;
    String idIzin, tggl, ketUpdate;
    Context context = this;
    boolean update, setHas = false;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_izin);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        contentListeners();
    }

    private void contentListeners() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tgglIzin = findViewById(R.id.id_tggl_izin);
        ketIzin = findViewById(R.id.id_ket_izin);
        ijin = findViewById(R.id.tambah_izin);
        update = getIntent().getBooleanExtra("updateIzin", false);
        idIzin = getIntent().getStringExtra("idIzin");
        tggl = getIntent().getStringExtra("setTggl");
        ketUpdate = getIntent().getStringExtra("setKet");

        DatePickerDialog.OnDateSetListener tgglCalendar = (datePicker, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setTgglIzin();
        };

        tgglIzin.getEditText().setOnClickListener(v -> {
            myCalendar.setTime(myCalendar.getTime());
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.my_dialog_theme, tgglCalendar,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });


        if (update) {
            tgglIzin.getEditText().setText(tggl);
            tgglIzin.setEnabled(false);
            ketIzin.getEditText().setText(ketUpdate);
            ijin.setText("Update Data Izin");
            ijin.setOnClickListener(v -> {
                if (!validateForm()) {

                } else {
                    updateIzin();
                }
            });
        } else {
            ijin.setOnClickListener(v -> {
                if (!validateForm()) {

                } else {
                    tambahIzin();
                }
            });
        }

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void updateIzin() {
        Preferences.showDialog(context,
                null,
                "Ubah Izin",
                "Apakah anda yakin ingin mengubah data izin ini?",
                "iya",
                null,
                null,
                (dialog, which) -> {
                    // Positive Button
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("sKet", ketIzin.getEditText().getText().toString());
                    databaseReference.child("user").child(firebaseUser.getUid()).child("sAbsensi").child(idIzin).updateChildren(postValues)
                            .addOnCompleteListener(task -> {
                                Toast.makeText(context, "Data Izin berhasil di edit", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                },
                (dialog, which) -> {
                    // Negative Button
                    dialog.cancel();
                },
                (dialog, which) -> {
                    // Neutral Button
                    dialog.cancel();
                },
                true,
                false);
    }

    private void tambahIzin() {
        databaseReference.child("user").child(firebaseUser.getUid()).child("sAbsensi").child(Preferences.getOnlyDigits(tgglIzin.getEditText().getText().toString())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean kehadiran = (boolean) snapshot.child("sKehadiran").getValue();
                    if (!setHas) {
                        if (kehadiran) {
                            Preferences.showDialog(context,
                                    null,
                                    "Pengajuan Izin",
                                    "Anda sudah absen hari ini, anda tidak dapat izin ketika anda sudah absen atau hadir ke kantor, pilih tanggal lain!",
                                    "oke",
                                    null,
                                    null,
                                    (dialog, which) -> {
                                        // Positive Button
                                        dialog.dismiss();
                                    },
                                    (dialog, which) -> {
                                        // Negative Button
                                        dialog.cancel();
                                    },
                                    (dialog, which) -> {
                                        // Neutral Button
                                        dialog.cancel();
                                    },
                                    true,
                                    false);
                        } else {
                            Preferences.showDialog(context,
                                    null,
                                    "Pengajuan Izin",
                                    "Anda sudah izin hari ini, pilih tanggal lain untuk mengajukan izin!",
                                    "oke",
                                    null,
                                    null,
                                    (dialog, which) -> {
                                        // Positive Button
                                        dialog.dismiss();
                                    },
                                    (dialog, which) -> {
                                        // Negative Button
                                        dialog.cancel();
                                    },
                                    (dialog, which) -> {
                                        // Neutral Button
                                        dialog.cancel();
                                    },
                                    true,
                                    false);

                        }
                    }
                } else {
                    setHas = true;
                    Preferences.showDialog(context,
                            null,
                            "Pengajuan Izin",
                            "Apakah anda yakin ingin mengajukan izin pada tanggal " + tgglIzin.getEditText().getText().toString() + " ?",
                            "Iya",
                            "Tidak",
                            null,
                            (dialog, which) -> {
                                // Positive Button
                                sayaIzin();
                            },
                            (dialog, which) -> {
                                // Negative Button
                                dialog.cancel();
                            },
                            (dialog, which) -> {
                                // Neutral Button
                                dialog.cancel();
                            },
                            true,
                            false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sayaIzin() {
        String tggl = Preferences.getOnlyDigits(tgglIzin.getEditText().getText().toString());
        String ketizin = ketIzin.getEditText().getText().toString();
        boolean absenKantor = false;
        boolean telat = getIntent().getBooleanExtra("telat", false);
        boolean lembur = getIntent().getBooleanExtra("lembur", false);
        boolean hadir = false;
        boolean acc = false;
        boolean konfirmAdmin = false;

        AbsenData absenData = new AbsenData("", "", ketizin, "", "", "", absenKantor, hadir, telat, lembur, acc, konfirmAdmin);
        databaseReference.child("user").child(firebaseUser.getUid()).child("sAbsensi").child(tggl).setValue(absenData)
                .addOnCompleteListener(task -> {
                    Toast.makeText(context, "Pengajuan izin berhasil di tambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
    }

    private void setTgglIzin() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date today = myCalendar.getTime();
        tgglIzin.getEditText().setText(sdf.format(today));
    }

    private boolean validateForm() {
        if (tgglIzin.getEditText().getText().toString().isEmpty()) {
            tgglIzin.setError("Isi tanggal izin anda!");
            ketIzin.setError(null);
            ketIzin.setErrorEnabled(false);
            return false;
        } else if (ketIzin.getEditText().getText().toString().isEmpty()) {
            ketIzin.setError("Isi Keterangan izin");
            tgglIzin.setError(null);
            tgglIzin.setErrorEnabled(false);
            return false;
        } else if (ketIzin.getEditText().getText().toString().length() <= 3) {
            ketIzin.setError("Keterangan anda terlalu singkat!");
            tgglIzin.setError(null);
            tgglIzin.setErrorEnabled(false);
            return false;
        } else {
            tgglIzin.setError(null);
            tgglIzin.setErrorEnabled(false);
            ketIzin.setError(null);
            ketIzin.setErrorEnabled(false);
            return true;
        }
    }

    private void cancelIzin() {
        Preferences.showDialog(context,
                null,
                "Batalkan Pengajuan Izin",
                "Apakah anda yakin ingin membatalkan pengajuan izin pada tanggal " + tgglIzin.getEditText().getText().toString() + " ?",
                "Iya",
                "Tidak",
                null,
                (dialog, which) -> {
                    // Positive Button
                    databaseReference.child("user").child(firebaseUser.getUid()).child("sAbsensi").child(idIzin).removeValue()
                            .addOnCompleteListener(task -> {
                                finish();
                                Toast.makeText(context, "Pengajuan izin di batalkan!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                },
                (dialog, which) -> {
                    // Negative Button
                    dialog.cancel();
                },
                (dialog, which) -> {
                    // Neutral Button
                    dialog.cancel();
                },
                true,
                false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (update) {
            getMenuInflater().inflate(R.menu.update_izin_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.cancel_izin:
                cancelIzin();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}