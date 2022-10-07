package com.absensi.inuraini.admin.verifvyaccount;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.absensi.inuraini.R;

public class VerifAccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verif_account, container, false);
        // Inflate the layout for this fragment
        layoutBinding(root);
        return root;
    }

    private void layoutBinding(View root) {

    }
}