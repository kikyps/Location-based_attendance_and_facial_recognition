package com.absensi.inuraini.common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.absensi.inuraini.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataDiriTwo extends AppCompatActivity {

    ImageView back;
    Button next;
    RadioGroup jk;
    RadioButton pria, wanita;
    Date today;
    final Calendar myCalendar = Calendar.getInstance();
    DatePicker datePicker;
    public static String gender, ttlku;
    Calendar calendar = Calendar.getInstance();
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_diri_two);
        back = findViewById(R.id.back_button);
        jk = findViewById(R.id.gender);
        pria = findViewById(R.id.male);
        wanita = findViewById(R.id.famale);
        datePicker = findViewById(R.id.date_picker);
        next = findViewById(R.id.next3);
        myCalendar.add(Calendar.YEAR, -18);
        today = myCalendar.getTime();

        contentListeners();
    }

    private void contentListeners() {
        datePicker.setMaxDate(System.currentTimeMillis());
        back.setOnClickListener(v -> {
            startActivity(new Intent(this, DataDiriOne.class));
        });
        calendar.setTimeInMillis(System.currentTimeMillis());

        next.setOnClickListener(v -> {
            if (gender() && ttl()){
                int tgl = datePicker.getDayOfMonth();
                int bulan = datePicker.getMonth();
                int tahun = datePicker.getYear();
                calendar.set(tahun, bulan, tgl);
                String curentDate = dateFormat.format(calendar.getTime());
                ttlku = curentDate;

                startActivity(new Intent(this, DataDiriThree.class));
            }
        });
    }

    boolean gender(){
        if (pria.isChecked()){
            gender = pria.getText().toString();
            return true;
        } else if (wanita.isChecked()){
            gender = wanita.getText().toString();
            return true;
        } else {
            Toast.makeText(this, "Pilih Jenis kelamin", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    boolean ttl(){
        int year = myCalendar.get(Calendar.YEAR);

        if (datePicker.getYear() > year){
            Toast.makeText(this, "Umur anda tidak mencukupi", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}