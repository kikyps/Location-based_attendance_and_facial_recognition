package com.absensi.inuraini.admin.verifyaccount;

import android.content.Context;
import android.os.Build;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class VerifyAccountFragment extends Fragment {
    VerifyRecyclerAdapter recyclerAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<DataVerify> listAkun = new ArrayList<>();
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verif_account, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        contentListeners();
        return root;
    }

    private void layoutBinding(View root) {
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Preferences.customProgresBar(root);
        }
        showData();
    }

    private void contentListeners() {
        Collections.sort(listAkun, DataVerify.dataVerifyComparator);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showData() {
        databaseReference.child("user").orderByChild("sVerified").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listAkun = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DataVerify dataVerify = dataSnapshot.getValue(DataVerify.class);
                        if (dataVerify != null) {
                            dataVerify.setKey(dataSnapshot.getKey());
                        }
                        listAkun.add(dataVerify);
                        Preferences.progressDialog.dismiss();
                    }
                } else {
                    Preferences.progressDialog.dismiss();
                }
                recyclerAdapter = new VerifyRecyclerAdapter(listAkun, getActivity());
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