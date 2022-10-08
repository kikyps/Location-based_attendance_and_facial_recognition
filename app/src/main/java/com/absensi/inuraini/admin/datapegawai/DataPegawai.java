package com.absensi.inuraini.admin.datapegawai;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    ProgressDialog progressDialog;
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
        customProgresBar();
        showData();
    }

    private void contentListeners() {
        Collections.sort(listPegawai, StoreDataPegawai.storePegawaiComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void customProgresBar(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.cutom_progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
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
                            pegawai.setKey(item.getKey());
                        }
                        listPegawai.add(pegawai);
                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                }
                recyclerAdapter = new PegawaiRecyclerAdapter(listPegawai, mContext);
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