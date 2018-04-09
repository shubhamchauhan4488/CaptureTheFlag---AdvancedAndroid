package com.lambton.capturetheflag;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.Manifest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
<<<<<<< HEAD
import com.google.android.gms.maps.model.CircleOptions;
=======
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
>>>>>>> 60d9c9545c408b7950e7c9c2bb11ba3799d9bac1
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Geofire extends FragmentActivity implements OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;

    private static final String TAG = MainActivity.class.getSimpleName();
    private Marker geoFenceMarker;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    GoogleApiClient googleapiclient = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofire);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        googleapiclient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }
                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Log.d(TAG, "Failed to connect to Google API Client -" + connectionResult.getErrorMessage());
<<<<<<< HEAD
                        requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,

                                    Manifest.permission.ACCESS_COARSE_LOCATION
=======
                        requestPermissions(new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
>>>>>>> 60d9c9545c408b7950e7c9c2bb11ba3799d9bac1
                        },1234);

                    }
                })
                .addApi( LocationServices.API )
                .build();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(43.7822457, -79.349838);
<<<<<<< HEAD
        mMap.addMarker(new MarkerOptions().position(sydney).title("Lambton College"));
        LatLng dangerous_area = new LatLng(37.7533,-122.4056);
        mMap.addCircle(new CircleOptions()
                .center(dangerous_area)
                .radius(500) //in mts
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)
=======

        geoFenceMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Lambton College"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        beginGeofence();
>>>>>>> 60d9c9545c408b7950e7c9c2bb11ba3799d9bac1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geofence: {
                beginGeofence();

            }
        }
        return true;
    }

        private Geofence createGeofence (LatLng latLng,float radius ){
            Log.d(TAG, "createGeofence");
            return new Geofence.Builder()
                    .setRequestId(GEOFENCE_REQ_ID)
                    .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                    .setExpirationDuration(GEO_DURATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                            | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        }


        // Create a Geofence Request
        private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
            Log.d(TAG, "createGeofenceRequest");
            return new GeofencingRequest.Builder()
                    .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                    .addGeofence( geofence )
                    .build();
        }


        // Start Geofence creation process
        private void beginGeofence () {
            Log.i(TAG, "startGeofence()");


            if (geoFenceMarker != null) {
                Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
                GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            }
            else {
                Log.e(TAG, "Geofence marker is null");

            }
}

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleapiclient.connect();
    }


}