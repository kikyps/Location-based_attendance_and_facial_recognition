package com.absensi.inuraini.user.absen;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.absensi.inuraini.MyLongClickListener;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.common.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;
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

public class AbsenFragment extends Fragment {

    TextInputLayout ket;
    TextView inhere, tanggal, jam, waktuAbsen, myAddresss;
    @SuppressLint("StaticFieldLeak")
    public static Button hadir, izin;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;
    ImageButton nxt, prev;
    ImageView done;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;

    private Context mContext;

    String userLogin, eventDate;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    Object[] myLatLong = new Object[3];

    static double aoiLat;
    static double aoiLong;

    static int distance;

    public static boolean getloc;

    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    DateFormat jamFormat = new SimpleDateFormat("HH:mm:ss");
    DateFormat jamAbsen = new SimpleDateFormat("HH:mm");
    Calendar calendar = Calendar.getInstance();

    private final Handler handler = new Handler();
    private Runnable runnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_absen, container, false);
        layoutBinding(root);
        setJam();
        setTanggal();
        buttonOncreate();
        progressBar.setVisibility(View.INVISIBLE);
        return root;
    }

    private void layoutBinding(View root) {
        firebaseUser = Preferences.mAuth.getCurrentUser();
        userLogin = firebaseUser.getUid();
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

    private void buttonOncreate() {
        myAddresss.setVisibility(View.GONE);

        hadir.setOnClickListener(v -> {
            getloc = true;
            progressBar.setVisibility(View.VISIBLE);
            Preferences.getMyLocation(mContext, getActivity());
        });

        if (Preferences.getDataStatus(mContext).equals("admin")) {
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

    @Override
    public void onStart() {
        super.onStart();
        getLatlong();
        setTanggal();
    }

    private void getLatlong() {
        DatabaseReference dataLatlong = FirebaseDatabase.getInstance().getReference();
        dataLatlong.child("data").child("latlong").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    comeBack();
                } else if (snapshot.child("sLatitude").getValue().toString().isEmpty() && snapshot.child("sLongitude").getValue().toString().isEmpty() && !snapshot.child("sDistance").getValue().toString().isEmpty()) {
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

    private void comeBack() {
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

            if (keterangan.isEmpty()) {
                Toast.makeText(mContext, "Isi Keterangan Terlebih Dahulu!", Toast.LENGTH_SHORT).show();
            } else {
                AbsenData absenData = new AbsenData(stathadir, jamAbsen, keterangan);
                databaseReference.child(userLogin).child("sAbsensi").child(eventDate).setValue(absenData).addOnSuccessListener(unused -> validIzin()).addOnFailureListener(e -> Toast.makeText(requireContext(), "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setTanggal() {
        String curentDate = dateFormat.format(calendar.getTime());
        eventDate = dateRekap.format(calendar.getTime());
        tanggal.setText(curentDate);
        showAbsenToday();
        seleksiAbsen();
    }

    private void setJam() {
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

    private void checkLokasi() {
        myLatLong = Preferences.getMyLocation(mContext, getActivity());
        Toast.makeText(mContext, "Latitude = " + myLatLong[0] + " Longitude = " + myLatLong[1], Toast.LENGTH_SHORT).show();
        Toast.makeText(mContext, (CharSequence) myLatLong[2], Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLatlong();
        setTanggal();
    }

    public static boolean inLocation() {
        float[] results = new float[1];
        Location.distanceBetween(aoiLat, aoiLong, Preferences.latitude, Preferences.longitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters < distance;
    }

    private void showAbsenToday() {
        databaseReference.child(userLogin).child("sAbsensi").child(eventDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String kehadiran = snapshot.child("sKehadiran").getValue().toString();
                    String jamAbsen = snapshot.child("sJam").getValue().toString();
                    String ketHadir = snapshot.child("sKet").getValue().toString();

                    if (kehadiran.equals("hadir")) {
                        waktuAbsen.setText(jamAbsen);
                        inhere.setText(ketHadir);
                        validHadir();
                    } else if (kehadiran.equals("izin")) {
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

    private void validHadir() {
        done.setVisibility(View.VISIBLE);
        Drawable drawable = done.getDrawable();

        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }

        izin.setEnabled(false);
        hadir.setClickable(false);
        hadir.setEnabled(true);
        hadir.setBackgroundColor(Color.GREEN);
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void validIzin() {
        done.setVisibility(View.INVISIBLE);
        hadir.setEnabled(false);
        izin.setEnabled(true);
        izin.setClickable(false);
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
        hadir.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void validNoData() {
        done.setVisibility(View.INVISIBLE);
        izin.setEnabled(false);
        hadir.setEnabled(false);
        hadir.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void belumAbsen() {
        done.setVisibility(View.INVISIBLE);
        izin.setEnabled(true);
        izin.setClickable(true);
        hadir.setEnabled(true);
        hadir.setClickable(true);
        izin.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_500));
        hadir.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_500));
    }

    private void seleksiAbsen() {
        String curentDate = dateFormat.format(calendar.getTime());
        String tgglNow = dateFormat.format(new Date().getTime());
        if (curentDate.equals(tgglNow)) {
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