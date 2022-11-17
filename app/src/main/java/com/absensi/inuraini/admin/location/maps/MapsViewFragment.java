package com.absensi.inuraini.admin.location.maps;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.absensi.inuraini.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsViewFragment extends Fragment {

    SupportMapFragment mapFragment;
    Context mContext;
    SendDataInterface mSendDataInterface;
    private int mMapWidth = 1160;
    private int mMapHeight = 300;

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

        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.my_map);

        mapFragment.getView().setClickable(false);

        mapFragment.getMapAsync(googleMap -> {
            mapSync(googleMap, getLatlong);
        });
    }

    private void mapSync(GoogleMap googleMap, LatLng getLatlong) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.addMarker(new MarkerOptions().position(getLatlong));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatlong, 15.0f));

        googleMap.setOnMapLoadedCallback(() -> {
            mapFragment.getView().setDrawingCacheEnabled(true);
            mapFragment.getView().measure(View.MeasureSpec.makeMeasureSpec(mMapWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(mMapHeight, View.MeasureSpec.EXACTLY));
            mapFragment.getView().layout(0, 0, mMapWidth, mMapHeight);
            mapFragment.getView().buildDrawingCache(true);
            Bitmap b = Bitmap.createBitmap(mapFragment.getView().getDrawingCache());
            mapFragment.getView().setDrawingCacheEnabled(false);
            mSendDataInterface.sendData(b);
            mapFragment.getView().layout(0, 0, mMapWidth, mMapHeight);
        });
    }

    public interface SendDataInterface{
        void sendData(Bitmap dataBmp);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();

        Activity activity =(Activity)context ;
        try{
            mSendDataInterface= (SendDataInterface) activity;
        }catch(RuntimeException e){
            throw new RuntimeException(activity.toString()+" must implement method");
        }
    }
}