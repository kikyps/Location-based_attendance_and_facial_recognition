package com.absensi.inuraini.admin.rekap;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.absensi.inuraini.MyLongClickListener;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class RekapAbsen extends Fragment {

    AbsenRecyclerAdapter absenRecyclerAdapter;
    ArrayList<DataStore> listUser = new ArrayList<>();
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    EditText filterData;
    ImageButton nxt, prev;
    LinearLayout dateArrow;
    TextView tanggal;
    private Spinner rekapSpinner;
    String getSelectedRekap;
    public static String eventDate, userLogin;
    public static DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    public static Calendar calendar = Calendar.getInstance();
    private Context mContext;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rekap_absen, container, false);
        // Inflate the layout for this fragment
        layoutbinding(root);
        listenerAction();
        showData();
        setTanggal();
        return root;
    }

    private void layoutbinding(View root) {
        firebaseUser = Preferences.mAuth.getCurrentUser();
        userLogin = firebaseUser.getUid();
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayout);
//        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        filterData = root.findViewById(R.id.filter_data);
        tanggal = root.findViewById(R.id.tanggal);
        nxt = root.findViewById(R.id.next);
        prev = root.findViewById(R.id.previous);
        dateArrow = root.findViewById(R.id.date_rekap);
        rekapSpinner = root.findViewById(R.id.admin_user);
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
        Collections.sort(listUser, DataStore.dataStoreComparator);
    }

    private void listenerAction(){
        filterData.addTextChangedListener(filter);
//        dateArrow.setVisibility(View.GONE);

        nxt.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, 1);
            setTanggal();
        });

        prev.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, -1);
            setTanggal();
        });

        String[] rekapData = getResources().getStringArray(R.array.pilih_rekap);
        ArrayAdapter<String> pilihRekap = new ArrayAdapter<>(mContext, R.layout.spinner_data, rekapData);
        rekapSpinner.setAdapter(pilihRekap);
        rekapSpinner.setSelection(1);

        rekapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSelectedRekap = rekapSpinner.getSelectedItem().toString().toLowerCase(Locale.ROOT);
                showData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && dy != 0) {
                    //Load more items here
//                    Toast.makeText(mContext, "End of item!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private final TextWatcher filter = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (absenRecyclerAdapter != null){
                absenRecyclerAdapter.getFilter().filter(charSequence);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }

    private void setTanggal(){
        String curentDate = dateFormat.format(calendar.getTime());
        eventDate = dateRekap.format(calendar.getTime());
        tanggal.setText(curentDate);
        seleksiAbsen();
        showData();
    }

    private void seleksiAbsen(){
        String curentDate = dateFormat.format(calendar.getTime());
        String tgglNow = dateFormat.format(new Date().getTime());
        if (curentDate.equals(tgglNow)){
            nxt.setEnabled(false);
            nxt.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_next_disabled));
        } else {
            nxt.setEnabled(true);
            nxt.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_next));
        }
    }

    private void showData(){
        databaseReference.orderByChild("sVerified").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUser = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        DataStore rekap = item.getValue(DataStore.class);
                        if (rekap != null) {
                            if (rekap.getsStatus().toLowerCase().equals(getSelectedRekap)) {
                                rekap.setKey(item.getKey());
                                listUser.add(rekap);
                            }
                        }
                    }
                }
                absenRecyclerAdapter = new AbsenRecyclerAdapter(listUser, getActivity());
                recyclerView.setAdapter(absenRecyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}