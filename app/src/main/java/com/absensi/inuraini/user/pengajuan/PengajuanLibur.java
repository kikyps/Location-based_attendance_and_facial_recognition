package com.absensi.inuraini.user.pengajuan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.absensi.inuraini.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PengajuanLibur extends Fragment {

    FloatingActionButton fab;
    Context mContext;

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
        fab = root.findViewById(R.id.fab);
    }

    private void contentLiteners() {
        fab.setOnClickListener(v -> {
            startActivity(new Intent(mContext, TambahIzinActivity.class));
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}