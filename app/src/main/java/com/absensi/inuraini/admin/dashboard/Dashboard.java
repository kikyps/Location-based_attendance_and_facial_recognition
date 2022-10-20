package com.absensi.inuraini.admin.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.datapengajuan.DataPengajuanAdapter;
import com.absensi.inuraini.admin.datapengajuan.DataReqIzin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Dashboard extends Fragment {

    TextView dataPegawaiCount, dataUserCount, dataAdminCount, dataVerifCount, dataIzinCount;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DataPengajuanAdapter recyclerAdapter;
    ProgressBar progressBar;
    Context mContext;
    RecyclerView recyclerView;
    ArrayList<DataReqIzin> listReqIzin = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        return root;
    }

    private void layoutBinding(View root) {
        progressBar = root.findViewById(R.id.progresbar);
        dataPegawaiCount = root.findViewById(R.id.jumlah_pegawai);
        dataUserCount = root.findViewById(R.id.jumlah_user);
        dataAdminCount = root.findViewById(R.id.jumlah_admin);
        dataVerifCount = root.findViewById(R.id.jumlah_verif_acc);
        dataIzinCount = root.findViewById(R.id.jumlah_req_izin);
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        contentListeners();
    }

    private void contentListeners() {
        getsPegawaiCount();
        getsUserCount();
        getsAdminCount();
        getsDataizin();
        getsVerifAccountCount();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            getsPegawaiCount();
            getsUserCount();
            getsAdminCount();
            getsDataizin();
            getsVerifAccountCount();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void getsPegawaiCount(){
        databaseReference.child("user").orderByChild("sVerified").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = (int) snapshot.getChildrenCount();
                    dataPegawaiCount.setText(String.valueOf(count));
                } else {
                    dataPegawaiCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getsUserCount(){
        databaseReference.child("user").orderByChild("sStatus").equalTo("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = (int) snapshot.getChildrenCount();
                    dataUserCount.setText(String.valueOf(count));
                } else {
                    dataUserCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getsAdminCount(){
        databaseReference.child("user").orderByChild("sStatus").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = (int) snapshot.getChildrenCount();
                    dataAdminCount.setText(String.valueOf(count));
                } else {
                    dataAdminCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getsVerifAccountCount(){
        databaseReference.child("user").orderByChild("sVerified").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = (int) snapshot.getChildrenCount();
                    dataVerifCount.setText(String.valueOf(count));
                } else {
                    dataVerifCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getsDataizin(){
        dataIzinCount.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.child("user").orderByChild("sVerified").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listReqIzin = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        DataReqIzin rekap = item.getValue(DataReqIzin.class);
                        if (rekap != null) {
                            if (rekap.getsStatus().equals("user")) {
                                rekap.setKey(item.getKey());
                                listReqIzin.add(rekap);
                            }
                        }
                    }
                } else {
                    Toast.makeText(mContext, "Kosong", Toast.LENGTH_SHORT).show();
                }
                recyclerAdapter = new DataPengajuanAdapter(listReqIzin, getActivity());
                recyclerView.setAdapter(recyclerAdapter);
                Runnable runnable = () -> {
                  dataIzinCount.setText(DataPengajuanAdapter.countIzin);
                  dataIzinCount.setVisibility(View.VISIBLE);
                  progressBar.setVisibility(View.INVISIBLE);
                };
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(runnable, 2500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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