package com.absensi.inuraini.admin.datapengajuan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.datapegawai.DetailPegawai;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ApprovalActivity extends AppCompatActivity {

    ApprovalAdapter recyclerAdapter;
    Context context = this;
    ArrayList<DataApprove> listIzin = new ArrayList<>();
    CardView cardView;
    RecyclerView recyclerView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    SwipeRefreshLayout swipeRefreshLayout;
    public static String idIzin;
    String getnama, getjab;
    TextView snama, sjabatan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contentListeners();
    }

    private void contentListeners() {
        idIzin = getIntent().getStringExtra("idIzin");
        getnama = getIntent().getStringExtra("getNama");
        getjab = getIntent().getStringExtra("getJabatan");
        snama = findViewById(R.id.sNama);
        sjabatan = findViewById(R.id.sJab);
        recyclerView = findViewById(R.id.rv_view);
        cardView = findViewById(R.id.card_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = findViewById(R.id.swiper);

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailPegawai.class);
            intent.putExtra("idPegawai", idIzin);
            startActivity(intent);
        });

        snama.setText(getnama);
        sjabatan.setText(getjab);
        showData();

        Collections.sort(listIzin, DataApprove.dataApproveComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void showData(){
        databaseReference.child("user").child(idIzin).child("sAbsensi").orderByChild("sKehadiran").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listIzin = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        DataApprove izin = item.getValue(DataApprove.class);
                        if (izin != null) {
                            izin.setKey(item.getKey());
                            listIzin.add(izin);
                        }
                    }
                }
                recyclerAdapter = new ApprovalAdapter(listIzin, context);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}