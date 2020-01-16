package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap,mMap2;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        Intent I = getIntent();
        String uri = I.getData().toString();
        String[] userr = uri.split("=");
        user = userr[1];

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    LocationManager locationManager;
    LocationListener locationListener;

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    final HashMap<String, Marker> hashMapMarker= new HashMap<>();
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap2 = googleMap;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());
                Marker marker = hashMapMarker.get("Your Location");
                if(marker!=null)
                marker.remove();
                hashMapMarker.remove("Your Location");
                mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation, 15));
                marker = mMap2.addMarker(new MarkerOptions().position(userlocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                hashMapMarker.put("Your Location",marker);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},0);
            return;
        }
        Location userlastloc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng userlocation = new LatLng(userlastloc.getLatitude(), userlastloc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userlocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation,15));
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mReference = mDatabase.getReference("Users");
        if(user!=null) {
            mReference.child(user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Double lat = Double.parseDouble(user.lat);
                    Double lon = Double.parseDouble(user.lon);
                    setMap(lat,lon);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void setMap(Double lat, Double lon) {
//        mMap.clear();
        LatLng sydney = new LatLng(lat, lon);
        Marker marker = hashMapMarker.get("xyz");
        if(marker!=null)
        marker.remove();
        hashMapMarker.remove("xyz");
        mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
        marker = mMap2.addMarker(new MarkerOptions().position(sydney).title("xyz"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
        hashMapMarker.put("xyz",marker);

//        mMap.addMarker(new MarkerOptions().position(sydney).title("xyz"));

    }
}
