package com.absensi.inuraini.user.pengajuan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.absensi.inuraini.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PengajuanLibur extends Fragment {

    FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pengajuan_libur, container, false);
        // Inflate the layout for this fragment
        return root;
    }
}