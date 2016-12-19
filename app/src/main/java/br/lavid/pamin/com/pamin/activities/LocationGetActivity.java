package br.lavid.pamin.com.pamin.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import br.lavid.pamin.com.pamin.R;

public class LocationGetActivity extends AppCompatActivity implements OnMapReadyCallback { //Adicionado implements OnMapReadyCallback

    private static final String TAG = "MAIN_ACTIVITY_ASYNC";

    private Context context=null;

    private GoogleApiClient googleApi;
    private MarkerOptions marker;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapView mapView;
    private AutoCompleteTextView textField;
    private ArrayAdapter<String> autoCompleteAdapter;
    private List<Address> addresses;
    private Button selecionar;

    private String where;
    private double lat, lng;
    private LatLng pos;

    private Collection<Integer> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationget);
        if (getActionBar() == null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else
            getActionBar().setDisplayHomeAsUpEnabled(true);

        context= this;

        textField = (AutoCompleteTextView) findViewById(R.id.activity_locationget_field);

        /*
        selecionar = (Button) findViewById(R.id.location_getBtn);
        selecionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendingResult();
            }
        });
        */
        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("") || mMap == null)
                    return;

                LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                AutocompleteFilter mAutocompleteFilter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                        .build();
                PendingResult result =
                        Places.GeoDataApi.getAutocompletePredictions(googleApi,
                                s.toString(),
                                latLngBounds,
                                mAutocompleteFilter);//AutocompleteFilter.create(places)) para mAutocompleteFilter

                result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {

                    @Override
                    public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
                        if (autocompletePredictions.getStatus().isSuccess()) {
                            autoCompleteAdapter.clear();
                            //Atualizando auto.getDescription() para auto.getFullText(null).toString()
                            for (AutocompletePrediction auto : autocompletePredictions) {
                                CharacterStyle c = new UnderlineSpan();
                                autoCompleteAdapter.add(auto.getFullText(null).toString());
                                Log.v("test", auto.getFullText(null).toString());
                            }
                            autocompletePredictions.release();
                            autoCompleteAdapter.notifyDataSetChanged();

                        }
                    }
                });
            }
        });
        textField.setThreshold(1);

        autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        places = new ArrayList<Integer>();
        places.add(Place.TYPE_STREET_ADDRESS);
        places.add(Place.TYPE_ESTABLISHMENT);

        textField.setAdapter(autoCompleteAdapter);

        googleApi = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                        Location userLatlng;
                        if (mMap != null) {
                            //Toast.makeText(LocationGetActivity.this, "Just Fine, mMap is null", Toast.LENGTH_LONG).show();
                            if (Build.VERSION.SDK_INT == 23) {
                                if (ContextCompat.checkSelfPermission(LocationGetActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(LocationGetActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                            userLatlng = LocationServices.FusedLocationApi.getLastLocation(googleApi);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(userLatlng.getLatitude(), userLatlng.getLongitude()), 14);
                            mMap.animateCamera(cameraUpdate);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            mMap.setMyLocationEnabled(true);


                            pos = new LatLng(userLatlng.getLatitude(), userLatlng.getLongitude());

                            mMap.clear();
                            marker = new MarkerOptions()
                                    .position(pos)
                                    .title("Aqui!");
                            mMap.addMarker(marker);

                            (new GeocodeTask()).execute();
                            //getAdress(new LatLng(userLatlng.getLatitude(), userLatlng.getLongitude()));
                            //Toast.makeText(LocationGetActivity.this, "Just Fine, mMap is null", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LocationGetActivity.this, "Erro, mMap is null", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

        setUpMapIfNeeded();
    }

    public void cancelBtn(View v) {
        onBackPressed();
    }

    public void getBtn(View v) {
        sendingResult();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApi.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApi.disconnect();
    }

    @Override
    protected void onPause() {
//        sendingResult();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            //MapFragment mapFrag = null;
            //mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.activity_location_get_map);
            //mapFrag.getMapAsync(LocationGetActivity.this);

            //Toast.makeText(LocationGetActivity.this, "Just Fine, mMap is null", Toast.LENGTH_LONG).show();
            //mapView.getMapAsync(LocationGetActivity.this);
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.activity_location_get_map)).getMapAsync(LocationGetActivity.this);//getMap()

            /*
            if (mMap != null) {
                Toast.makeText(LocationGetActivity.this, "This time is all right", Toast.LENGTH_LONG).show();
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Toast.makeText(LocationGetActivity.this, "I clicked, dont know how", Toast.LENGTH_LONG).show();
                        getAdress(latLng);
                    }
                });
                //DO THINGS
            }
            */
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    private void sendingResult() {
        Intent intent = new Intent();
        if (where != null && lat != 0 && lng != 0) {
            Log.v("LocGetAct", "Found Place, RESULT OK");
            intent.putExtra("where", where);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            setResult(RESULT_OK, intent);
        } else {
            Log.v("LocGetAct", "Found Place returned FAIL");
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    public void getAdress(Context ctx, LatLng latLng) {

        Geocoder geocoder;

        //List<Address> addresses;
        geocoder = new Geocoder(ctx, Locale.getDefault());

        lat = latLng.latitude;
        lng = latLng.longitude;

        try {
            int n = geocoder.getFromLocation(lat, lng, 2).size();
            Toast.makeText(this, "Tamanho do addresses: " + n, Toast.LENGTH_LONG).show();
            addresses = geocoder.getFromLocation(lat, lng, 1);
            //textField.setText(addresses.get(0).getAddressLine(0));
            //where = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(LocationGetActivity.this, "Error, permission", Toast.LENGTH_LONG).show();
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (mMap != null) {
            //Toast.makeText(LocationGetActivity.this, "This time is all right", Toast.LENGTH_LONG).show();
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    //Toast.makeText(LocationGetActivity.this, "I clicked, dont know how", Toast.LENGTH_LONG).show();
                    pos = latLng;

                    mMap.clear();
                    marker = new MarkerOptions()
                            .position(latLng)
                            .title("Aqui!");
                    mMap.addMarker(marker);

                    (new GeocodeTask()).execute();
                    //getAdress(context, latLng);
                }
            });
            //DO THINGS
        }
    }

    private class GeocodeTask extends AsyncTask<Void, Void, Address> {

        String errorMessage = "Error, address null";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Address doInBackground(Void ... none) {

            Geocoder geocoder = new Geocoder(LocationGetActivity.this, Locale.ENGLISH);
            //List<Address> addresses = null;


                double latitude = pos.latitude;
                double longitude = pos.longitude;

                lat = latitude;
                lng = longitude;

                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException ioException) {
                    errorMessage = "Service Not Available";
                    Log.e(TAG, errorMessage, ioException);
                } catch (IllegalArgumentException illegalArgumentException) {
                    errorMessage = "Invalid Latitude or Longitude Used";
                    Log.e(TAG, errorMessage + ". " +
                            "Latitude = " + latitude + ", Longitude = " +
                            longitude, illegalArgumentException);
                }

            if(addresses != null && addresses.size() > 0)
                return addresses.get(0);

            return null;
        }

        protected void onPostExecute(Address address) {
            if(address == null) {

                textField.setText(errorMessage);
            }
            else {
                //String addressName = "";
                /*
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressName += " --- " + address.getAddressLine(i);
                }
                textField.setVisibility(View.VISIBLE);
                textField.setText("Latitude: " + address.getLatitude() + "\n" +
                        "Longitude: " + address.getLongitude() + "\n" +
                        "Address: " + addressName);
                */
                textField.setText(addresses.get(0).getAddressLine(0));
                where = addresses.get(0).getAddressLine(0);
            }
        }
    }

}
