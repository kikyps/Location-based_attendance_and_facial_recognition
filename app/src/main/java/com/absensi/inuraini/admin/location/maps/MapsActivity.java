package com.absensi.inuraini.admin.location.maps;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.absensi.inuraini.OnDoubleClickListener;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.location.DataKordinat;
import com.absensi.inuraini.databinding.ActivityMapsBinding;
import com.absensi.inuraini.spotlight.OnSpotlightStateChangedListener;
import com.absensi.inuraini.spotlight.Spotlight;
import com.absensi.inuraini.spotlight.shape.Circle;
import com.absensi.inuraini.spotlight.target.CustomTarget;
import com.absensi.inuraini.spotlight.target.Target;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap;
    ActivityMapsBinding binding;
    Context context = this;
    Object[][] myLatLong = new Object[2][13];
    LatLng getLatlong;
    TextView alamat;
    CardView cardAddress;
    FloatingActionButton back, mycur;
    SupportMapFragment mapFragment;
    ImageView mark;
    MaterialSearchBar searchBar;
    PlacesClient placesClient;
    List<AutocompletePrediction> predictionList;
    boolean setOnce = true;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    float ZOOM_CAMERA_VIEW = 17.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        contentListeners();
    }

    private void contentListeners() {
        back = findViewById(R.id.back);
        mycur = findViewById(R.id.cur_now);
        mark = findViewById(R.id.marker);
        searchBar = findViewById(R.id.searchBar);
        alamat = findViewById(R.id.titik_lokasi);
        cardAddress = findViewById(R.id.card_address);

        boolean seeLocation = getIntent().getBooleanExtra("seeLocation", false);
        String getLatitude = getIntent().getStringExtra("getAbsenLatitude");
        String getLongitude = getIntent().getStringExtra("getAbsenLongitude");
        String getAddres = getIntent().getStringExtra("getAbsenLokasi");

        myLatLong = Preferences.getMyLocation(context, MapsActivity.this);

        searchAddress();

        back.setOnClickListener(v -> finish());

        mapFragment.getMapAsync(googleMap -> {
            if (seeLocation) {
                mMap = googleMap;
                mark.setVisibility(View.GONE);
                searchBar.setVisibility(View.GONE);
                searchBar.setEnabled(false);
                cardAddress.setVisibility(View.GONE);
//                myLatLong = Preferences.getMyLocation(context, MapsActivity.this);

                // Add a marker in Sydney and move the camera
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                getLatlong = new LatLng(Double.parseDouble(getLatitude), Double.parseDouble(getLongitude));
                mMap.addMarker(new MarkerOptions().position(getLatlong).title(getAddres));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatlong, ZOOM_CAMERA_VIEW));

                try {
                    assert mapFragment.getView() != null;
                    final ViewGroup parent = (ViewGroup) mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton").getParent();
                    parent.post(() -> {
                        try {
                            for (int i = 0, n = parent.getChildCount(); i < n; i++) {
                                View view = parent.getChildAt(i);
                                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                // position on right bottom
                                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                                rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                rlp.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                                rlp.rightMargin = rlp.leftMargin = 80;
                                rlp.bottomMargin = 280;
                                view.requestLayout();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                mycur.setOnClickListener(v -> {
//              mMap.moveCamera(CameraUpdateFactory.newLatLng(getLatlong));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getLatlong, ZOOM_CAMERA_VIEW));
                });
            } else {
                mMap = googleMap;
                mark.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
                searchBar.setEnabled(true);
                cardAddress.setVisibility(View.GONE);
                getDBLatlong();

                googleMap.setOnCameraIdleListener(() -> {
                    getLatlong = mMap.getCameraPosition().target;
                    alamat.setText(Preferences.getAddressFromLocation(context, getLatlong.latitude, getLatlong.longitude)[0]);
                });

                googleMap.setOnMapLongClickListener(latLng -> {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_CAMERA_VIEW));
                });

                if (!Preferences.getMapsGuide(context)) {
                    Runnable runnable = this::setCustomSpotLight;
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(runnable, 1000);
                } else {
                    cardAddress.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getDBLatlong() {
        databaseReference.child("data").child("latlong").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String dbLatitude = snapshot.child("sLatitude").getValue(String.class);
                    String dbLongitude = snapshot.child("sLongitude").getValue(String.class);
                    getLatlong = new LatLng(Double.parseDouble(dbLatitude), Double.parseDouble(dbLongitude));

                    // Add a marker in Sydney and move the camera
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                    mMap.addMarker(new MarkerOptions().position(getLatlong).title(Preferences.getAddressFromLocation(context, Double.parseDouble(dbLatitude), Double.parseDouble(dbLongitude))[0]));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatlong, ZOOM_CAMERA_VIEW));

                    try {
                        assert mapFragment.getView() != null;
                        final ViewGroup parent = (ViewGroup) mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton").getParent();
                        parent.post(() -> {
                            try {
                                for (int i = 0, n = parent.getChildCount(); i < n; i++) {
                                    View view = parent.getChildAt(i);
                                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                    // position on right bottom
                                    rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                                    rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                                    rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                    rlp.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                                    rlp.rightMargin = rlp.leftMargin = 60;
                                    rlp.topMargin = 250;
                                    view.requestLayout();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    mycur.setOnClickListener(new OnDoubleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            LatLng myLatlong = new LatLng(Double.parseDouble(dbLatitude), Double.parseDouble(dbLongitude));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatlong, ZOOM_CAMERA_VIEW));
                            if (setOnce)
                                Toast.makeText(context, "Tekan dan tahan untuk mengatur lokasi absensi", Toast.LENGTH_LONG).show();
                            setOnce = false;
                        }

                        @Override
                        public void onDoubleClick(View v) {
                            LatLng myLatlong = new LatLng((Double) myLatLong[0][0], (Double) myLatLong[0][1]);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatlong, ZOOM_CAMERA_VIEW));
                        }
                    });

                    mycur.setOnLongClickListener(v -> {
                        Preferences.showDialog(context,
                                null,
                                "Konfirmasi",
                                "Set titik lokasi anda saat ini sebagai lokasi absensi?",
                                "Ya",
                                "Cancel",
                                null,
                                (dialog, which) -> {
//                                Positive Button
                                    DataKordinat dataKordinat = new DataKordinat(String.valueOf(getLatlong.latitude), String.valueOf(getLatlong.longitude), Preferences.getAddressFromLocation(context, getLatlong.latitude, getLatlong.longitude)[12]);
                                    Map<String, Object> postValues = dataKordinat.toMap();

                                    databaseReference.child("data").child("latlong").updateChildren(postValues).addOnSuccessListener(unused -> {
                                        mMap.clear();
                                        mMap.addMarker(new MarkerOptions().position(getLatlong).title(Preferences.getAddressFromLocation(context, Double.parseDouble(dbLatitude), Double.parseDouble(dbLongitude))[1]));
                                        Toast.makeText(context, "Lokasi anda saat ini di set sebagai lokasi absensi karyawan", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(context, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show();
                                    });
                                },
                                (dialog, which) -> {
//                                Negative Button
                                    dialog.dismiss();
                                },
                                (dialog, which) -> {
//                                Neutral Button
                                    dialog.dismiss();
                                },
                                true,
                                true);
                        return true;
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setCustomSpotLight(){
        LayoutInflater inflater = LayoutInflater.from(context);

        ArrayList<Target> targets = new ArrayList<>();

        // make an target
        View first = inflater.inflate(R.layout.custom_spotlight, null);
        final CustomTarget firstTarget =
                new CustomTarget.Builder(MapsActivity.this).setPoint(mycur)
                        .setShape(new Circle(100f))
                        .setOverlay(first)
                        .build();

        targets.add(firstTarget);

        View second = inflater.inflate(R.layout.custom_spotlight2, null);
        final CustomTarget secondTarget =
                new CustomTarget.Builder(MapsActivity.this).setPoint(mycur)
                        .setShape(new Circle(100f))
                        .setOverlay(second)
                        .build();

        targets.add(secondTarget);

        View third = inflater.inflate(R.layout.custom_spotlight3, null);
        final CustomTarget thirdTarget =
                new CustomTarget.Builder(MapsActivity.this).setPoint(mycur)
                        .setShape(new Circle(100f))
                        .setOverlay(third)
                        .build();

        targets.add(thirdTarget);

        final Spotlight spotlight =

                Spotlight.with(MapsActivity.this)
                        .setOverlayColor(R.color.background)
                        .setDuration(1000L)
                        .setAnimation(new DecelerateInterpolator(2f))
                        .setTargets(targets)
                        .setClosedOnTouchedOutside(false)
                        .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                            @Override
                            public void onStarted() {

                            }

                            @Override
                            public void onEnded() {
                                Preferences.setMapsGuide(context, true);
                                cardAddress.setVisibility(View.VISIBLE);
                            }
                        });
        spotlight.start();

        View.OnClickListener closeOne = v -> {
            spotlight.closeCurrentTarget();
        };

        View.OnClickListener closeDouble = new OnDoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Toast.makeText(context, "Klik 2 kali secara cepat!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDoubleClick(View v) {
                spotlight.closeCurrentTarget();
            }
        };

        View.OnLongClickListener closeLong = v -> {
            spotlight.closeCurrentTarget();
            return true;
        };

        first.findViewById(R.id.close_curSpotlight).setOnClickListener(closeOne);
        second.findViewById(R.id.close_curSpotlight).setOnClickListener(closeDouble);
        third.findViewById(R.id.close_curSpotlight).setOnLongClickListener(closeLong);
    }

    public void searchAddress() {
        Places.initialize(context, getString(R.string.google_maps_api));
        placesClient = Places.createClient(context);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
//                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
//                    //opening or closing a navigation drawer
//                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
//                    searchBar.closeSearch();
//                }
            }
        });

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
//                        .setCountry("id")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                        if (predictionsResponse != null) {
                            predictionList = predictionsResponse.getAutocompletePredictions();
                            List<String> suggestionsList = new ArrayList<>();
                            for (int i = 0; i < predictionList.size(); i++) {
                                AutocompletePrediction prediction = predictionList.get(i);
                                suggestionsList.add(prediction.getFullText(null).toString());
                            }
                            searchBar.updateLastSuggestions(suggestionsList);
                            if (!searchBar.isSuggestionsVisible()) {
                                searchBar.showSuggestionsList();
                            }
                        }
                    } else {
                        Log.i("mytag", "Prediction fetching task unsuccessful Error code : " + task.getException().getMessage());
//                        Toast.makeText(context, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Preferences.showDialog(context,
                                null,
                                "Pemberitahuan",
                                "Fitur ini dalam tahap pengembangan!, untuk saat ini fitur pencarian lokasi belum tersedia",
                                "Mengerti",
                                null,
                                null,
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    searchBar.closeSearch();
                                },
                                (dialog, which) -> dialog.dismiss(),
                                (dialog, which) -> dialog.dismiss(),
                                false,
                                true);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = searchBar.getLastSuggestions().get(position).toString();
                searchBar.setText(suggestion);

                new Handler().postDelayed(() -> searchBar.clearSuggestions(), 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    Log.i("mytag", "Place found: " + place.getName());
                    LatLng latLngOfPlace = place.getLatLng();
                    if (latLngOfPlace != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, ZOOM_CAMERA_VIEW));
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        apiException.printStackTrace();
                        int statusCode = apiException.getStatusCode();
                        Log.i("mytag", "place not found: " + e.getMessage());
                        Log.i("mytag", "status code: " + statusCode);
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}