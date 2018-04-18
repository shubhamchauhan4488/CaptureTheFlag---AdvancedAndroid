package com.lambton.capturetheflag;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
//    private Location lastLocation;

    private MapFragment mapFragment;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize GoogleMaps

        databaseReference = FirebaseDatabase.getInstance().getReference().child("players");

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // create GoogleApiClient
        createGoogleApi();
        if(checkPermission()){
            Toast.makeText(this, "Go ahead and Create the Playing Area from Menu", Toast.LENGTH_SHORT).show();
        }else{
            askPermission();
        }
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    //Connecting GooGle api client on start()
    @Override
    protected void onStart() {
        super.onStart();
        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }
    //Disconnecting google api client on start()
    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }



    //Creting the 3 dots on top right : MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geofence: {
                startGeofence();
                getUpdateOnMap();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    // MARK : PERMISSIONS HANDLING
    private final int REQ_PERMISSION = 888;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, "Go ahead and Create the Playing Area from Menu", Toast.LENGTH_SHORT).show();
//                    getLastKnownLocation();

                } else {
                    // Permission denied
                    Log.w(TAG, "permissionsDenied()");
                }
                break;
            }
        }
    }
    //-------------PERMISSIONS HANDLING FINISHED -------------

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
//        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);

//        databaseReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                LatLng newLocation = new LatLng(
//                        dataSnapshot.child("latitude").getValue(Long.class),
//                        dataSnapshot.child("longitude").getValue(Long.class)
//                );
//                map.addMarker(new MarkerOptions()
//                        .position(newLocation)
//                        .title(dataSnapshot.getKey()));
//            }
//
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
 }

//    @Override
//    public void onMapClick(LatLng latLng) {
//        Log.d(TAG, "onMapClick("+latLng +")");
//        markerForGeofence(latLng);
//    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

//    private LocationRequest locationRequest;
//    // Defined in mili seconds.
//    // This number in extremely low, and should be used only for debug
//    private final int UPDATE_INTERVAL = 1000;
//    private final int FASTEST_INTERVAL = 900;
//
//    // Start location Updates
//    private void startLocationUpdates() {
//        Log.i(TAG, "startLocationUpdates()");
//        locationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(UPDATE_INTERVAL)
//                .setFastestInterval(FASTEST_INTERVAL);
//
//        if (checkPermission())
//            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
////        Log.d(TAG, "onLocationChanged ["+location+"]");
////        lastLocation =
////        writeActualLocation(location);
//    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
//        getLastKnownLocation();
//        recoverGeofenceMarker();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }



//    private void writeActualLocation(Location location) {
//        textLat.setText( "Lat: " + location.getLatitude() );
//        textLong.setText( "Long: " + location.getLongitude() );
//
//        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
//    }
//
//    private void writeLastLocation() {
//        writeActualLocation(lastLocation);
//    }

//    private Marker locationMarker;

//    private void markerLocation(LatLng latLng) {
//        Log.i(TAG, "markerLocation(" + latLng + ")");
//        String title = latLng.latitude + ", " + latLng.longitude;
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng)
//                .title(title);
//        if (map != null) {
//            if (locationMarker != null)
//                locationMarker.remove();
//            locationMarker = map.addMarker(markerOptions);
//            float zoom = 15f;
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
//            map.animateCamera(cameraUpdate);
//        }
//    }


    private Marker geoFenceMarker;
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (map != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();
            geoFenceMarker = map.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    // Start Geofence creation process
    private void startGeofence() {
        LatLng college = new LatLng(43.716412, -79.33437789999999);
        markerForGeofence(college);
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Log.e("log", "Geofence marker CREATED");
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 1000.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
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
    @NonNull
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }


    //Making a service to give push notifications when User enters or exists the geofence
    //TO BE ADDED IN THE PLAYER APP alongwith GeoTransitionservice java file
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            Log.i(TAG, "Status is successful");
//            saveGeofence();
            drawGeofence();
        } else {
            Log.i(TAG, "Status is NOT successful");
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }

//    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
//    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
//    private void saveGeofence() {
//        Log.d(TAG, "saveGeofence()");
//        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//
//        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(geoFenceMarker.getPosition().latitude));
//        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(geoFenceMarker.getPosition().longitude));
//        editor.apply();
//    }

    // Recovering last Geofence marker
//    private void recoverGeofenceMarker() {
//        Log.d(TAG, "recoverGeofenceMarker");
//        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );
//
//        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
//            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
//            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
//            LatLng latLng = new LatLng( lat, lon );
//            markerForGeofence(latLng);
//            drawGeofence();
//        }
//    }

    //Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    //Removing geofence
    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if (geoFenceMarker != null)
            geoFenceMarker.remove();
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
    }

    //get data from fire base and show it on the map.
    public void getUpdateOnMap(){
//        = null;
        if(valueEventListener  == null){
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: ==============Sanapshot==========="+dataSnapshot.toString());
                    List<Player> players =  new ArrayList<>();
//                    Log.d(TAG, "onDataChange: $$$$$$$$$$$$$$$$$$$$$$$$$$");
//                    Log.d(TAG, "onChildAdded: ==================PLayer Update============= "+ dataSnapshot.getValue().toString());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Log.d(TAG, "onDataChange: ==================Player Object To String========"+snapshot.getValue().toString());
                        Player player = snapshot.getValue(Player.class);
                        players.add(player);
                        Log.d(TAG, "Name: "+ player.playerName);
//                        Log.d(TAG, "Latitude: "+player.latitude);
//                        Log.d(TAG, "Longitude: "+player.longitude);
//                        Log.d(TAG, "Team Name: "+player.teamName);
                    }
                    setPlayerMaker(players);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            databaseReference.addValueEventListener(valueEventListener);

        }
    }

    public void setPlayerMaker(List<Player> players){
        if(players.size() != 0) {
            map.clear();
            for (Player player : players) {
                LatLng latLng = new LatLng(player.latitude,player.longitude);
                map.addMarker(new MarkerOptions().position(latLng).title(player.getPlayerName()));
            }
        }
    }


}

