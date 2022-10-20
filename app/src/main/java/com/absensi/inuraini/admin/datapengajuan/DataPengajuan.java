package com.absensi.inuraini.admin.datapengajuan;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
    LinearLayout progressLayout;
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
        progressLayout = root.findViewById(R.id.progresLayout);
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = root.findViewById(R.id.swiper);
//        progressLayout.setVisibility(View.VISIBLE);
//        showData();
    }

    private void contentLiteners() {
//        Collections.sort(listIzin, DataIzin.dataIzinComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressLayout.setVisibility(View.VISIBLE);
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        progressLayout.setVisibility(View.VISIBLE);
        showData();
    }

    private void displayProgressBar(boolean visible) {
        RelativeLayout layout = new RelativeLayout(getActivity());
        layout.setBackgroundColor(Color.TRANSPARENT);
        ProgressBar progressBar = new ProgressBar(getActivity(),null,android.R.attr.progressBarStyleLarge);
        progressBar.setBackgroundColor(Color.TRANSPARENT);
        progressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        if (visible) {
            layout.addView(progressBar, params);
            getActivity().setContentView(layout);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
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
                Runnable runnable = () -> {
                    progressLayout.setVisibility(View.GONE);
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