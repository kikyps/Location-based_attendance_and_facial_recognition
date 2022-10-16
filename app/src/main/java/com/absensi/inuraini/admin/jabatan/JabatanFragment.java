package com.absensi.inuraini.admin.jabatan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.absensi.inuraini.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class JabatanFragment extends Fragment {
    JabatanRecyclerAdapter recyclerAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<StoreJabatan> listJabatan = new ArrayList<>();
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog progressDialog;
    FloatingActionButton fab;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_jabatan, container, false);
        // Inflate the layout for this fragment
        contentBinding(root);
        contentListeners();
        setHasOptionsMenu(true);
        return root;
    }

    private void contentBinding(View root) {
        recyclerView = root.findViewById(R.id.rv_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = root.findViewById(R.id.swiper);
        fab = root.findViewById(R.id.fab);
        customProgresBar();
        showData();
    }

    private void contentListeners() {
        Collections.sort(listJabatan, StoreJabatan.storejabatanComparator);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE){
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE){
                    fab.show();
                }
            }
        });

        fab.setOnClickListener(v ->{
            Intent intent = new Intent(mContext, TambahJabatan.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("tambah", true);
            mContext.startActivities(new Intent[]{intent});
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void customProgresBar(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.jabatan_menu, menu);
        MenuItem item = menu.findItem(R.id.filter_jabatan);
        SearchView searchViewBagian = (SearchView) item.getActionView();
        searchViewBagian.setQueryHint("Search");
        searchViewBagian.setIconified(false);
        searchViewBagian.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                recyclerAdapter.getFilter().filter(s);
                return false;
            }
        });
        searchViewBagian.setOnQueryTextFocusChangeListener((view, b) -> {
            if (!b){
                searchViewBagian.setIconified(true);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showData() {
        databaseReference.child("DataJabatan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listJabatan = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        StoreJabatan divisi = item.getValue(StoreJabatan.class);
                        if (divisi != null) {
                            divisi.setKey(item.getKey());
                        }
                        listJabatan.add(divisi);
                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                }
                recyclerAdapter = new JabatanRecyclerAdapter(listJabatan, getActivity());
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