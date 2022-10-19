package com.absensi.inuraini.admin.datapengajuan;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.user.pengajuan.PengajuanLiburAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DataPengajuan extends Fragment {

    DataPengajuanAdapter recyclerAdapter;
    Context mContext;
    ArrayList<DataReqIzin> listReqIzin = new ArrayList<>();
    RecyclerView recyclerView;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_pengajuan, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        contentLiteners();
        return root;
    }

    private void layoutBinding(View root) {
        firebaseUser = Preferences.mAuth.getCurrentUser();
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        recyclerView.setVisibility(View.GONE);
        showData();
        Runnable runnable = () -> recyclerView.setVisibility(View.VISIBLE);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, 1300);
    }

    private void contentLiteners() {
//        Collections.sort(listIzin, DataIzin.dataIzinComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recyclerView.setVisibility(View.GONE);
            showData();
            Runnable runnable = () -> recyclerView.setVisibility(View.VISIBLE);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(runnable, 1300);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showData(){
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