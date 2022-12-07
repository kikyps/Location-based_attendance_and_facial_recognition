package com.absensi.inuraini.user.absen;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.absensi.inuraini.MyLongClickListener;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.camera.CameraActivity;
import com.absensi.inuraini.clocationlib.GetLocation;
import com.absensi.inuraini.clocationlib.OnMyLocation;
import com.absensi.inuraini.common.LoginActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AbsenFragment extends Fragment {

    TextView kehadiranTxt, inhere, tanggal, jam, absenMasuk, absenKeluar, absenNow, ketId, wktAbsenId, lemburId;
    @SuppressLint("StaticFieldLeak")
    public static Button inKantor, outKantor, absenKel;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;
    ImageButton nxt, prev;
    ImageView done;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    LinearLayout absenIn, absenOut;

    boolean sudahAbsen, isToday, izinAcc, konfirmAdmin, kehadiran, isSetTimeTr;
    public static boolean setTimeTr = false;

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    String userLogin, eventDate, jamKeluar, ketHadir;
    public static String traveler;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

//    Object[] myLatLong = new Object[3];

    static double aoiLat;
    static double aoiLong;

    static int distance;

    public static boolean doAbsen, doAbsenKeluar, atOffice, absenKantor, telat, lembur;

    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    DateFormat jamFormat = new SimpleDateFormat("HH:mm:ss");
    DateFormat jamAbsen = new SimpleDateFormat("HH:mm");
    Calendar calendar = Calendar.getInstance();
    Calendar timeNow = Calendar.getInstance();

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
        mContext = root.getContext();
        kehadiranTxt = root.findViewById(R.id.kehadiran);
        inhere = root.findViewById(R.id.ditempat);
        tanggal = root.findViewById(R.id.tanggal);
        jam = root.findViewById(R.id.jam);
        inKantor = root.findViewById(R.id.absen_kantor);
        outKantor = root.findViewById(R.id.absen_luar_kantor);
        absenIn = root.findViewById(R.id.layout_absen_masuk);
        absenOut = root.findViewById(R.id.layout_absen_keluar);
        absenKel = root.findViewById(R.id.absen_keluar);
        absenNow = root.findViewById(R.id.absen_txt);
        nxt = root.findViewById(R.id.next);
        prev = root.findViewById(R.id.previous);
        done = root.findViewById(R.id.icon_done);
        absenMasuk = root.findViewById(R.id.jam_masuk);
        absenKeluar = root.findViewById(R.id.jam_keluar);
        ketId = root.findViewById(R.id.keterangan_txt);
        wktAbsenId = root.findViewById(R.id.wkt_absen);
        lemburId = root.findViewById(R.id.lembur_txt);
        progressBar = root.findViewById(R.id.progresbar);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buttonOncreate() {
        inKantor.setOnClickListener(v -> {
            doAbsen = true;
            doAbsenKeluar = false;
            atOffice = true;
            progressBar.setVisibility(View.VISIBLE);
            Preferences.getMyLocation(mContext, getActivity());
        });

        // Tombol absen di kantor
        outKantor.setOnClickListener(v -> {
            doAbsen = true;
            doAbsenKeluar = false;
            atOffice = false;
            progressBar.setVisibility(View.VISIBLE);

            // check lokasi
            Preferences.getMyLocation(mContext, getActivity());
        });

        // Tombol absen di luar kantor
        absenKel.setOnClickListener(v -> {
            doAbsen = false;
            doAbsenKeluar = true;
            progressBar.setVisibility(View.VISIBLE);

            // check lokasi
            Preferences.getMyLocation(mContext, getActivity());
        });

        nxt.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, 1);
            setTanggal();
        });

        nxt.setOnTouchListener(new MyLongClickListener(4000) {
            @Override
            public void onLongClick() {
//                throw new RuntimeException("Test Crash"); // Force a crash
                Preferences.doRestart(mContext);
            }
        });

        prev.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, -1);
            setTanggal();
