package com.absensi.inuraini.admin.location.maps;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.absensi.inuraini.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsViewFragment extends Fragment {

    SupportMapFragment mapFragment;
    Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps_view, container, false);
        // Inflate the layout for this fragment
        viewBinding(view);
        return view;
    }

    private void viewBinding(View view) {

        contentListeners();
    }

    private void contentListeners() {
        String strLatitude = getArguments().getString("viewMyLatitude");
        String strLongitude = getArguments().getString("viewMyLongitude");
        String strLokasi = getArguments().getString("viewMyLokasi");
        LatLng getLatlong = new LatLng(Double.parseDouble(strLatitude), Double.parseDouble(strLongitude));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.my_map);
        mapFragment.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.addMarker(new MarkerOptions().position(getLatlong));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatlong, 15.0f));
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
    }
}