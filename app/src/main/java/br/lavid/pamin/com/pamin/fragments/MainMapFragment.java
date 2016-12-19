package br.lavid.pamin.com.pamin.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.activities.CulturalRegisterActivity;
import br.lavid.pamin.com.pamin.activities.MainActivity;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.models.User;


public class MainMapFragment extends com.google.android.gms.maps.SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LinkedList<CulturalRegister> allList;

    public MainMapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.getMapAsync(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        this.googleMap = map;
        LatLng userLocation = null;

        allList = MainActivity.getActCulturalRegisters();
        if (User.getInstance(getActivity()).getUserLocation() != null) {
            userLocation = new LatLng(
                    User.getInstance(getActivity()).getUserLocation().getLatitude(),
                    User.getInstance(getActivity()).getUserLocation().getLongitude()
            );
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)
                        , 19.0f));

                for (CulturalRegister cr : MainActivity.actCulturalRegisters)
                    if (cr.checkPositionAndName(marker.getTitle(), marker.getPosition())) {
                        Intent intent = new Intent(getActivity(), CulturalRegisterActivity.class);
                        intent.putExtra("culturalRegister", cr.getIdCulturalRegister());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.smart_card_in, R.anim.smart_card_in2);
                    }
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(MainMapFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainMapFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //Toast.makeText(getActivity(), "Error permission denied here in MainMapFragment", Toast.LENGTH_LONG).show();
                return;
            }
        }

        map.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        if (location != null)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , 12.0f));
        else if (userLocation != null)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12.0f));

        pinElements();

    }

    private void pinElements() {

        googleMap.clear();

        for (CulturalRegister culturalRegister : allList) {
            int icon = 0;
            switch (culturalRegister.getCategory()) {
                case "Lugares": {
                    icon = R.drawable.places;
                    break;
                }
                case "Celebrações": {
                    icon = R.drawable.celeb;
                    break;
                }
                case "Saberes": {
                    icon = R.drawable.know;
                    break;
                }
                default: {
                    icon = R.drawable.places;
                }
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(culturalRegister.getLatitude(), culturalRegister.getLongitude()))
                    .title(culturalRegister.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(icon));
            googleMap.addMarker(markerOptions);
        }
    }


    public void goToPosition(LatLng position) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 18.0f));

    }

    public void updateList() {
        LatLng userLocation = null;

        allList = MainActivity.getActCulturalRegisters();
        if (User.getInstance(getActivity()).getUserLocation() != null) {
            userLocation = new LatLng(
                    User.getInstance(getActivity()).getUserLocation().getLatitude(),
                    User.getInstance(getActivity()).getUserLocation().getLongitude()
            );
        }

        if (googleMap == null) {return;}

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(MainMapFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainMapFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }

        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        if (location != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , 12.0f));
        else if (userLocation != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12.0f));

        pinElements();
    }
}