//            Toast.makeText(mContext, GetLocation.MyAddress() != null ? "Alamat : " +  GetLocation.MyAddress().getAddressLine(0) : "", Toast.LENGTH_SHORT).show();
        });

        prev.setOnTouchListener(new MyLongClickListener(2000) {
            @Override
            public void onLongClick() {
//                throw new RuntimeException("Boom!");
//                startActivity(new Intent(mContext, SpotTestActivity.class));
//                checkLokasi();
                GetLocation location = new GetLocation(mContext, getActivity());
                location.setAcceptMockProvider(false);
                location.getMyLocation(new OnMyLocation() {
                    @Override
                    public void onCurrentLocation(Location location, Address address) {
                        Toast.makeText(mContext, "Kota : " + address.getThoroughfare(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int errorCode, String msg) {

                    }
                });
            }
        });

        DatePickerDialog.OnDateSetListener date = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setTanggal();
        };

        tanggal.setOnClickListener(v -> {
            calendar.setTime(timeNow.getTime());
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.my_dialog_theme, date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(timeNow.getTimeInMillis());
            datePickerDialog.show();
        });


        jam.setOnLongClickListener(v -> {
                if (isSetTimeTr) {
                    if (Preferences.getDataStatus(mContext).equals("admin") ||
                            Preferences.getDataStatus(mContext).equals(Preferences.retriveSec("yVGdzFWb"))) {
                        setTimeTrav();
                    }
                }
            return true;
        });
    }

    private void setTimeTrav() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_time_picker, null);
        builder.setView(dialogView);
        builder.setTitle("Time setter");
        TimePicker timePick = dialogView.findViewById(R.id.timePicker1);
        timePick.setIs24HourView(true);
        builder.setPositiveButton("Set", (dialog, which) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String hour = String.valueOf(timePick.getHour()).length() < 2 ? "0" + timePick.getHour() : String.valueOf(timePick.getHour());
                String minute = String.valueOf(timePick.getMinute()).length() < 2 ? "0" + timePick.getMinute() : String.valueOf(timePick.getMinute());
                traveler = hour + ":" + minute;
                setTimeTr = true;
                jam.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Reset", (dialog, which) -> {
            setTimeTr = false;
            jam.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
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
                    Preferences.signOut(mContext, true, LoginActivity.class);
                });
        builder.setCancelable(false);
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
            try {
                String waktuMasuk = "07:00:00";
                Date time1 = new SimpleDateFormat("HH:mm:ss").parse(waktuMasuk);
                Calendar absenMasuk = Calendar.getInstance();
                absenMasuk.setTime(time1);

                String waktuTelat = "07:15:00";
                Date time2 = new SimpleDateFormat("HH:mm:ss").parse(waktuTelat);
                Calendar absenTelat = Calendar.getInstance();
                absenTelat.setTime(time2);

                String waktuKeluar = "16:00:00";
                Date time3 = new SimpleDateFormat("HH:mm:ss").parse(waktuKeluar);
                Calendar absenKeluar = Calendar.getInstance();
                absenKeluar.setTime(time3);

                String waktuLembur = "17:00:00";
                Date time4 = new SimpleDateFormat("HH:mm:ss").parse(waktuLembur);
                Calendar absenLembur = Calendar.getInstance();
                absenLembur.setTime(time4);

                String waktuAkhir = "23:59:59";
                Date time5 = new SimpleDateFormat("HH:mm:ss").parse(waktuAkhir);
                Calendar absenAkhir = Calendar.getInstance();
                absenAkhir.setTime(time5);

                Date timeTrav;

                if (setTimeTr){
                    Date getTimeTrav = new SimpleDateFormat("HH:mm").parse(traveler);
                    Calendar calTrav = Calendar.getInstance();
                    calTrav.setTime(getTimeTrav);

                    timeTrav = calTrav.getTime();

                    jam.setText(jamFormat.format(timeTrav.getTime()));
                } else {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    java.util.Date currenttime = dateFormat
                            .parse(dateFormat.format(new java.util.Date()));
                    Calendar currentcal = Calendar.getInstance();
                    currentcal.setTime(currenttime);

                    timeTrav = currentcal.getTime();
                    jam.setText(jamFormat.format(timeTrav.getTime()));
                }

                if (timeTrav.after(absenMasuk.getTime()) && timeTrav.before(absenTelat.getTime())) {
                    //checkes whether the current time is between 14:49:00 and 20:11:13.
                    absenNow.setVisibility(View.VISIBLE);
                    absenNow.setText("Absen Masuk");
                    telat = false;
                    lembur = false;
                    if (sudahAbsen){
                        if (kehadiran){
                            absenIn.setVisibility(View.VISIBLE);
                            absenOut.setVisibility(View.GONE);
                        } else {
                            absenNow.setVisibility(View.GONE);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        }
                    } else {
                        absenIn.setVisibility(View.VISIBLE);
                        absenOut.setVisibility(View.GONE);
                    }
                } else if (timeTrav.after(absenTelat.getTime()) && timeTrav.before(absenKeluar.getTime())){
                    telat = true;
                    lembur = false;
                    if (sudahAbsen){
                        if (kehadiran){
                            absenNow.setVisibility(View.VISIBLE);
                            absenNow.setText("Absen Masuk");
                            absenIn.setVisibility(View.VISIBLE);
                            absenOut.setVisibility(View.GONE);
                        } else {
                            absenNow.setVisibility(View.GONE);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        }
                    } else {
                        absenNow.setVisibility(View.VISIBLE);
                        absenNow.setText("Absen Masuk");
                        absenIn.setVisibility(View.VISIBLE);
                        absenOut.setVisibility(View.GONE);
                    }
                } else if (timeTrav.after(absenKeluar.getTime()) && timeTrav.before(absenLembur.getTime())) {
                    lembur = false;
                    if (sudahAbsen) {
                        if (kehadiran){
                            if (jamKeluar.isEmpty()) {
                                absenNow.setVisibility(View.VISIBLE);
                                absenNow.setText("Absen Keluar");
                                absenIn.setVisibility(View.GONE);
                                absenOut.setVisibility(View.VISIBLE);
                            } else {
                                absenNow.setVisibility(View.GONE);
                                absenIn.setVisibility(View.GONE);
                                absenOut.setVisibility(View.GONE);
                            }
                        } else {
                            absenNow.setVisibility(View.GONE);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        }
                    } else {
                        if (isToday) {
                            absenNow.setVisibility(View.VISIBLE);
                            absenNow.setText("Anda tidak dapat absen di luar jam kerja");
                            absenNow.setTextSize(13);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        } else {
                            absenNow.setVisibility(View.GONE);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        }
                    }
                } else if (timeTrav.after(absenLembur.getTime()) && timeTrav.before(absenAkhir.getTime())){
                    lembur = true;
                    if (sudahAbsen) {
                        if (kehadiran){
                            if (jamKeluar.isEmpty()) {
                                absenNow.setVisibility(View.VISIBLE);
                                absenNow.setText("Absen Keluar");
                                absenIn.setVisibility(View.GONE);
                                absenOut.setVisibility(View.VISIBLE);
                            } else {
                                absenNow.setVisibility(View.GONE);
                                absenIn.setVisibility(View.GONE);
                                absenOut.setVisibility(View.GONE);
                            }
                        } else {
                            absenNow.setVisibility(View.GONE);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        }
                    } else {
                        if (isToday) {
                            absenNow.setVisibility(View.VISIBLE);
                            absenNow.setText("Anda tidak dapat absen di luar jam kerja");
                            absenNow.setTextSize(13);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        } else {
                            absenNow.setVisibility(View.GONE);
                            absenIn.setVisibility(View.GONE);
                            absenOut.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (isToday) {
                        absenNow.setVisibility(View.VISIBLE);
                        absenNow.setText("Anda tidak dapat absen di luar jam kerja");
                        absenNow.setTextSize(13);
                        absenIn.setVisibility(View.GONE);
                        absenOut.setVisibility(View.GONE);
                    } else {
                        absenNow.setVisibility(View.GONE);
                        absenIn.setVisibility(View.GONE);
                        absenOut.setVisibility(View.GONE);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            handler.postDelayed(runnable, 100);
        };
        handler.postDelayed(runnable, 100);
    }

    private void checkLokasi() {
        Object[][] myLatLong = Preferences.getMyLocation(mContext, getActivity());
        Toast.makeText(mContext, "Latitude = " + myLatLong[0][0] + " Longitude = " + myLatLong[0][1], Toast.LENGTH_SHORT).show();
        Toast.makeText(mContext, (CharSequence) myLatLong[1][12], Toast.LENGTH_LONG).show();
    }

    // Check absensi di kantor atau di luar kantor
    public static void checkAbsenKantor(){
        if (atOffice) {
            if (!inLocation()) {
                outKantor.setEnabled(true);
                inKantor.setClickable(true);
                inKantor.setBackgroundColor(Color.RED);
                Toast.makeText(mContext, "Anda tidak berada di lokasi!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(mContext, CameraActivity.class);
                intent.putExtra("faceid", false);
                intent.putExtra("atOffice", true);
                intent.putExtra("telat", telat);
                intent.putExtra("lembur", lembur);
                mContext.startActivity(intent);
            }
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            if (inLocation()) {
                outKantor.setEnabled(true);
                inKantor.setClickable(true);
                inKantor.setBackgroundColor(Color.CYAN);
                Preferences.showDialog(mContext,
                        null,
                        "Pemberitahuan!",
                        "Anda saat ini berada di kantor, jika anda ingin absen di kantor pilih absen kantor!",
                        "Okey",
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
                        false,
                        false);
            } else {
                Intent intent = new Intent(mContext, CameraActivity.class);
                intent.putExtra("faceid", false);
                intent.putExtra("atOffice", false);
                intent.putExtra("telat", telat);
                intent.putExtra("lembur", lembur);
                mContext.startActivity(intent);
            }
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public static void checkAbsenKeluar(){
        if (absenKantor) {
            if (!inLocation()) {
                outKantor.setEnabled(true);
                inKantor.setClickable(true);
                inKantor.setBackgroundColor(Color.RED);
                Toast.makeText(mContext, "Anda tidak berada di kantor!!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(mContext, CameraActivity.class);
                intent.putExtra("faceid", false);
                intent.putExtra("absenOut", true);
                intent.putExtra("telat", telat);
                intent.putExtra("lembur", lembur);
                mContext.startActivity(intent);
            }
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            Intent intent = new Intent(mContext, CameraActivity.class);
            intent.putExtra("faceid", false);
            intent.putExtra("absenOut", true);
            intent.putExtra("telat", telat);
            intent.putExtra("lembur", lembur);
            mContext.startActivity(intent);
            progressBar.setVisibility(View.INVISIBLE);
        }
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
                    sudahAbsen = true;
                    String jamMasuk = snapshot.child("sJamMasuk").getValue().toString();
                    jamKeluar = snapshot.child("sJamKeluar").getValue().toString();
                    ketHadir = snapshot.child("sKet").getValue().toString();
                    absenKantor = (boolean) snapshot.child("sKantor").getValue();
                    kehadiran = (boolean) snapshot.child("sKehadiran").getValue();
                    boolean terlambatMasuk = (boolean) snapshot.child("sTerlambat").getValue();
                    boolean jamLembur = (boolean) snapshot.child("sLembur").getValue();
                    izinAcc = (boolean) snapshot.child("sAcc").getValue();
                    konfirmAdmin = (boolean) snapshot.child("sKonfirmAdmin").getValue();

                    if (kehadiran) {
                        kehadiranTxt.setText("Hadir");
                        absenMasuk.setText(jamMasuk);
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
                            validInKantor();
                        } else {
                            validLuarKantor();
                        }
                    } else {
                        validIzin();
                    }
                } else {
                    sudahAbsen = false;
                    seleksiAbsen();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validInKantor() {
        if (jamKeluar.isEmpty()){
            absenKeluar.setText("-");
            done.setVisibility(View.VISIBLE);
            done.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_access_time_24));
            isSetTimeTr = true;
        } else {
            absenKeluar.setText(jamKeluar);
            done.setVisibility(View.VISIBLE);
            done.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.avd_done));
            Drawable drawable = done.getDrawable();

            if (drawable instanceof AnimatedVectorDrawableCompat) {
                avd = (AnimatedVectorDrawableCompat) drawable;
                avd.start();
            } else if (drawable instanceof AnimatedVectorDrawable) {
                avd2 = (AnimatedVectorDrawable) drawable;
                avd2.start();
            }
            isSetTimeTr = false;
        }

        inhere.setText("Absen di kantor");
        ketId.setText("-");
        outKantor.setEnabled(false);
        inKantor.setClickable(false);
        inKantor.setEnabled(true);
        inKantor.setBackgroundColor(Color.GREEN);
        outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void validLuarKantor() {
        if (jamKeluar.isEmpty()){
            absenKeluar.setText("-");
            done.setVisibility(View.VISIBLE);
            done.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_access_time_24));
            isSetTimeTr = true;
        } else {
            absenKeluar.setText(jamKeluar);
            done.setVisibility(View.VISIBLE);
            done.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.avd_done));
            Drawable drawable = done.getDrawable();

            if (drawable instanceof AnimatedVectorDrawableCompat) {
                avd = (AnimatedVectorDrawableCompat) drawable;
                avd.start();
            } else if (drawable instanceof AnimatedVectorDrawable) {
                avd2 = (AnimatedVectorDrawable) drawable;
                avd2.start();
            }
            isSetTimeTr = false;
        }

        inhere.setText("Absen di luar kantor");
        ketId.setText("-");
        inKantor.setEnabled(false);
        outKantor.setEnabled(true);
        outKantor.setClickable(false);
        outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.green));
        inKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void validIzin(){
        if (konfirmAdmin) {
            if (izinAcc) {
                done.setVisibility(View.INVISIBLE);
                kehadiranTxt.setText("Izin");
                ketId.setText(ketHadir);
                absenMasuk.setText("-");
                wktAbsenId.setText("-");
                absenKeluar.setText("-");
                lemburId.setText("-");
                inhere.setText("-");
                outKantor.setEnabled(true);
                outKantor.setClickable(false);
                inKantor.setEnabled(false);
                inKantor.setClickable(true);
                inKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
                outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
            } else {
                done.setVisibility(View.INVISIBLE);
                kehadiranTxt.setText("Izin");
                ketId.setText(ketHadir);
                absenMasuk.setText("-");
                wktAbsenId.setText("-");
                absenKeluar.setText("-");
                lemburId.setText("-");
                inhere.setText("-");
                outKantor.setEnabled(true);
                outKantor.setClickable(false);
                inKantor.setEnabled(false);
                inKantor.setClickable(true);
                inKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
                outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
            }
        } else {
            done.setVisibility(View.INVISIBLE);
            kehadiranTxt.setText("Izin (Belum Dikonfirmasi Admin)");
            ketId.setText(ketHadir);
            absenMasuk.setText("-");
            wktAbsenId.setText("-");
            absenKeluar.setText("-");
            lemburId.setText("-");
            inhere.setText("-");
            outKantor.setEnabled(true);
            outKantor.setClickable(false);
            inKantor.setEnabled(false);
            inKantor.setClickable(true);
            inKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
            outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
        }
    }

    private void validNoData() {
        done.setVisibility(View.INVISIBLE);
        outKantor.setEnabled(false);
        inKantor.setEnabled(false);
        inKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
        outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.shot_black));
    }

    private void belumAbsen() {
        done.setVisibility(View.INVISIBLE);
        outKantor.setEnabled(true);
        outKantor.setClickable(true);
        inKantor.setEnabled(true);
        inKantor.setClickable(true);
        outKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_500));
        inKantor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_500));
    }

    private void seleksiAbsen() {
        String curentDate = dateFormat.format(calendar.getTime());
        String tgglNow = dateFormat.format(timeNow.getTime());
        if (curentDate.equals(tgglNow)) {
            isToday = true;
            isSetTimeTr = true;
            kehadiranTxt.setText("Anda belum absen hari ini!");
            nxt.setEnabled(false);
            nxt.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_next_disabled));
            belumAbsen();
            absenMasuk.setText("-");
            ketId.setText("-");
            wktAbsenId.setText("-");
            absenKeluar.setText("-");
            lemburId.setText("-");
            inhere.setText("-");
        } else {
            absenNow.setVisibility(View.GONE);
            absenOut.setVisibility(View.GONE);
            isToday = false;
            setTimeTr = false;
            isSetTimeTr = false;
            jam.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            kehadiranTxt.setText("Tidak Hadir!");
            nxt.setEnabled(true);
            nxt.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_next));
            validNoData();
            absenMasuk.setText("-");
            ketId.setText("Tidak ada data absen!");
            wktAbsenId.setText("-");
            absenKeluar.setText("-");
            lemburId.setText("-");
            inhere.setText("-");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}