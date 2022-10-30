package com.absensi.inuraini.admin.datapegawai;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.absensi.inuraini.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class DataPegawai extends Fragment {
    PegawaiRecyclerAdapter recyclerAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<StoreDataPegawai> listPegawai = new ArrayList<>();
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_pegawai, container, false);
        // Inflate the layout for this fragment
        contentBinding(root);
        contentListeners();
//        setHasOptionsMenu(true);
        return root;
    }

    private void contentBinding(View root) {
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        showData();
    }

    private void contentListeners() {
        Collections.sort(listPegawai, StoreDataPegawai.storePegawaiComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showData() {
        databaseReference.child("user").orderByChild("sVerified").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listPegawai = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        StoreDataPegawai pegawai = item.getValue(StoreDataPegawai.class);
                        if (pegawai != null) {
                            if (pegawai.getsStatus().equals("user") || pegawai.getsStatus().equals("admin")) {
                                pegawai.setKey(item.getKey());
                                listPegawai.add(pegawai);
                            }
                        }
                    }
                }
                recyclerAdapter = new PegawaiRecyclerAdapter(listPegawai, getActivity());
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