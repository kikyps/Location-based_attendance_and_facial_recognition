package com.absensi.inuraini.user.pengajuan;

import android.content.Context;
import android.content.Intent;
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

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class PengajuanLibur extends Fragment {
    PengajuanLiburAdapter recyclerAdapter;
    FloatingActionButton fab;
    Context mContext;
    ArrayList<DataIzin> listIzin = new ArrayList<>();
    RecyclerView recyclerView;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pengajuan_libur, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        contentLiteners();
        return root;
    }

    private void layoutBinding(View root) {
        firebaseUser = Preferences.mAuth.getCurrentUser();
        fab = root.findViewById(R.id.fab);
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        showData();
    }

    private void contentLiteners() {
        fab.setOnClickListener(v -> {
            startActivity(new Intent(mContext, TambahIzinActivity.class));
        });

        Collections.sort(listIzin, DataIzin.dataIzinComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showData(){
        databaseReference.child("user").child(firebaseUser.getUid()).child("sAbsensi").orderByChild("sKehadiran").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listIzin = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        DataIzin izin = item.getValue(DataIzin.class);
                        if (izin != null) {
                            izin.setKey(item.getKey());
                            listIzin.add(izin);
                        }
                    }
                }
                recyclerAdapter = new PengajuanLiburAdapter(listIzin, getActivity());
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